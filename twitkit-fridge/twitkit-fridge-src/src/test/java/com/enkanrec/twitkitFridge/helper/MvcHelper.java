/*
 * Author : Rinka
 * Date   : 2020/2/7
 */
package com.enkanrec.twitkitFridge.helper;

import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Assert;
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

    public <T> T apiPost(String uri, Object data, Class<T> returnHint) throws Exception {
        String postData;
        if (data != null) {
            if (data instanceof Map || data instanceof List) {
                postData = JsonUtil.dumps(data);
            } else {
                postData = data.toString();
            }
        } else {
            postData = null;
        }
        RequestBuilder request = MockMvcRequestBuilders.post(this.baseUrl + uri)
                .param("forwardFrom", "tester")
                .param("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (data != null) {
            ((MockHttpServletRequestBuilder) request).param("data", postData);
        }
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
