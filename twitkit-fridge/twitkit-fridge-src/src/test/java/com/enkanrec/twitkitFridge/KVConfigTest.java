/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge;

import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Class : KVConfigTest
 * Usage :
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@TestPropertySource("classpath:application-test.properties")
public class KVConfigTest {

    private static final String BASE_URL = "/api/db/kv";

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    public void GetAll() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.post(BASE_URL + "/getall")
                .param("forwardFrom", "tester")
                .param("timestamp", "2020-01-29T14:40:00.000+08:00");
        MvcResult future = this.mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        MockHttpServletResponse result = future.getResponse();
        String cnt = result.getContentAsString(StandardCharsets.UTF_8);
        StandardResponse resp = JsonUtil.Mapper.readValue(cnt, new TypeReference<StandardResponse>() {});
        Assert.assertNotNull(resp);
        Assert.assertEquals(StandardResponse.CODE_SUCCESS, resp.getCode());
        Assert.assertEquals(StandardResponse.MESSAGE_SUCCESS, resp.getMessage());
        Assert.assertNotNull(resp.getData());
        Map<String, Object> respData = (Map<String, Object>) resp.getData();
        Assert.assertEquals("already", respData.get("test.existed"));
        Assert.assertEquals("中文＋Emoji❤", respData.get("test.existed.2"));
        Assert.assertNull(respData.get("test.not.exist.one"));
    }
}
