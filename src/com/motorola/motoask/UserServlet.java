package com.motorola.motoask;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.motorola.motoask.RestRequest;
import com.motorola.motoask.Utils;
import com.motorola.motoask.gcm.RegisterServlet;
import com.motorola.motoask.Datastore;



@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(UserServlet.class.getName());
    
    public static final String PARAMETER_REG_ID = "regId"; // string
    public static final String PARAMETER_USER_ID = "userId";// string
    public static final String PARAMETER_EMAIL = "email";// string
    public static final String PARAMETER_NAME = "userName";// string
    public static final String PARAMETER_AVATAR = "imageUrl";// string
    public static final String PARAMETER_DEVINFO = "deviceInfo"; //JSON string
    public static final String PARAMETER_MOTOEMP = "moto"; //boolean
    public static final String PARAMETER_TOPICS = "topics"; //boolean
    public static final String PARAMETER_NEW_POINTS = "newPoints"; //boolean
    
    //public static final String PARAMETER_DEVINFO = "devInfo"; //JSON string
    
    
    private static JSONArray userStorage = new JSONArray();

    public static  enum Resource {
        users, user, motosme, motosmes
     }
     
     private String REST_RES_PATTERN = "/(users|user|motosmes)$";

     private String REST_RES_WITH_ID_PATTERN = "/(users|user|motosme)/([A-Za-z0-9]*)";

     
     @Override
     public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
             IOException {

         PrintWriter out = res.getWriter();
         JSONObject jsonResp = new JSONObject();
         try {
             // get our REST request object.
             RestRequest resourceValues = new RestRequest(
                     req.getPathInfo(),
                     REST_RES_PATTERN,
                     REST_RES_WITH_ID_PATTERN
                     );

             boolean resIsValid = true;

             switch (Resource.valueOf(resourceValues.getResource())) {

                 case users:
                     jsonResp = this.handlePostToUsers(req, res);
                     break;
                     
                 case motosmes:
                     jsonResp = this.handlePostToMotoSmes(req, res);
                     break;
                     
                 default:
                     resIsValid = false;
                     break;
             }

             if (!resIsValid) {
                 res.setStatus(400);
                 res.resetBuffer();
             }

         } catch (ServletException e) {
             jsonResp.put("success", false);
             jsonResp.put("message", e.toString());
             //res.setStatus(404);
             res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
         }
     
         res.getWriter().write(jsonResp.toString());
         res.flushBuffer();
         return;

     }
     
     @Override
     public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException,
             IOException {

         PrintWriter out = res.getWriter();
         JSONObject jsonResp = new JSONObject();
         try {
             // get our REST request object.
             RestRequest resourceValues = new RestRequest(
                     req.getPathInfo(),
                     REST_RES_PATTERN,
                     REST_RES_WITH_ID_PATTERN
                     );

             boolean resIsValid = true;

             switch (Resource.valueOf(resourceValues.getResource())) {
                // GET users shoud return a list of ALL users
                 case motosme:
                    jsonResp = this.handlePutMotoSme(req, res, resourceValues);
                 
                    break;         
             
                 default:
                     resIsValid = false;
                     break;
             }

             if (!resIsValid) {
                 res.setStatus(400);
                 res.resetBuffer();
             }

         } catch (ServletException e) {
             jsonResp.put("success", false);
             jsonResp.put("message", e.toString());
             //res.setStatus(404);
             res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
         }
     
         res.getWriter().write(jsonResp.toString());
         res.flushBuffer();
         return;

     }
     
     
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
             IOException {

         PrintWriter out = res.getWriter();
         JSONObject jsonResp = new JSONObject();
         try {
             // get our REST request object.
             RestRequest resourceValues = new RestRequest(
                     req.getPathInfo(),
                     REST_RES_PATTERN,
                     REST_RES_WITH_ID_PATTERN
                     );

             boolean resIsValid = true;

             switch (Resource.valueOf(resourceValues.getResource())) {
                // GET users shoud return a list of ALL users
                 case users:
                    jsonResp = this.handleGetUsers(req, res);
                 
                    break;
                 case motosmes:
                     jsonResp = this.handleGetMotoSmes(req, res);
                  
                     break;
                 case user:
                     // This should return a single user's record 
                     jsonResp = this.handleGetUser(req, res, resourceValues);
                        
                     break;                   
             
                 default:
                     resIsValid = false;
                     break;
             }

             if (!resIsValid) {
                 res.setStatus(400);
                 res.resetBuffer();
             }

         } catch (ServletException e) {
             jsonResp.put("success", false);
             jsonResp.put("message", e.toString());
             //res.setStatus(404);
             res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
         }
     
         res.getWriter().write(jsonResp.toString());
         res.flushBuffer();
         return;

     }
     //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com","regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g","imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
     public static JSONObject handlePostToMotoSmes(HttpServletRequest req, HttpServletResponse res) {

         JSONObject jsonResp = new JSONObject();
         JSONObject jsonData = new JSONObject();
         
         JSONObject devObj = null;
         JSONArray topics = null;
         
         String id = null, name = null, email = null, photo = null, devInfoStr = null,
                 topicsStr = null, regId = null;
         try {
             jsonData = Utils.getJsonBody(req);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         userStorage.put(jsonData);
      
//        try {
//            id = jsonData.getString(PARAMETER_USER_ID);
//            
//        } catch (Exception e) {
//
//            log.info("don't have an id");
//        }
      
        try {
            name = jsonData.getString(PARAMETER_NAME);
        } catch (Exception e) {
            log.info("don't have a name");
        }
        
        try {
            email = jsonData.getString(PARAMETER_EMAIL);
            
        } catch (Exception e) {
            log.severe("missing mandatory param email");
            jsonResp.put("success", false);
            jsonResp.put("message", "missing mandatory param email");
           
            return jsonResp;  
        }
        
//        try {
//            photo = jsonData.getString(PARAMETER_AVATAR);
//          
//        } catch (Exception e) {
//            log.info("don't have photo");
//        }
//        try {
//            regId = jsonData.getString(PARAMETER_REG_ID);
//          
//        } catch (Exception e) {
//            log.info("don't have photo");
//        }
//        
        try {
            topicsStr = jsonData.getString(PARAMETER_TOPICS);
            topics = new JSONArray(topicsStr);
          
        } catch (Exception e) {
            log.severe("missing mandatory param topics");
            jsonResp.put("success", false);
            jsonResp.put("message", "missing mandatory param topics");
           
            return jsonResp;  
        }
//        
//        try {
//            devInfoStr = jsonData.getString(PARAMETER_DEVINFO);
//            devObj = new JSONObject(devInfoStr);
//          
//        } catch (Exception e) {
//            log.info("don't have dev info");
//        }
                  
        // now figure out if this is being created by the admin UI for a motorola SME or not...
        //  we know it's a motorolan if there are topics and if there is no userId
         // write this to datastore wiht the non-null params only of course!
        if (id == null) {
            // this would be a motorola user creation...
            Datastore.createMotoSme(name, email, topics);
        } else {
            
            Datastore.createUser(id, name, email, regId, photo, devInfoStr);
        }
        jsonResp.put("success", true);
        jsonResp.put("message", "user added successfully");
       
        return jsonResp;  
     }
     
   
     
    //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com","regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g","imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
    public static JSONObject handlePostToUsers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        JSONObject devObj = null;
        JSONArray topics = null;
        
        String id = null, name = null, email = null, photo = null, devInfoStr = null,
                topicsStr = null, regId = null;
        try {
            jsonData = Utils.getJsonBody(req);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        userStorage.put(jsonData);
     
       try {
           id = jsonData.getString(PARAMETER_USER_ID);
           
       } catch (Exception e) {

           log.info("don't have an id");
       }
     
       try {
           name = jsonData.getString(PARAMETER_NAME);
       } catch (Exception e) {
           log.info("don't have a name");
       }
       try {
           email = jsonData.getString(PARAMETER_EMAIL);
           
       } catch (Exception e) {
           log.info("don't have email");
       }
       
       try {
           photo = jsonData.getString(PARAMETER_AVATAR);
         
       } catch (Exception e) {
           log.info("don't have photo");
       }
       try {
           regId = jsonData.getString(PARAMETER_REG_ID);
           
           com.motorola.motoask.gcm.Datastore.register(id, email, regId);
         
       } catch (Exception e) {
           log.severe("missing mandatory param regId");
           jsonResp.put("success", false);
           jsonResp.put("message", "missing mandatory param regId");        
           return jsonResp;  
           
       }
       
//       try {
//           topicsStr = jsonData.getString(PARAMETER_TOPICS);
//           topics = new JSONArray(topicsStr);
//         
//       } catch (Exception e) {
//           log.info("don't have photo");
//       }
       
       try {
           devInfoStr = jsonData.getString(PARAMETER_DEVINFO);
           devObj = new JSONObject(devInfoStr);
         
       } catch (Exception e) {
           log.info("don't have dev info");
       }
                 
       // now figure out if this is being created by the admin UI for a motorola SME or not...
       //  we know it's a motorolan if there are topics and if there is no userId
        // write this to datastore wiht the non-null params only of course!
     
           
       jsonResp = Datastore.createUser(id, name, email, regId, photo, devInfoStr);
       
       //jsonResp.put("success", true);
       //jsonResp.put("message", "user added successfully");
      
       return jsonResp;  
    }
    
    //  curl  localhost:8888/api/v1/user/12345
    public static JSONObject handleGetUsers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        JSONArray result = Datastore.getUsers(0,0);

        jsonResp.put("success", true);
        jsonResp.put("data", result);
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/motosmes/12345
    public static JSONObject handleGetMotoSmes (HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        JSONArray result = Datastore.getMotoSmes(0,0);

        jsonResp.put("success", true);
        jsonResp.put("data", result);
        return jsonResp;
        
    }
    //  curl  localhost:8888/api/v1/motosme/12345
    public static JSONObject handlePutMotoSme(HttpServletRequest req, HttpServletResponse res,
            RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        String id = resource.getId();
        
        if (id == null) {
            log.severe("missing mandatory param id");
            jsonResp.put("success", false);
            jsonResp.put("message", "missing mandatory param id");   
            return jsonResp;
        }
        String email = null, regId = null, photo = null, newPoints = null;
        
        HashMap<String,String> props = new HashMap<String,String>();
        try {
            jsonData = Utils.getJsonBody(req);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            email = jsonData.getString(PARAMETER_EMAIL);
            props.put(PARAMETER_EMAIL, email);
            
        } catch (Exception e) {
            log.info("don't have email");
        }
        
        try {
            photo = jsonData.getString(PARAMETER_AVATAR);
            props.put(PARAMETER_AVATAR, photo);
          
        } catch (Exception e) {
            log.info("don't have photo");
        }
        
        try {
            newPoints = jsonData.getString(PARAMETER_NEW_POINTS);
            props.put(PARAMETER_NEW_POINTS, newPoints);
          
        } catch (Exception e) {
            log.info("don't have new points");
        }
        
        try {
            regId = jsonData.getString(PARAMETER_REG_ID);
            props.put(PARAMETER_REG_ID, regId);
            com.motorola.motoask.gcm.Datastore.register(id, email, regId);
          
        } catch (Exception e) {
            log.severe("missing mandatory param regId");
            jsonResp.put("success", false);
            jsonResp.put("message", "missing mandatory param regId");        
            return jsonResp;  
            
        }
        jsonResp = Datastore.updateMotoSme(id, props);
        
        // pull the 
        jsonResp.put("success", true);
        jsonResp.put("message", jsonData.toString());
        return jsonResp;
        
    }
    
    
    //  curl  localhost:8888/api/v1/user/12345
    public static JSONObject handleGetUser(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        String userId = resource.getId();
       
        try {
            jsonData = Utils.getJsonBody(req);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // for this one, you have to look up the User enity by the email!! you won't 
        // be able to use the ID the first time cuz the record won't have one
        jsonResp.put("success", true);
        jsonResp.put("message", jsonData.toString());
        return jsonResp;
        
    }
}