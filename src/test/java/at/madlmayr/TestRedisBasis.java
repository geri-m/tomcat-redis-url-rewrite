package at.madlmayr;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRedisBasis {

    @Test
    public void connectToRedisAndRWData(){
        Jedis jedis = new Jedis();
        jedis.set("events/city/rome", "32,15,223,828");
        String cachedResponse = jedis.get("events/city/rome");
        assertEquals(cachedResponse, "32,15,223,828");
    }

}
