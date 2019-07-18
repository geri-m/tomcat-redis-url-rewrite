package at.madlmayr;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRedisBasis {

    @Test
    public void connectToRedisAndRWData(){
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("events/city/rome", "32,15,223,828");
        String cachedResponse = jedis.get("events/city/rome");
        assertEquals(cachedResponse, "32,15,223,828");
    }

    @Test
    public void regexUrlParameterMatching(){
        // Cheat Sheet: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
        // ^ = beginning of line
        // ? = once, or not at all
        // .* = match any Char
        // $  = end of line

        // First  - there can be the Google Click Id (gclid). '?', as this occurs only once or not. (right after beginning of line)
        // Second - some sting + there must be a shop, brand or tag parameter.
        // Third  - there might be other parameters, wo don't use (shop, sale, searchstring, gender)  + '&' as separator
        // Forth  - the last parameter, which we store as the remainder (right before one-of-line)

        Pattern p = Pattern.compile("^(gclid=.*)?(?<start>.*(shop|brand|tag)=[0-9]+)+&?(.*(shop|sale|searchstring|gender)=.*?&?)*(|&(?<remainder>.*))$");

        // This is the path from the HTTP Request
        Matcher matcher = p.matcher("/search.action?gclid=asdfasdf&shop=11111&brand=1111");

        // don't do matcher(), only find() to get the groups back.
        // boolean b = matcher.matches();
        while (matcher.find()) {
            System.out.println("Start    : " + matcher.group("start"));
            System.out.println("Remainder: " + matcher.group("remainder"));
        }
    }

}
