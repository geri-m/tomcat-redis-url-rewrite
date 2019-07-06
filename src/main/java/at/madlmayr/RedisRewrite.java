package at.madlmayr;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class RedisRewrite extends RequestFilterValve {

    private static final Log log = LogFactory.getLog(RedisRewrite.class);

    // --------------------------------------------------------- Public Methods

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        log.info("Start: " + request.getRequestURI());
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
