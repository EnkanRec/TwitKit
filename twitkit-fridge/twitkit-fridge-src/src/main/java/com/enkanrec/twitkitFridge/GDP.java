/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Class : GDP
 * Usage : Global data package
 */
@Component
@Order(0)
public class GDP {

    @Autowired
    private ApplicationArguments appArguments;

    public static boolean EnableWebSocket = false;

    @PostConstruct
    private void init() {
        Set<String> rawTags = this.appArguments.getOptionNames();
        if (rawTags != null) {
            rawTags.forEach(tag -> {
                switch (tag) {
                    case "enable-websocket":
                        GDP.EnableWebSocket = true;
                        break;
                }
            });
        }
    }
}
