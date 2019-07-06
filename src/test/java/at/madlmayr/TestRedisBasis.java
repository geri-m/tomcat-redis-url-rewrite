package at.madlmayr;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRedisBasis {

    @Test
    public void connectToRedisAndRWData(){
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("events/city/rome", "32,15,223,828");
        String cachedResponse = jedis.get("events/city/rome");
        assertEquals(cachedResponse, "32,15,223,828");
    }

}
