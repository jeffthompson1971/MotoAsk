package com.motorola.motoask.gcm;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gson.Gson;
import com.motorola.motoask.gcm.server.Message;
import com.motorola.motoask.Constants;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class Utils {

    protected static final Logger logger = Logger.getLogger(Utils.class.getName());
    
   
    public static void enqueuePush (Message msgObj, List<String> devices) {
        
//        if (tbdId != null && tbdId.length() != 0) {
//    }
//
//        log.info("request to push to id: " + tbdId);
//        
//        devices = Datastore.getDevicesByOrkpId(tbdId);
//
//    } else if (creds != null && creds.length() != 0)  {
//        
//        devices = Datastore.getDevices();
//    } else
//        
//        devices = Datastore.getDevices();
    
    String status;
     
    // convert our serializable Message object to json string for transfer
    Gson gson = new Gson();
    String jsonMsg = gson.toJson(msgObj); 
    logger.info("serialized Message object to " + jsonMsg);
    
    if (devices.isEmpty()) {
      status = "Message ignored as there is no device registered!";
    } else {
      Queue queue = QueueFactory.getQueue("gcm");
      // NOTE: check below is for demonstration purposes; a real application
      // could always send a multi-cast, even for just one recipient
      if (devices.size() == 1) {
        // send a single message using plain post
        String device = devices.get(0);
        queue.add(withUrl("/send")
                .param(SendMessageServlet.PARAMETER_DEVICE, device)
                .param(SendMessageServlet.PARAMETER_MESSAGE_OBJ, jsonMsg));
        status = "Single message queued for registration id " + device;
        
      } else {
        // send a multicast message using JSON
        // must split in chunks of 1000 devices (GCM limit)
        int total = devices.size();
        List<String> partialDevices = new ArrayList<String>(total);
        int counter = 0;
        int tasks = 0;
        for (String device : devices) {
            
          logger.info(": " + device);
          counter++;
          partialDevices.add(device);
          logger.info("device: " + device);
          int partialSize = partialDevices.size();
          if (partialSize == Datastore.MULTICAST_SIZE || counter == total) {
            String multicastKey = Datastore.createMulticast(partialDevices);
            logger.info("Queuing " + partialSize + " devices on multicast man " +
                multicastKey);
            TaskOptions taskOptions = TaskOptions.Builder
                .withUrl("/send")
                .param(SendMessageServlet.PARAMETER_MULTICAST, multicastKey)
                .param(SendMessageServlet.PARAMETER_MESSAGE_OBJ, jsonMsg)
                .method(Method.POST);
            queue.add(taskOptions);
            partialDevices.clear();
            tasks++;
            
          } else {
              
          }
        }
        //status = "Queued tasks to send " + tasks + " multicast messages to " +
       //     total + " devices";
      }
    }
    }
    //req.setAttribute(HomeServlet.ATTRIBUTE_STATUS, status.toString());
    public static Message createMessage(JSONObject msgJson) {
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

    public static Message createMessage(String msgString) {
        // Message message = new Message.Builder().build();
        Message message = new Message.Builder()
                // TODO - use the collpase key
                // .collapseKey(collapseKey)
                .timeToLive(3).delayWhileIdle(true).dryRun(false).addData("message", msgString)
                .build();

        logger.log(Level.INFO, "Sending message :" + message.toString());
        return message;
    }

    public static Message createMessage(String title, String theMessage, JSONObject data) {
        // Message message = new Message.Builder().build();
        Message.Builder mb = new Message.Builder()

                // TODO - use the collpase key to avoid spamming...
                // .collapseKey(collapseKey)
                .timeToLive(3).delayWhileIdle(true).dryRun(false).addData("title", title)
                .addData("message", theMessage);

        if (data != null)
            mb.addData("data", data.toString());

        logger.log(Level.INFO, "Sending message :" + mb.toString());
        return mb.build();
    }
    
   
    
}
