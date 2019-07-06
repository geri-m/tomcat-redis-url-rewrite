package at.madlmayr;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import java.io.IOException;

public class RedisRewrite extends RequestFilterValve {

    private static final Log log = LogFactory.getLog(RedisRewrite.class);

    private String host = "localhost";
    private int port = 6379;

    private static Jedis singleton;

    public synchronized static Jedis getJedis(String host, int port){
        if(singleton == null)
            singleton = new Jedis(host, port);

        return singleton;
    }

    public RedisRewrite(){
        super();
        log.debug("const");
    }

    public void setHost(String host){
        log.info("setHost" + host);
        this.host = host;
    }

    public void setPort(int port){
        log.info("setPort" + port);
        this.port = port;
    }

    // --------------------------------------------------------- Public Methods

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        log.info("Start: " + request.getRequestURI());

        getJedis(this.host, this.port).get("events/city/rome");

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
