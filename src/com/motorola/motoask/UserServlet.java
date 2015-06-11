package com.motorola.motoask;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONObject;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.motorola.motoask.RestRequest;
import com.motorola.motoask.Utils;
import com.motorola.motoask.gcm.RegisterServlet;
import com.motorola.motoask.OfyService;



@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(UserServlet.class.getName());
    
    public static final String PARAMETER_REG_ID = "regId"; // string
    public static final String PARAMETER_USER_ID = "userId";// string
    public static final String PARAMETER_EMAIL = "email";// string
    public static final String PARAMETER_NAME = "userName";// string
    public static final String PARAMETER_AVATAR = "imageUrl";// string
    public static final String PARAMETER_DEVINFO = "deviceInfo"; //JSON string
    //public static final String PARAMETER_DEVINFO = "devInfo"; //JSON string
    
    //public final static OfyService ofyService;
    
 

    public static  enum Resource {
        users, user
     }
     
     private String REST_RES_PATTERN = "/(users)$";

     private String REST_RES_WITH_ID_PATTERN = "/(users|user)/([A-Za-z0-9]*)";

     
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
                    jsonResp = this.handlePostToUsers(req, res);
                 
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
   
   
     //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com", "regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g", "imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
    //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com",
    //  "regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g",
    //  "imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
    public static JSONObject handlePostToUsers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        
   
        try {
            jsonData = Utils.getJsonBody(req);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
        String regId = jsonData.getString(PARAMETER_REG_ID);
        String userId = jsonData.getString(PARAMETER_USER_ID);
        String name = jsonData.getString(PARAMETER_NAME);
        String email = jsonData.getString(PARAMETER_EMAIL);
        String photo = jsonData.getString(PARAMETER_AVATAR);
        String devInfo = jsonData.getString(PARAMETER_DEVINFO);
        
        
        JSONObject devObj = new JSONObject(devInfo);
        
        UserDataEntity userData =
        new UserDataEntity()
          .setRegId(regId)
          .setUserId(userId)
          .setName(name)
          .setEmail(email)
          .setImageUrl(photo)
          .setDeviceInfo(devInfo);

        OfyService ofyService = OfyService.getInstance();
        ofyService.save(userData);
        // write this to datastore!
        
        
        jsonResp.put("success", true);
        jsonResp.put("message", "user added successfully");
        jsonResp.put("devInfo", devObj.toString());
        
        
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/user/12345
    public static JSONObject handleGetUser(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        String userId = resource.getId();
        
        jsonResp.put("success", true);
        jsonResp.put("message", "shoudl be returning data for user:" + userId);
        return jsonResp;
        
    }
}