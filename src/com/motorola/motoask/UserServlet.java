package com.motorola.motoask;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONObject;

import com.motorola.motoask.RestRequest;
import com.motorola.motoask.Utils;



@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(UserServlet.class.getName());
    
    public static  enum Resource {
        users, user
     }
     
     private String REST_RES_PATTERN = "/(users|user)$";

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
   
   
    // curl -X POST -d '{}' localhost:8888/api/v1/users
    public static JSONObject handlePostToUsers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        try {
            jsonData = Utils.getJsonBody(req);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //String userId = restReq.getId()
        
        jsonResp.put("success", true);
        jsonResp.put("message", "POST message body:" + jsonData.toString());
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/user/12345
    public static JSONObject handleGetUser(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        String userId = resource.getId();
      
        //String userId = restReq.getId()
        
        jsonResp.put("success", true);
        jsonResp.put("message", "shoudl be returning data for user:" + userId);
        return jsonResp;
        
    }
}