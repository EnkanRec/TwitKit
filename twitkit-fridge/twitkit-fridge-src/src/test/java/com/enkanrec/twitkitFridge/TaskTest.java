/*
 * Author : Rinka
 * Date   : 2020/2/7
 */
package com.enkanrec.twitkitFridge;

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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Transactional
    @Test
    public void setPublishedAndUnpublished() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("tid", 1001);
        Map<String, Object> back = helper.apiPost("/published", queryData, Map.class);
        Assert.assertTrue((Boolean) back.get("published"));
        back = helper.apiPost("/unpublished", queryData, Map.class);
        Assert.assertFalse((Boolean) back.get("published"));

        queryData = new HashMap<>();
        queryData.put("tid", 555);
        back = helper.apiPost("/published", queryData, Map.class);
        Assert.assertNull(back);
        back = helper.apiPost("/unpublished", queryData, Map.class);
        Assert.assertNull(back);
    }

    @Transactional
    @Test
    public void setHideAndVisible() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("tid", 1003);
        Map<String, Object> back = helper.apiPost("/hide", queryData, Map.class);
        Assert.assertTrue((Boolean) back.get("hided"));

        Map<String, Object> twitterL = helper.apiPost("/last", null, Map.class);
        Assert.assertEquals(1002, twitterL.get("tid"));
        Assert.assertEquals("URL_2", twitterL.get("url"));
        Assert.assertEquals("内容2🍒", twitterL.get("content"));
        Assert.assertEquals("[\"media_2\"]", twitterL.get("media"));

        back = helper.apiPost("/visible", queryData, Map.class);
        Assert.assertFalse((Boolean) back.get("hided"));

        twitterL = helper.apiPost("/last", null, Map.class);
        Assert.assertEquals(1003, twitterL.get("tid"));
        Assert.assertEquals("URL_3", twitterL.get("url"));

        queryData = new HashMap<>();
        queryData.put("tid", 555);
        back = helper.apiPost("/hide", queryData, Map.class);
        Assert.assertNull(back);
        back = helper.apiPost("/visible", queryData, Map.class);
        Assert.assertNull(back);
    }

    @Transactional
    @Test
    public void updateComment() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        String currentTs = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        queryData.put("tid", 1000);
        queryData.put("comment", currentTs);
        Map<String, Object> back = helper.apiPost("/comment", queryData, Map.class);
        Assert.assertEquals(currentTs, back.get("comment"));

        queryData.put("comment", "");
        back = helper.apiPost("/comment", queryData, Map.class);
        Assert.assertEquals("", back.get("comment"));

        queryData.put("tid", 1);
        back = helper.apiPost("/comment", queryData, Map.class);
        Assert.assertNull(back);
    }

    @Transactional
    @Test
    public void insertOneGetBackRemove() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        String url1 = UUID.randomUUID().toString();
        queryData.put("url", url1);
        queryData.put("content", "入库推文1");
        queryData.put("media", "[\"media_1\"]");
        Map<String, Object> back = helper.apiPost("/create", queryData, Map.class);
        Map<String, Object> twitterL = (Map<String, Object>) back.get("twitter");
        Integer inTid = (Integer) twitterL.get("tid");
        Assert.assertEquals(url1, twitterL.get("url"));
        Assert.assertEquals("入库推文1", twitterL.get("content"));
        Assert.assertEquals("[\"media_1\"]", twitterL.get("media"));

        Map<String, Object> data = new HashMap<>();
        data.put("tid", inTid);
        Map<String, Object> respData = helper.apiPost("/get", data, Map.class);
        Assert.assertTrue(respData.containsKey("twitter"));
        Assert.assertTrue(respData.containsKey("translation"));
        Map twitter = (Map) respData.get("twitter");
        Assert.assertEquals(inTid, twitter.get("tid"));
        Assert.assertEquals("入库推文1", twitter.get("content"));
        Assert.assertEquals("[\"media_1\"]", twitter.get("media"));
        Assert.assertNull(respData.get("translation"));

        Boolean deleteResp = helper.apiPost("/delete", data, Boolean.class);
        Assert.assertTrue(deleteResp);
        deleteResp = helper.apiPost("/delete", data, Boolean.class);
        Assert.assertFalse(deleteResp);

        respData = helper.apiPost("/get", data, Map.class);
        Assert.assertNull(respData);
    }

    @Transactional
    @Test
    public void insertIgnoreOneRemove() throws Exception {
        Map<String, Object> queryData = new HashMap<>();
        String url1 = UUID.randomUUID().toString();
        queryData.put("url", url1);
        queryData.put("content", "入库推文1");
        queryData.put("media", "[\"media_1\"]");
        Map<String, Object> back = helper.apiPost("/create", queryData, Map.class);
        Map<String, Object> twitterL = (Map<String, Object>) back.get("twitter");
        Integer inTid = (Integer) twitterL.get("tid");
        Assert.assertEquals(url1, twitterL.get("url"));
        Assert.assertEquals("入库推文1", twitterL.get("content"));
        Assert.assertEquals("[\"media_1\"]", twitterL.get("media"));
        Assert.assertFalse((Boolean) back.get("alreadyExist"));

        Map<String, Object> backSame = helper.apiPost("/create", queryData, Map.class);
        Map<String, Object> twitterSame = (Map<String, Object>) backSame.get("twitter");
        Assert.assertEquals(inTid, (Integer) twitterSame.get("tid"));
        Assert.assertTrue((Boolean) backSame.get("alreadyExist"));

        Map<String, Object> queryData2 = new HashMap<>();
        queryData2.put("url", url1);
        queryData2.put("content", "入库推文1_update");
        queryData2.put("media", "[\"media_1\"]");
        Map<String, Object> back2 = helper.apiPost("/create", queryData, Map.class);
        Map<String, Object> twitter2 = (Map<String, Object>) back2.get("twitter");
        Assert.assertEquals(inTid, (Integer) twitter2.get("tid"));
        Assert.assertEquals("入库推文1", twitter2.get("content"));
        Assert.assertTrue((Boolean) back2.get("alreadyExist"));

        Map<String, Object> tidData = new HashMap<>();
        tidData.put("tid", inTid);
        Boolean deleteResp = helper.apiPost("/delete", tidData, Boolean.class);
        Assert.assertTrue(deleteResp);
    }

    @Transactional
    @Test
    public void bulkWithAlreadyExist() throws Exception {
        List<Map> bulkData1 = new ArrayList<>();
        Map<String, Object> queryData1 = new HashMap<>();
        String url1 = UUID.randomUUID().toString();
        queryData1.put("url", url1);
        queryData1.put("content", "入库推文1");
        queryData1.put("media", "[\"media_1\"]");
        Map<String, Object> queryData2 = new HashMap<>();
        String url2 = UUID.randomUUID().toString();
        queryData2.put("url", url2);
        queryData2.put("content", "入库推文2");
        queryData2.put("media", "[\"media_2\"]");
        bulkData1.add(queryData1);
        bulkData1.add(queryData2);
        List<Object> back1 = helper.apiPost("/bulk", bulkData1, List.class);

        Assert.assertEquals(2, back1.size());

        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> twitter1Raw = (Map) ((Map) back1.get(0)).get("twitter");
        Integer inTid1 = (Integer) twitter1Raw.get("tid");
        Map<String, Object> twitter2Raw = (Map) ((Map) back1.get(1)).get("twitter");
        Integer inTid2 = (Integer) twitter2Raw.get("tid");

        resultMap.put((String) twitter1Raw.get("url"), twitter1Raw);
        resultMap.put((String) twitter2Raw.get("url"), twitter2Raw);

        Map twitter1 = (Map) resultMap.get(url1);
        Assert.assertEquals(url1, twitter1.get("url"));
        Assert.assertEquals("入库推文1", twitter1.get("content"));
        Assert.assertEquals("[\"media_1\"]", twitter1.get("media"));
        Assert.assertFalse((Boolean) ((Map) back1.get(0)).get("alreadyExist"));

        Map twitter2 = (Map) resultMap.get(url2);
        Assert.assertEquals(url2, twitter2.get("url"));
        Assert.assertEquals("入库推文2", twitter2.get("content"));
        Assert.assertEquals("[\"media_2\"]", twitter2.get("media"));
        Assert.assertFalse((Boolean) ((Map) back1.get(1)).get("alreadyExist"));

        List<Map> bulkData2 = new ArrayList<>();
        bulkData2.add(queryData2);
        Map<String, Object> queryData3 = new HashMap<>();
        String url3 = UUID.randomUUID().toString();
        queryData3.put("url", url3);
        queryData3.put("content", "入库推文3");
        queryData3.put("media", "[\"media_3\"]");
        bulkData2.add(queryData3);
        List<Object> back2 = helper.apiPost("/bulk", bulkData2, List.class);
        Assert.assertEquals(2, back2.size());

        Map<String, Object> twitter22Raw = (Map) ((Map) back2.get(0)).get("twitter");
        Integer inTid22 = (Integer) twitter22Raw.get("tid");
        Map<String, Object> twitter23Raw = (Map) ((Map) back2.get(1)).get("twitter");
        Integer inTid23 = (Integer) twitter23Raw.get("tid");

        Integer alreadyTid;
        Map alreadyTwitter, otherTwitter;
        if (inTid22.equals(inTid1) || inTid22.equals(inTid2)) {
            alreadyTid = inTid22;
            alreadyTwitter = twitter22Raw;
            otherTwitter = twitter23Raw;
            Assert.assertTrue((Boolean) ((Map) back2.get(0)).get("alreadyExist"));
            Assert.assertFalse((Boolean) ((Map) back2.get(1)).get("alreadyExist"));
        } else {
            alreadyTid = inTid23;
            alreadyTwitter = twitter23Raw;
            otherTwitter = twitter22Raw;
            Assert.assertTrue((Boolean) ((Map) back2.get(1)).get("alreadyExist"));
            Assert.assertFalse((Boolean) ((Map) back2.get(0)).get("alreadyExist"));
        }

        resultMap.put((String) twitter22Raw.get("url"), twitter22Raw);
        resultMap.put((String) twitter23Raw.get("url"), twitter23Raw);

        Assert.assertEquals(3, resultMap.size());

        Map twitter22 = alreadyTwitter;
        Assert.assertEquals(alreadyTid, (Integer) twitter22.get("tid"));
        Assert.assertEquals(url2, twitter22.get("url"));

        Map twitter23 = otherTwitter;
        Assert.assertEquals(url3, twitter23.get("url"));
        Assert.assertEquals("入库推文3", twitter23.get("content"));
        Assert.assertEquals("[\"media_3\"]", twitter23.get("media"));

        Map<String, Object> tidData = new HashMap<>();
        for (Map.Entry<String, Object> distinctKvp : resultMap.entrySet()) {
            Map curTwi = (Map) distinctKvp.getValue();
            tidData.put("tid", curTwi.get("tid"));
            Boolean deleteResp = helper.apiPost("/delete", tidData, Boolean.class);
            Assert.assertTrue(deleteResp);
        }
    }
}
