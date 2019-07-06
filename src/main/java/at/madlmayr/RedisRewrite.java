package at.madlmayr;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedisRewrite extends RequestFilterValve {

    private static final Log log = LogFactory.getLog(RedisRewrite.class);

    private String host = "localhost";
    private int port = 6379;
    private int timeout = 100;

    private JedisPool pool;

    public RedisRewrite(){
        super();
        log.debug("constructor");
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

    private void createPool(){
        log.info(String.format("create Pool: %s:%s, %s ms", host ,port , timeout));
        pool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
    }

    // --------------------------------------------------------- Public Methods

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        long start = System.currentTimeMillis();

        String property;
        if (getAddConnectorPort()) {
            property = request.getRequest().getRemoteAddr() + ";" + request.getConnector().getPort();
        } else {
            property = request.getRequest().getRemoteAddr();
        }

        String url = pool.getResource().get("events/city/rome");

        if(url != null){
            response.sendRedirect(url, HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            process(property, request, response);
        }

        log.info(String.format("Property '%s' Done '%s', Duration: '%s' ms", property, request.getRequestURI(), (System.currentTimeMillis() - start)));
    }

    @Override
    protected Log getLog() {
        return log;
    }
}
