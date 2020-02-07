/*
 * Author : Rinka
 * Date   : 2020/2/3
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class : KVConfigTest
 * Usage :
 */
@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@TestPropertySource("classpath:application-test.properties")
public class KVConfigTest {

    private static final String BASE_URL = "/api/db/kv";

    @Autowired
    private WebApplicationContext ctx;

    private MvcHelper helper;

    @Before
    public void setUp() {
        this.helper = new MvcHelper(BASE_URL, MockMvcBuilders.webAppContextSetup(ctx).build());
    }

    @Transactional
    @Test
    public void getAll() throws Exception {
        Map<String, Object> respData = helper.apiPost("/getall", null, Map.class);
        Assert.assertEquals("iroha", respData.get("test.default.yachiyo.love"));
        Assert.assertEquals("五十铃怜", respData.get("test.default.rika"));
        Assert.assertEquals("❤①+123AB\uF8FF", respData.get("___TEST_MB4___#test.mb4.emoji"));
        Assert.assertNull(respData.get("test.not.exist.one"));
    }

    @Transactional
    @Test
    public void setDefaultAndGet() throws Exception {
        String currentTs = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, String> data = new HashMap<>();
        data.put("test.currentTsStr", currentTs);
        data.put("test.mb4", "❤①+123AB");
        String respData = helper.apiPost("/set", data, String.class);
        Assert.assertEquals("", respData);

        List<String> data2 = new ArrayList<>();
        data2.add("test.currentTsStr");
        data2.add("test.not.exist.SetDefaultAndGet");
        data2.add("test.mb4");
        Map respData2 = helper.apiPost("/get", data2, Map.class);
        Assert.assertEquals(currentTs, respData2.get("test.currentTsStr"));
        Assert.assertNull(respData2.get("test.not.exist.SetDefaultAndGet"));
        Assert.assertEquals("❤①+123AB\uF8FF", respData2.get("test.mb4"));
    }
}
