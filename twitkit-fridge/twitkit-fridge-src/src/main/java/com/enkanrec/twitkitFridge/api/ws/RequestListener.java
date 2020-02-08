/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.enkanrec.twitkitFridge.api.form.BaseFridgeForm;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.api.rest.KVConfigController;
import com.enkanrec.twitkitFridge.api.rest.TaskController;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Class : RequestListener
 * Usage :
 */
@Slf4j
@Component
public class RequestListener implements DataListener<String> {

    @Autowired
    private KVConfigController kvConfigController;
    @Autowired
    private TaskController taskController;

    // TODO
    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        Map baseForm = JsonUtil.parse(data, Map.class);
        String useController = (String) baseForm.get("of");
        String useMethod = (String) baseForm.get("command");
        log.info(String.format("Client request[%s]: %s %s", client.getSessionId(), useController, useMethod));
        Class controllerClazz;
        switch (useController) {
            case "kv":
                controllerClazz = KVConfigController.class;
                break;
            case "task":
                controllerClazz = TaskController.class;
                break;
            default:
                String hint = "unsupported controller in `of`: " + useController;
                log.warn(hint);
                client.sendEvent(FridgeWSServer.RESPONSE_EVT, StandardResponse.exception(hint));
                return;
        }
        String normalizedUseMethod = "/" + useMethod;
        Method[] methods = controllerClazz.getMethods();
        Method chosenMethod = null;
        for (Method method : methods) {
            RequestMapping ano = method.getAnnotation(RequestMapping.class);
            if (ano != null) {
                String[] rm = ano.value();
                for (String rmUri : rm) {
                    if (rmUri.equals(normalizedUseMethod)) {
                        chosenMethod = method;
                        break;
                    }
                }
                if (chosenMethod != null) {
                    break;
                }
            } else {
                System.out.println(" >>> " + method.toString());
            }
        }
        Object chosenController = null;
        if (controllerClazz.equals(TaskController.class)) {
            chosenController = this.taskController;
        } else if (controllerClazz.equals(KVConfigController.class)) {
            chosenController = this.kvConfigController;
        }

        Parameter[] ps = chosenMethod.getParameters();
        Class chosenForm = null;
        for (Parameter p : ps) {
            Class formType = (Class) p.getParameterizedType();
            if (BaseFridgeForm.class.isAssignableFrom(formType)) {
                chosenForm = formType;
                break;
            }
        }
        if (chosenForm != null) {
            Object form = JsonUtil.Mapper.readValue(data, chosenForm);
            Object resp = chosenMethod.invoke(chosenController, form);
            log.error(resp.toString());
        } else {
            log.error("cannot map any form type");
            return;
        }
    }
}
