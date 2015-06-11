/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.motorola.motoask.gcm;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.motorola.motoask.gcm.server.Constants;
import com.motorola.motoask.gcm.server.Message;
import com.motorola.motoask.gcm.server.MulticastResult;
import com.motorola.motoask.gcm.server.Result;
import com.motorola.motoask.gcm.server.Sender;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Servlet that sends a message to a device.
 * <p>
 * This servlet is invoked by AppEngine's Push Queue mechanism.
 */
@SuppressWarnings("serial")
public class SendMessageServlet extends BaseServlet {

    private static final String HEADER_QUEUE_COUNT = "X-AppEngine-TaskRetryCount";
    private static final String HEADER_QUEUE_NAME = "X-AppEngine-QueueName";
    private static final int MAX_RETRY = 3;

    static final String PARAMETER_DEVICE = "device";
    static final String PARAMETER_MESSAGE = "message";
    static final String PARAMETER_MESSAGE_OBJ = "message_obj";
    static final String PARAMETER_MULTICAST = "multicastKey";

    private Sender sender;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        sender = newSender(config);
    }

    /**
     * Creates the {@link Sender} based on the servlet settings.
     */
    protected Sender newSender(ServletConfig config) {
        String key = (String) config.getServletContext().getAttribute(
                ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
        return new Sender(key);
    }

    /**
     * Indicates to App Engine that this task should be retried.
     */
    private void retryTask(HttpServletResponse resp) {
        resp.setStatus(500);
    }

    /**
     * Indicates to App Engine that this task is done.
     */
    private void taskDone(HttpServletResponse resp) {
        resp.setStatus(200);
    }

    class MessageInstanceCreator implements InstanceCreator<Message> {
        // public Message createInstance(Message type) {
        // return new Message(Object.class);
        // }

        @Override
        public Message createInstance(Type arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * Processes the request to add a new message.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getHeader(HEADER_QUEUE_NAME) == null) {
            throw new IOException("Missing header " + HEADER_QUEUE_NAME);
        }
        String retryCountHeader = req.getHeader(HEADER_QUEUE_COUNT);
        logger.fine("retry count: " + retryCountHeader);
        if (retryCountHeader != null) {
            int retryCount = Integer.parseInt(retryCountHeader);
            if (retryCount > MAX_RETRY) {
                logger.severe("Too many retries, dropping task");
                taskDone(resp);
                return;
            }
        }
        // String message = req.getParameter(PARAMETER_MESSAGE);

        String messageObjStr = req.getParameter(PARAMETER_MESSAGE_OBJ);

        String regId = req.getParameter(PARAMETER_DEVICE);
        if (regId != null && messageObjStr != null) {
            handleSingleMessageRequest(regId, messageObjStr, resp);
            return;
        }
        String multicastKey = req.getParameter(PARAMETER_MULTICAST);
        if (multicastKey != null) {
            sendMulticastMessage(multicastKey, messageObjStr, resp);
            return;
        }
        logger.severe("Invalid request!");
        taskDone(resp);
        return;
    }

    private Message createMessage() {
        // Message message = new Message.Builder().build();
        Message message = new Message.Builder()
                // .collapseKey(collapseKey)
                .timeToLive(3).delayWhileIdle(true).dryRun(false)
                // .restrictedPackageName(restrictedPackageName)

                .addData("message", "Jeffrey is the BOSS BIAAATCH!")
                
                .addData("title", "TEXT FOR YOU")

                .build();

        return message;
    }

    private Message createMessage(JSONObject msgJson) {
        // Message message = new Message.Builder().build();
        String colKey, rpn;
        int ttl;
        boolean dwi, dr;
        Message.Builder msgBldr = new Message.Builder();
   
        try {
            rpn = msgJson.getString("restrictedPackageName");
            msgBldr.restrictedPackageName(rpn);
        } catch (JSONException e) {

        }
        try {
            colKey = msgJson.getString("collapseKey");
            msgBldr.collapseKey(colKey);
        } catch (JSONException e) {

        }
        try {
            ttl = msgJson.getInt("timeToLive");
            msgBldr.timeToLive(ttl);
        } catch (JSONException e) {

        }
        try {
            dwi = msgJson.getBoolean("delayWhileIdle");
            msgBldr.delayWhileIdle(dwi);
        } catch (JSONException e) {

        }
        try {
            dr = msgJson.getBoolean("dryRun");
            msgBldr.delayWhileIdle(dr);
        } catch (JSONException e) {

        }
        try {
            JSONObject data = msgJson.getJSONObject("data");
            for (int i = 0; i < data.names().length(); i++) {
                String name = data.names().getString(i);
                // data field is an ojbect not string but we'll cast to a string
                if (name.equals("data")) {
                    JSONObject payload = data.getJSONObject(name);
                    msgBldr.addData("data", payload.toString());
                } else
                    msgBldr.addData(name, data.getString(name));

                logger.info("data key name: " + name);
            }

        } catch (JSONException e) {

        }

        Message message = msgBldr.build();
        // .collapseKey(collapseKey)
        // .timeToLive(msgJson.getInt()).delayWhileIdle(true).dryRun(false)
        // .restrictedPackageName(restrictedPackageName)

        // .addData("message", "Jeffrey is the BOSS BIAAATCH!").build();
        logger.log(Level.INFO, "Created message :" + message.toString());
        return message;
    }

    private Message createMessage(String theMessage) {
        // Message message = new Message.Builder().build();
        Message message = new Message.Builder()
        // TODO - use the collapse key
        // .collapseKey(collapseKey)
                .timeToLive(3).delayWhileIdle(true).dryRun(false)
                // .restrictedPackageName(restrictedPackageName)

                .addData("message", theMessage).build();
        logger.log(Level.INFO, "Sending message :" + message.toString());
        return message;
    }

    private void handleSingleMessageRequest(String regId, String msgObjStr, HttpServletResponse resp) {
        logger.info("Sending message to device " + regId);
        Message message;

        if (msgObjStr != null && msgObjStr.length() > 0) {
     
            JSONObject jsonMsg = new JSONObject(msgObjStr);
            
            message = Utils.createMessage(jsonMsg);

        } else {

            return;
        }

        logger.info("Sending message %s " + message.toString());
        Result result;
        try {
            result = sender.sendNoRetry(message, regId);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception posting " + message, e);
            taskDone(resp);
            return;
        }
        if (result == null) {
            retryTask(resp);
            return;
        }
        if (result.getMessageId() != null) {
            logger.info("Succesfully sent message to device " + regId);
            String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {
                // same device has more than on registration id: update it
                logger.finest("canonicalRegId " + canonicalRegId);
                Datastore.updateRegistration(regId, canonicalRegId);
            }
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                // application has been removed from device - unregister it
                Datastore.unregister(regId);
            } else {
                logger.severe("Error sending message to device " + regId + ": " + error);
            }
        }
    }

    public void sendSingleMessage(String regId, String msgObjStr) {
        logger.info("Sending message to device " + regId);

        Message message;

        if (msgObjStr != null && msgObjStr.length() > 0) {

            Gson gson = new Gson();
            message = gson.fromJson(msgObjStr, Message.class);

        } else {

            return;
        }

        logger.info("Sending message %s " + message.toString());
        Result result;
        try {
            result = sender.sendNoRetry(message, regId);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception posting " + message, e);
            // taskDone(resp);
            return;
        }
        if (result == null) {
            // retryTask(resp);
            return;
        }
        if (result.getMessageId() != null) {
            logger.info("Succesfully sent message to device " + regId);
            String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {
                // same device has more than on registration id: update it
                logger.finest("canonicalRegId " + canonicalRegId);
                Datastore.updateRegistration(regId, canonicalRegId);
            }
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                // application has been removed from device - unregister it
                Datastore.unregister(regId);
            } else {
                logger.severe("Error sending message to device " + regId + ": " + error);
            }
        }
    }

    private void sendMulticastMessage(String multicastKey, String msgJson,
            HttpServletResponse resp) {
        // Recover registration ids from datastore
        List<String> regIds = Datastore.getMulticast(multicastKey);

        logger.info("regIds " + regIds.toString());
        logger.info("msgObj " + msgJson);

        Message message;

        if (msgJson != null && msgJson.length() > 0) {

            JSONObject temp = new JSONObject(msgJson);
            logger.info("temp " + temp.toString());
            message = createMessage(temp);

        } else {

            return;
        }

        // String uid = UUID.randomUUID().toString();
        //
        // JSONObject payload = new JSONObject();
        // //payload.put("origin", "showingtime");
        //
        // payload.put("data", theMessage);
        // payload.put("type", "admin");
        // payload.put("uuid", UUID.randomUUID().toString());
        // Message message =
        // com.threebd.apps.gcm.Utils.createMessage(theMessage,
        // theMessage, payload);
        // logger.info("Sending multicast message: " + message.toString());

        MulticastResult multicastResult;
        try {
            multicastResult = sender.sendNoRetry(message, regIds);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception posting " + message, e);
            multicastDone(resp, multicastKey);
            return;
        }
        boolean allDone = true;
        // check if any registration id must be updated
        if (multicastResult.getCanonicalIds() != 0) {
            List<Result> results = multicastResult.getResults();
            for (int i = 0; i < results.size(); i++) {
                String canonicalRegId = results.get(i).getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    String regId = regIds.get(i);
                    Datastore.updateRegistration(regId, canonicalRegId);
                }
            }
        }
        if (multicastResult.getFailure() != 0) {
            logger.severe("multicast failure");
            // there were failures, check if any could be retried
            List<Result> results = multicastResult.getResults();
            List<String> retriableRegIds = new ArrayList<String>();
            for (int i = 0; i < results.size(); i++) {
                String error = results.get(i).getErrorCodeName();
                if (error != null) {
                    String regId = regIds.get(i);
                    logger.warning("Got error (" + error + ") for regId " + regId);
                    if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        // application has been removed from device - unregister
                        // it
                        Datastore.unregister(regId);
                    }
                    if (error.equals(Constants.ERROR_UNAVAILABLE)) {
                        retriableRegIds.add(regId);
                    }
                }
            }
            if (!retriableRegIds.isEmpty()) {
                // update task
                Datastore.updateMulticast(multicastKey, retriableRegIds);
                allDone = false;
                retryTask(resp);
            }
        }
        if (allDone) {
            multicastDone(resp, multicastKey);
        } else {
            retryTask(resp);
        }
    }

    private void multicastDone(HttpServletResponse resp, String encodedKey) {
        Datastore.deleteMulticast(encodedKey);
        taskDone(resp);
    }

}
