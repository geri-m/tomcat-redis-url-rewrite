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

public class RedisRewrite extends ValveBase {

    private static final Log log = LogFactory.getLog(RedisRewrite.class);

    private String host = "localhost";
    private int port = 6379;
    private int timeout = 100;

    private JedisPool pool;

    public RedisRewrite(){
        super();
        log.debug("Creating RedisRewrite Valve");
        createPool();
    }

    public void setHost(String host){
        log.info("setHost: " + host);
        this.host = host;
        createPool();
    }

    public void setPort(int port){
        log.info("setPort: " + port);
        this.port = port;
        createPool();
    }

    public void setTimeout(int timeout){
        log.info("setTimeout: " + timeout);
        this.timeout = timeout;
        createPool();
    }

    // TODO: Create a Pool with 100 something connection. Check the average amount of concurrent requests.
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

    // --------------------------------------------------------- Public Methods

    @Override
    public void invoke(Request request, Response response) throws IOException {
        long start = System.currentTimeMillis();


        String url = pool.getResource().get("events/city/rome");

        if(url != null){
            response.sendRedirect(url, HttpServletResponse.SC_MOVED_TEMPORARILY);

        } else {
            log.info(String.format("No Rewrite Done for '%s', Duration: '%s' ms", createUrlWithPath(request), (System.currentTimeMillis() - start)));
        }
    }

}
