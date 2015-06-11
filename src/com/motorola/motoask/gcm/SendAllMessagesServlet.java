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
        String userId = req.getParameter("userId");
 
       
        // message is a JSON string with enough data to build a message
        String message = req.getParameter("message");
        
        String messageObj = req.getParameter("message_obj");
        
        if (userId != null && userId.length() != 0) {

            log.info("request to push to id: " + userId);

            devices = Datastore.getDevicesByUserId(userId);

        }  else
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
       
      
    }

}
