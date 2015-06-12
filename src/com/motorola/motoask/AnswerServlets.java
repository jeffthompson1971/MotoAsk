package com.motorola.motoask;



import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.gson.Gson;
import com.motorola.motoask.RestRequest;
import com.motorola.motoask.Utils;
import com.motorola.motoask.gcm.RegisterServlet;




@SuppressWarnings("serial")
public class AnswerServlets extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(QuestionServlet.class.getName());
    
    public static final String PARAMETER_AID = "aId"; // string
    public static final String PARAMETER_QID = "qId"; // string
    public static final String PARAMETER_USER_ID = "userId";// string
    public static final String PARAMETER_USER_EMAIL = "userEmail";// string
    public static final String PARAMETER_A = "a"; // string
   
    //public static final String PARAMETER_DEVINFO = "devInfo"; //JSON string
    
    
 

    public static  enum Resource {
        answer, answers
     }
     
     private String REST_RES_PATTERN = "/(answers|answer)$";

     private String REST_RES_WITH_ID_PATTERN = "/(answer)/([A-Za-z0-9]*)";

     
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

                 case answers:
                     jsonResp = AnswerServlets.handlePostToAnswers(req, res);
                     break;
                 case answer:
                     jsonResp = AnswerServlets.handlePostEditToAnswers(req, res, resourceValues);
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
         } catch (Exception ex) {
             jsonResp.put("success", false);
             jsonResp.put("message", ex.toString());
        	 
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
                 case answers:
                    jsonResp = AnswerServlets.handleGetAnswers(req, res);
                    break;
                 case answer:
                    jsonResp = AnswerServlets.handleGetAnswer(req, res, resourceValues);
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
         } catch (Exception ex) {
             jsonResp.put("success", false);
             jsonResp.put("message", ex.toString());
        	 
         }
     
         res.getWriter().write(jsonResp.toString());
         res.flushBuffer();
         return;

     }
     private static void notifyUserOfAnswer (Long qId, String q, String userId, String title ) {
         
         JSONObject payload = new JSONObject();
         payload.put("userId", userId);
         payload.put("questionId", qId);
         payload.put("state", 0);
        
         JSONArray topics = new JSONArray();
         topics.put("accessories");
         topics.put("gps");
         topics.put("camera");
         
         //String messageText = ;
         List<String> res = Datastore.getMotoSmeDevices(topics);
         
         List<String> devices =  com.motorola.motoask.gcm.Datastore.getDevices();
         com.motorola.motoask.gcm.server.Message msg = 
                 com.motorola.motoask.gcm.Utils.createMessage(title, q, payload);
              
         
         com.motorola.motoask.gcm.Utils.enqueuePush(msg, devices);
         
         
     }
   
    //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com","regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g","imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
    public static JSONObject handlePostToAnswers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        
   
        try {
            jsonData = Utils.getJsonBody(req);
            //jsonData.getString(PARAMETER_QID);
            String qIdString = jsonData.getString(PARAMETER_QID);
            String userId = jsonData.getString(PARAMETER_USER_ID);
            String userEmail = jsonData.getString(PARAMETER_USER_EMAIL);
            String aInfo = jsonData.getString(PARAMETER_A);
            
            Long qId = Long.parseLong(qIdString, 10);
            AnswerDataEntity answerData =
                    new AnswerDataEntity()
            	      .setQuestionId(qId)
                      .setUserId(userId)
                      .setEmail(userEmail)
                      .setAInfo(aInfo); 

            OfyService ofyService = OfyService.getInstance();
            ofyService.save(answerData);
            Long aId = answerData.getAnswerId();
            
            jsonResp.put("success", true);
            jsonResp.put(PARAMETER_AID, aId);
            jsonResp.put("message", "user added successfully");
        	OfyService.releaseInstance();

            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        
        return jsonResp;
        
    }
    
    public static JSONObject handlePostEditToAnswers(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        try {
            jsonData = Utils.getJsonBody(req);
            String aIdString =  resource.getId();
            String userId = "";
            String userEmail = "";
            String aInfo = "";
            
            if(jsonData.has(PARAMETER_USER_ID)){
            	userId = jsonData.getString(PARAMETER_USER_ID);
            }
            if(jsonData.has(PARAMETER_USER_EMAIL)){
            	userEmail = jsonData.getString(PARAMETER_USER_EMAIL);
            }
            if(jsonData.has(PARAMETER_A)){
            	aInfo = jsonData.getString(PARAMETER_A);
            }
            
            jsonResp.put("success", false);
            
            if(!aIdString.isEmpty()){
                Long aId = Long.parseLong(aIdString, 10);
            	OfyService ofyService = OfyService.getInstance();
            	
            	AnswerDataEntity answerData = ofyService.findById(AnswerDataEntity.class, aId);
            	
            	if(answerData != null){
            		
            		if(!userId.isEmpty()){
            			answerData.setUserId(userId);
            		}
            		if(!userEmail.isEmpty()){
            			answerData.setEmail(userEmail);
            		}
            		if(!aInfo.isEmpty()){
            			answerData.setAInfo(aInfo);
            		}
                    
            		ofyService.save(answerData);
            		
                    jsonResp.put("success", true);
                    jsonResp.put(PARAMETER_AID, aId);
                    jsonResp.put("message", "answer edited successfully");
            	}
            	
            	OfyService.releaseInstance();
            	
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/answer/12345 - question id
    public static JSONObject handleGetAnswer(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
                
        String qIdString = resource.getId();
        try {
            jsonResp.put("success", false);
            
            if(!qIdString.isEmpty()){
                Long qId = Long.parseLong(qIdString, 10);

            	OfyService ofyService = OfyService.getInstance();
            	
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put(PARAMETER_QID, qId);

            	List<AnswerDataEntity> listqData = ofyService.fetch(AnswerDataEntity.class, filters);
            	
            	Gson gson = new Gson();
            	 
            	String answerDataString = gson.toJson(listqData);
            	
                jsonResp.put("success", true);
                jsonResp.put("items", answerDataString);
            	OfyService.releaseInstance();

            }
            	
            	
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/questions/
    public static JSONObject handleGetAnswers(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        try {
            jsonResp.put("success", false);
            
        	OfyService ofyService = OfyService.getInstance();
        	
        	List<AnswerDataEntity> listqData = ofyService.fetch(AnswerDataEntity.class, null);
        	
        	Gson gson = new Gson();
        	 
        	String answerDataString = gson.toJson(listqData);
        	
            jsonResp.put("success", true);
            jsonResp.put("items", answerDataString);
        	OfyService.releaseInstance();
	
            	
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        return jsonResp;
        
    }
}
	