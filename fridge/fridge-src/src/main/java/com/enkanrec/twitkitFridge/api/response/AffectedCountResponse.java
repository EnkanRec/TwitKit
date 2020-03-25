/*
 * Author : Rinka
 * Date   : 2020/2/5
 */
package com.enkanrec.twitkitFridge.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Class : AffectedCountResponse
 * Usage :
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AffectedCountResponse extends StandardResponse {

    private static final String KEY_AFFECTED_COUNT = "affected";

    @JsonIgnore
    private int affectedCount;

    public static AffectedCountResponse of(int affected) {
        AffectedCountResponse acr = new AffectedCountResponse();
        acr.affectedCount = affected;
        Map<String, Integer> payload = new HashMap<>();
        payload.put(KEY_AFFECTED_COUNT, affected);
        acr.setData(payload);
        acr.setCode(StandardResponse.CODE_SUCCESS);
        acr.setMessage(StandardResponse.MESSAGE_SUCCESS);
        return acr;
    }
}
