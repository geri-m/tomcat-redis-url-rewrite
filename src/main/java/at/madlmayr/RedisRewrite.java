package at.madlmayr;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletException;
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
        log.info("Start: " + request.getRequestURI());

        pool.getResource().get("events/city/rome");

        String property;
        if (getAddConnectorPort()) {
            property = request.getRequest().getRemoteAddr() + ";" + request.getConnector().getPort();
        } else {
            property = request.getRequest().getRemoteAddr();
        }
        process(property, request, response);
        log.info("Done, Duration: " + (System.currentTimeMillis() - start) + " ms");
    }

    @Override
    protected Log getLog() {
        return log;
    }
}
