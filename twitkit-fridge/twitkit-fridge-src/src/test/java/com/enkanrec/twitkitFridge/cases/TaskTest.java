/*
 * Author : Rinka
 * Date   : 2020/2/7
 */
package com.enkanrec.twitkitFridge.cases;

import com.enkanrec.twitkitFridge.helper.MvcHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class : TaskTest
 * Usage :
 */
@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@TestPropertySource("classpath:application-test.properties")
public class TaskTest {

    private static final String BASE_URL = "/api/db/task";

    @Autowired
    private WebApplicationContext ctx;

    private MvcHelper helper;

    @Before
    public void setUp() {
        this.helper = new MvcHelper(BASE_URL, MockMvcBuilders.webAppContextSetup(ctx).build());
    }

    @Transactional
    @Test
    public void getTask() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("tid", 1000);
        Map<String, Object> respData = helper.apiPost("/get", data, Map.class);
        Assert.assertTrue(respData.containsKey("twitter"));
        Assert.assertTrue(respData.containsKey("translation"));
        Map twitter = (Map) respData.get("twitter");
        Assert.assertEquals(1000, twitter.get("tid"));
        Assert.assertEquals("内容0", twitter.get("content"));
        Assert.assertEquals("[]", twitter.get("media"));
        Map translation = (Map) respData.get("translation");
        Assert.assertEquals(2, translation.get("version"));
        Assert.assertEquals("翻译A3", translation.get("translation"));
        Assert.assertEquals("[img3]", translation.get("img"));

        Map<String, Object> data2 = new HashMap<>();
        data2.put("tid", 1001);
        Map<String, Object> respData2 = helper.apiPost("/get", data2, Map.class);
        Assert.assertTrue(respData2.containsKey("twitter"));
        Assert.assertTrue(respData2.containsKey("translation"));
        Map twitter2 = (Map) respData2.get("twitter");
        Assert.assertEquals(1001, twitter2.get("tid"));
        Assert.assertEquals("内容1", twitter2.get("content"));
        Assert.assertEquals("[]", twitter2.get("media"));
        Map translation2 = (Map) respData2.get("translation");
        Assert.assertEquals(1, translation2.get("version"));
        Assert.assertEquals("翻译B2", translation2.get("translation"));
        Assert.assertEquals("[img_22]", translation2.get("img"));
    }

    @Transactional
    @Test
    public void getTasksAfterTid() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("tid", 999);
        List<Object> respData = helper.apiPost("/list", data, List.class);
        Assert.assertEquals(4, respData.size());

        Map r0 = (Map) respData.get(0);
        Map twitter = (Map) r0.get("twitter");
        Assert.assertEquals(1000, twitter.get("tid"));
        Assert.assertEquals("内容0", twitter.get("content"));
        Assert.assertEquals("[]", twitter.get("media"));
        Map translation = (Map) r0.get("translation");
        Assert.assertEquals(2, translation.get("version"));
        Assert.assertEquals("翻译A3", translation.get("translation"));
        Assert.assertEquals("[img3]", translation.get("img"));

        Map r2 = (Map) respData.get(2);
        Map twitter2 = (Map) r2.get("twitter");
        Assert.assertEquals(1002, twitter2.get("tid"));
        Assert.assertEquals("内容2🍒", twitter2.get("content"));
        Assert.assertEquals("[\"media_2\"]", twitter2.get("media"));
        Map translation2 = (Map) r2.get("translation");
        Assert.assertNotNull(translation2);
        Assert.assertEquals("翻译C1", translation2.get("translation"));
        Assert.assertEquals(0, translation2.get("version"));
        Assert.assertEquals("[img_31]", translation2.get("img"));
    }

    @Transactional
    @Test
    public void getLastWithTranslation() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("withTranslation", true);
        Map<String, Object> respData = helper.apiPost("/last", payload, Map.class);
        Map twitter2 = (Map) respData.get("twitter");
        Assert.assertEquals(1003, twitter2.get("tid"));
        Assert.assertEquals("URL_3", twitter2.get("url"));
        Assert.assertEquals("内容3", twitter2.get("content"));
        Assert.assertEquals("[]", twitter2.get("media"));
        Map translation2 = (Map) respData.get("translation");
        Assert.assertNull(translation2);
    }

    @Transactional
    @Test
    public void getLastWithoutTranslation() throws Exception {
        Map<String, Object> twitter2 = helper.apiPost("/last", null, Map.class);
        Assert.assertEquals(1003, twitter2.get("tid"));
        Assert.assertEquals("URL_3", twitter2.get("url"));
        Assert.assertEquals("内容3", twitter2.get("content"));
        Assert.assertEquals("[]", twitter2.get("media"));
    }

    @Transactional
    @Test
    public void getActualLast() throws Exception {
        Map<String, Object> twitter2 = helper.apiPost("/actuallast", null, Map.class);
        Assert.assertEquals(1004, twitter2.get("tid"));
        Assert.assertEquals("URL_4_HIDED", twitter2.get("url"));
        Assert.assertEquals("内容4", twitter2.get("content"));
        Assert.assertEquals("[]", twitter2.get("media"));
    }

    @Transactional
    @Test
    public void addTranslateAndRollback() throws Exception {
        Map<String, Object> twitterLast = helper.apiPost("/last", null, Map.class);
        Integer tid = (Integer) twitterLast.get("tid");
        Assert.assertTrue(tid >= 1000);

        int counter = 0;
        Map<String, Object> payload = new HashMap<>();
        payload.put("tid", tid);
        payload.put("img", "[\"IMG_t\"]");
        String transWord = "翻译内容" + counter;
        payload.put("trans", transWord);
        Map r0 = helper.apiPost("/translate", payload, Map.class);

        Map twitter = (Map) r0.get("twitter");
        Assert.assertEquals(tid, twitter.get("tid"));
        Assert.assertEquals(twitterLast.get("content"), twitter.get("content"));
        Map translation = (Map) r0.get("translation");
        Assert.assertEquals(counter, translation.get("version"));
        Assert.assertEquals(transWord, translation.get("translation"));
        Assert.assertEquals("[\"IMG_t\"]", translation.get("img"));

        counter += 1;

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("tid", tid);
        payload2.put("img", "[\"IMG_t2\"]");
        String transWord2 = "翻译内容" + counter;
        payload2.put("trans", transWord2);
        Map r1 = helper.apiPost("/translate", payload2, Map.class);
        Assert.assertEquals(transWord2, ((Map) r1.get("translation")).get("translation"));

        Map<String, Object> queryData = new HashMap<>();
        queryData.put("tid", tid);
        Map<String, Object> respDataQ = helper.apiPost("/get", queryData, Map.class);
        Assert.assertTrue(respDataQ.containsKey("twitter"));
        Assert.assertTrue(respDataQ.containsKey("translation"));
        Map twitterQ = (Map) respDataQ.get("twitter");
        Assert.assertEquals(tid, twitterQ.get("tid"));
        Map translationQ = (Map) respDataQ.get("translation");
        Assert.assertEquals(counter, translationQ.get("version"));
        Assert.assertEquals(transWord2, translationQ.get("translation"));
        Assert.assertEquals("[\"IMG_t2\"]", translationQ.get("img"));

        Map<String, Object> rbData = new HashMap<>();
        queryData.put("tid", tid);
        Map<String, Object> respDataRB = helper.apiPost("/rollback", queryData, Map.class);
        Map twitterRB = (Map) respDataRB.get("twitter");
        Assert.assertEquals(tid, twitterRB.get("tid"));
        Map translationRB = (Map) respDataRB.get("translation");
        Assert.assertEquals(counter - 1, translationRB.get("version"));
        Assert.assertEquals(transWord, translationRB.get("translation"));
        Assert.assertEquals("[\"IMG_t\"]", translationRB.get("img"));

        Map<String, Object> respDataRB2 = helper.apiPost("/rollback", queryData, Map.class);
        Map twitterRB2 = (Map) respDataRB2.get("twitter");
        Assert.assertEquals(tid, twitterRB2.get("tid"));
        Map translationRB2 = (Map) respDataRB2.get("translation");
        Assert.assertNull(translationRB2);
    }

    @Transactional
    @Test
    public void listAllTranslations() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("tid", 1000);
        Map<String, Object> tl = helper.apiPost("/translations", queryData, Map.class);
        Map twitter = (Map) tl.get("twitter");
        Assert.assertEquals(1000, twitter.get("tid"));
        Assert.assertEquals("URL_0", twitter.get("url"));
        Assert.assertEquals("内容0", twitter.get("content"));
        Assert.assertEquals("[]", twitter.get("media"));
        List translations = (List) tl.get("translations");
        Assert.assertEquals(3, translations.size());
        Assert.assertEquals(2, ((Map) translations.get(0)).get("version"));
        Assert.assertEquals("翻译A2", ((Map) translations.get(1)).get("translation"));
        Assert.assertEquals("[img1]", ((Map) translations.get(2)).get("img"));
    }
}
