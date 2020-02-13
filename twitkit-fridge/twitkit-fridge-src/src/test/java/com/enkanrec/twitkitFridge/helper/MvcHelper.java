/*
 * Author : Rinka
 * Date   : 2020/2/7
 */
package com.enkanrec.twitkitFridge.helper;

import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class : MvcHelper
 * Usage :
 */
public class MvcHelper {

    private String baseUrl;
    private MockMvc mvc;

    public MvcHelper(String baseUrl, MockMvc mvc) {
        this.baseUrl = baseUrl;
        this.mvc = mvc;
    }

    public <T> T apiPost(String uri, Object postData, Class<T> returnHint) throws Exception {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("forwardFrom", "tester");
        jsonData.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (postData != null) {
            jsonData.put("data", postData);
        }
        String jString = JsonUtil.dumps(jsonData);
        RequestBuilder request = MockMvcRequestBuilders.post(this.baseUrl + uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jString);
        MvcResult future = this.mvc
                .perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        MockHttpServletResponse result = future.getResponse();
        String cnt = result.getContentAsString(StandardCharsets.UTF_8);
        StandardResponse resp = JsonUtil.parseRaw(cnt, new TypeReference<StandardResponse>() {});
        Assert.assertNotNull(resp);
        Assert.assertEquals(StandardResponse.CODE_SUCCESS, resp.getCode());
        Assert.assertEquals(StandardResponse.MESSAGE_SUCCESS, resp.getMessage());
        return (T) resp.getData();
    }
}
