package at.madlmayr;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RedisRewriteValve extends ValveBase {

    private static final Log log = LogFactory.getLog(RedisRewriteValve.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private MessageDigest digest;
    private String host = "localhost";
    private int port = 6379;
    private int timeout = 100;

    private JedisPool pool;

    public RedisRewriteValve(){
        super();

        // Create SHA-256 of URL as this is the key for the URLs in Redis
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }

        createPool();
        log.debug("Creating RedisRewriteValve Valve");
    }

    // parameter set via initialization in manager.xml
    public void setHost(String host){
        log.info("setHost: " + host);
        this.host = host;
        createPool();
    }

    // parameter set via initialization in manager.xml
    public void setPort(int port){
        log.info("setPort: " + port);
        this.port = port;
        createPool();
    }

    // parameter set via initialization in manager.xml
    public void setTimeout(int timeout){
        log.info("setTimeout: " + timeout);
        this.timeout = timeout;
        createPool();
    }

    // TODO: Create a Pool with 100 something connection. Check the average amount of concurrent requests.
    // we can not determine the call of the setters with the parameters, so after each set was called,
    // the connection pool is created with updated parameters.
    private void createPool(){
        log.info(String.format("create Pool: %s:%s, %s ms", host ,port , timeout));
        pool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
    }


    /**
     * Simpel Method to create out of a request object a String with URL and the Query Parameter.
     * @param request HTTP Request to the Webserver
     * @return String in the format of http(s)://<host>/path/method?queryString
     */
    private static String createUrlWithPath(Request request){
         StringBuffer s;
         s = request.getRequestURL();

         if(request.getQueryString() == null ||request.getQueryString().isEmpty()){
             s.append("?");
             s.append(request.getQueryString());
         }

         return s.toString();
    }

    // Converts a byte-Array into a Hex-string. (keep this efficient!)
    // Taken from: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Rewrite Function of the Valve, that we are overwriting. Keep this as quick as possible
     *
     * @param request HTTP Request to the server
     * @param response HTTP Response from this Valve (might be modified)
     * @throws IOException in case something broke the request processing
     */

    @Override
    public void invoke(Request request, Response response) throws IOException {
        long start = System.currentTimeMillis();

        byte[] hashOfUrl = digest.digest(
                createUrlWithPath(request).getBytes(StandardCharsets.UTF_8));

        // Lookup the URL with this Hash in Redis.
        String url = pool.getResource().get(bytesToHex(hashOfUrl));

        // if an appropriate URL was found in Redis do a redirect
        if(url != null){
            response.sendRedirect(url, HttpServletResponse.SC_MOVED_TEMPORARILY);
            log.info(String.format("Rewrite from '%s' > '%s', Duration '%s' ms", createUrlWithPath(request), url,(System.currentTimeMillis() - start)));
        } else {
            log.info(String.format("No Rewrite Done for '%s', Duration: '%s' ms", createUrlWithPath(request), (System.currentTimeMillis() - start)));
        }
    }

}
