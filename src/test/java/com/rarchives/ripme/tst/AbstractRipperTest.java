package com.rarchives.ripme.tst;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.tst.TestRipper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.utils.Utils;
import redis.clients.jedis.Jedis; 
import static org.junit.jupiter.api.Assertions.assertEquals;
import redis.embedded.RedisServer;

public class AbstractRipperTest {

   @Test
   public void testGetFileName() throws IOException {
      String fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), "test", "test");
      assertEquals("test.test", fileName);

      fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), "test", null);
      assertEquals("test", fileName);

      fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), null, null);
      assertEquals("Object", fileName);

      fileName = AbstractRipper.getFileName(new URL("http://www.test.com/file.png"), null, null);
      assertEquals("file.png", fileName);

      fileName = AbstractRipper.getFileName(new URL("http://www.test.com/file."), null, null);
      assertEquals("file.", fileName);
   }

   @Test
   public void testHasDownloadedURL() throws IOException {
      int testRedisPort = 6379;
      RedisServer redisServer = new RedisServer(testRedisPort);
      try {

         URL ripURL = new URL("https://example.com");
         URL fileURL = new URL("https://example.com/picture.jpg");
         TestRipper ripper = new TestRipper(ripURL);
         redisServer.start();
         Jedis jedis = new Jedis("localhost", testRedisPort);
         // Test with empty redis
         Utils.setConfigString("url_history.redis_cache.host", "localhost");
         Utils.setConfigString("url_history.redis_cache.port", Integer.toString(testRedisPort));
         // Make the ripper connect to redis
         ripper.callInitializeHistoryStore();
         boolean hasAlreadyDownloaded = ripper.callHasDownloadedURL(fileURL.toString());
         assertEquals(false, hasAlreadyDownloaded);
         
         // Test with URL loaded into redis
         String keyPrefix = "somePrefix";
         Utils.setConfigString("url_history.redis_cache.key_prefix", keyPrefix);
         String key = keyPrefix + fileURL.toString().trim();
         jedis.set(key, "true");
         hasAlreadyDownloaded = ripper.callHasDownloadedURL(fileURL.toString());
         assertEquals(true, hasAlreadyDownloaded);

         redisServer.stop();

         
         // Re-initialize and test using hash set instead
         Utils.setConfigString("url_history.redis_cache.host", "");
         ripper.callInitializeHistoryStore();
         hasAlreadyDownloaded = ripper.callHasDownloadedURL(fileURL.toString());
         assertEquals(false, hasAlreadyDownloaded);

         // Test using hashset with URL added
         ripper.insertToURLHashSet(fileURL.toString());
         hasAlreadyDownloaded = ripper.callHasDownloadedURL(fileURL.toString());
         assertEquals(true, hasAlreadyDownloaded);
      } catch (Exception exception) {
         // Ensure that the redis server is destroyed, otherwise test re-runs will fail because it can't start a new server
         // on the same port
         redisServer.stop();
         throw exception;
      }
   }

   @Test
   public void testWriteDownloadedURL() throws IOException {
      int testRedisPort = 6379;
      RedisServer redisServer = new RedisServer(testRedisPort);
      try {
         URL ripURL = new URL("https://example.com");
         URL fileURL = new URL("https://example.com/picture.jpg");
         TestRipper ripper = new TestRipper(ripURL);
         redisServer.start();
         Jedis jedis = new Jedis("localhost", testRedisPort);
         String keyPrefix = "somePrefix";
         String key = keyPrefix + fileURL.toString().trim();
         Utils.setConfigString("url_history.redis_cache.key_prefix", keyPrefix);
         Utils.setConfigString("url_history.redis_cache.host", "localhost");
         Utils.setConfigString("url_history.redis_cache.port", Integer.toString(testRedisPort));
         Boolean urlInHashSet = ripper.checkURLInHashSet(fileURL.toString());
         String jedisResult = jedis.get(key);
         assertEquals(null, jedisResult);
         assertEquals(false, urlInHashSet);
         ripper.callInitializeHistoryStore();
         ripper.callWriteDownloadedURL(fileURL.toString());
         urlInHashSet = ripper.checkURLInHashSet(fileURL.toString());
         assertEquals(true, urlInHashSet);
         jedisResult = jedis.get(key);
         assertEquals("true", jedisResult);
         redisServer.stop();
      } catch (Exception exception) {
         // Ensure that the redis server is destroyed, otherwise test re-runs will fail because it can't start a new server
         // on the same port
         redisServer.stop();
         throw exception;
      }
   }

}
