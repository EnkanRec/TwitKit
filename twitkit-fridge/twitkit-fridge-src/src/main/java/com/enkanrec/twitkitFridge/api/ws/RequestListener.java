/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.enkanrec.twitkitFridge.api.form.BaseFridgeForm;
import com.enkanrec.twitkitFridge.api.form.JsonDataFridgeForm;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.api.rest.KVConfigController;
import com.enkanrec.twitkitFridge.api.rest.TaskController;
import com.enkanrec.twitkitFridge.monitor.WebSocketMonitor;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class : RequestListener
 * Usage :
 */
@SuppressWarnings("all")
@Slf4j
@Component
public class RequestListener implements DataListener<String> {

    private static final String LOG_KEY_REQUEST_ID = "requestId";

    @Autowired
    private KVConfigController kvConfigController;
    @Autowired
    private TaskController taskController;

    @Autowired
    private WebSocketMonitor monitor;

    Map<String, Method> methodAccessMap;
    Map<Method, Class> formClassAccessMap;

    @PostConstruct
    void init() {
        Map<String, Method> methodMap = new HashMap<>();
        Map<Method, Class> formClassMap = new HashMap<>();
        // method for request
        Method[] methods = KVConfigController.class.getMethods();
        for (Method method : methods) {
            RequestMapping ano = method.getAnnotation(RequestMapping.class);
            if (ano != null) {
                String[] rm = ano.value();
                for (String rmUri : rm) {
                    methodMap.put("kv&" + rmUri, method);
                }
            }
        }
        methods = TaskController.class.getMethods();
        for (Method method : methods) {
            RequestMapping ano = method.getAnnotation(RequestMapping.class);
            if (ano != null) {
                String[] rm = ano.value();
                for (String rmUri : rm) {
                    methodMap.put("task&" + rmUri.substring(1), method);
                }
            }
        }
        // web form
        for (Method chosenMethod : methodMap.values()) {
            Parameter[] ps = chosenMethod.getParameters();
            for (Parameter p : ps) {
                Class formType = (Class) p.getParameterizedType();
                if (BaseFridgeForm.class.isAssignableFrom(formType)) {
                    formClassMap.put(chosenMethod, formType);
                }
            }
        }
        // unmodify the mapping
        this.formClassAccessMap = Collections.unmodifiableMap(formClassMap);
        this.methodAccessMap = Collections.unmodifiableMap(methodMap);
    }

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        try {
            long beginTs = System.currentTimeMillis();
            Map baseForm = JsonUtil.parse(data, Map.class);
            String useController = (String) baseForm.get("of");
            String useMethod = (String) baseForm.get("command");
            String requestId = UUID.randomUUID().toString();
            MDC.put(LOG_KEY_REQUEST_ID, requestId);
            log.info(String.format("Client request[RequestID:%s SessionID:%s]: Controller:%s UriMethod:%s", requestId, client.getSessionId(), useController, useMethod));
            Object chosenController;
            switch (useController) {
                case "kv":
                    chosenController = this.kvConfigController;
                    break;
                case "task":
                    chosenController = this.taskController;
                    break;
                default:
                    String hint = "unsupported controller in `of`: " + useController;
                    log.warn(hint);
                    client.sendEvent(FridgeWSServer.RESPONSE_EVT, StandardResponse.exception(hint));
                    return;
            }
            String methodKey = useController + "&" + useMethod;
            Method chosenMethod = this.methodAccessMap.get(methodKey);
            if (chosenMethod != null) {
                Class chosenForm = this.formClassAccessMap.get(chosenMethod);
                Object formIns = chosenForm.newInstance();
                Method parseMethod = JsonDataFridgeForm.class.getMethod("fromRawString", String.class);
                parseMethod.invoke(formIns, data);
                try {
                    Object resp = chosenMethod.invoke(chosenController, formIns);
                    String handlerName = chosenController.getClass().getSimpleName() + "." + chosenMethod.getName();
                    log.info(String.format("ws request handled [%s], prepare to emit.", handlerName));
                    client.sendEvent(FridgeWSServer.RESPONSE_EVT, resp);
                    String code = String.valueOf(((StandardResponse) resp).getCode());
                    this.monitor.responseTimeInMs.labels(handlerName, "/" + useMethod, code)
                            .observe(System.currentTimeMillis() - beginTs);
                } catch (Exception invEx) {
                    String hint = "exception at invoke: " + invEx.getMessage();
                    log.warn(hint);
                    client.sendEvent(FridgeWSServer.RESPONSE_EVT, StandardResponse.exception(hint));
                    this.monitor.exceptionCounter.inc();
                }
            } else {
                String hint = "unsupported method: " + methodKey;
                log.warn(hint);
                client.sendEvent(FridgeWSServer.RESPONSE_EVT, StandardResponse.exception(hint));
                return;
            }
        }
        finally {
            MDC.remove(LOG_KEY_REQUEST_ID);
        }
    }
}
