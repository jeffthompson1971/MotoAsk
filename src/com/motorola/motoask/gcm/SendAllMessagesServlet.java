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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.motorola.motoask.gcm.server.Message;
//import com.threebd.apps.servlets.ThreeBDServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet that adds a new message to all registered devices.
 * <p>
 * This servlet is used just by the browser (i.e., not device).
 */
@SuppressWarnings("serial")
public class SendAllMessagesServlet extends BaseServlet {
    private static final Logger log = Logger.getLogger(SendAllMessagesServlet.class.getName());

    /**
     * Processes the request to add a new message.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            ServletException {

        log.info("POST to /push some shit my brother");

        // 
        Enumeration<String> parameterNames = req.getParameterNames();

        while (parameterNames.hasMoreElements()) {

            String paramName = parameterNames.nextElement();
            //out.write(paramName);
           // out.write("n");

            String[] paramValues = req.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                String paramValue = paramValues[i];
                log.info("param: " + paramValue);
                //out.write("t" + paramValue);
                //out.write("n");
            }

        }
        
        
        List<String> devices;
        
        Message msgObj;

        // we're not sure which of the following params the request is using.
        String tbdId = req.getParameter("tbdId");
 
        String creds = req.getParameter("c");

        // message is a JSON string with enough data to build a message
        String message = req.getParameter("message");
        
        String messageObj = req.getParameter("message_obj");
        
        if (tbdId != null && tbdId.length() != 0) {

            log.info("request to push to id: " + tbdId);

            devices = Datastore.getDevicesByTbdId(tbdId);

        } else if (creds != null && creds.length() != 0) {

            devices = Datastore.getDevicesByCreds(creds);

        } else
            devices = Datastore.getDevices();
          
        if (messageObj != null && message.length() > 0) {
            log.info("Request to push a message OBJECT");
           
            JSONObject jsonMsg = new JSONObject(messageObj);
          
            msgObj = Utils.createMessage(jsonMsg);
            
            Utils.enqueuePush(msgObj, devices);
        
        } else if (message != null && message.length() > 0) {
            
            // this is just an admin message - so create a message object 
            msgObj = Utils.createMessage(message);
            
            String uid = UUID.randomUUID().toString();

            JSONObject payload = new JSONObject();
            //payload.put("origin", "showingtime");

            // payload.put("data", theMessage);
            payload.put("type", "admin");
            payload.put("uuid",  UUID.randomUUID().toString());
            msgObj = Utils.createMessage("3BD Update", message, payload);
            
            logger.info("Sending multicast message: " + msgObj.toString());
            
           Utils.enqueuePush(msgObj, devices);
             
        } else {
            
           log.severe("no message text or object to push...bailing!"); 
        }
       
      
        /*
         * String status;
         * 
         * if (devices.isEmpty()) { status =
         * "Message ignored as there is no device registered!"; } else { Queue
         * queue = QueueFactory.getQueue("gcm"); // NOTE: check below is for
         * demonstration purposes; a real application // could always send a
         * multicast, even for just one recipient if (devices.size() == 1) { //
         * send a single message using plain post String device =
         * devices.get(0); queue.add(withUrl("/send")
         * .param(SendMessageServlet.PARAMETER_DEVICE, device)
         * .param(SendMessageServlet.PARAMETER_MESSAGE, message)); status =
         * "Single message queued for registration id " + device; } else { //
         * send a multicast message using JSON // must split in chunks of 1000
         * devices (GCM limit) int total = devices.size(); List<String>
         * partialDevices = new ArrayList<String>(total); int counter = 0; int
         * tasks = 0; for (String device : devices) {
         * 
         * logger.info(": " + device); counter++; partialDevices.add(device);
         * logger.info("device: " + device); int partialSize =
         * partialDevices.size(); if (partialSize == Datastore.MULTICAST_SIZE ||
         * counter == total) { String multicastKey =
         * Datastore.createMulticast(partialDevices); logger.info("Queuing " +
         * partialSize + " devices on multicast man " + multicastKey);
         * TaskOptions taskOptions = TaskOptions.Builder .withUrl("/send")
         * .param(SendMessageServlet.PARAMETER_MULTICAST, multicastKey)
         * .param(SendMessageServlet.PARAMETER_MESSAGE, message)
         * .method(Method.POST); queue.add(taskOptions); partialDevices.clear();
         * tasks++;
         * 
         * } else {
         * 
         * } } status = "Queued tasks to send " + tasks +
         * " multicast messages to " + total + " devices"; } }
         * 
         * req.setAttribute(HomeServlet.ATTRIBUTE_STATUS, status.toString()); //
         * getServletContext().getRequestDispatcher("/home").forward(req, resp);
         */
    }

}
