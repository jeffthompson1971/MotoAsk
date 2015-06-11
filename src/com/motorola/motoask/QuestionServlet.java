
package com.motorola.motoask;
import com.motorola.motoask.qc.QClassification;
import com.motorola.motoask.qc.QClassifier;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONObject;

import com.google.gson.Gson;
import com.motorola.motoask.RestRequest;
import com.motorola.motoask.Utils;
import com.motorola.motoask.gcm.RegisterServlet;




@SuppressWarnings("serial")
public class QuestionServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(QuestionServlet.class.getName());
    
    public static final String PARAMETER_QID = "qId"; // string
    public static final String PARAMETER_USER_ID = "userId";// string
    public static final String PARAMETER_USER_EMAIL = "userEmail";// string
    public static final String PARAMETER_Q = "q"; // string
    public static final String PARAMETER_Q_DETAILS = "qDetails";// string
    public static final String PARAMETER_Q_TOPICS = "qTopics"; 
    public static final String PARAMETER_Q_STATE = "qState"; 
   
    //public static final String PARAMETER_DEVINFO = "devInfo"; //JSON string
    
    public static  enum Resource {
        question, questions
     }
     
     private String REST_RES_PATTERN = "/(questions|question)$";

     private String REST_RES_WITH_ID_PATTERN = "/(question)/([A-Za-z0-9]*)";

     
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

                 case questions:
                     jsonResp = QuestionServlet.handlePostToQuestions(req, res);
                     break;
                 case question:
                     jsonResp = QuestionServlet.handlePostEditToQuestions(req, res, resourceValues);
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
                 case questions:
                    jsonResp = QuestionServlet.handleGetQuestions(req, res);
                    break;
                 case question:
                    jsonResp = QuestionServlet.handleGetQuestion(req, res, resourceValues);
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
   
   
    //  curl -X POST -d '{"userName":"Wentao Chang","email":"wchang@motorola.com","regId":"APA91bGw9qM1DXFZjBeBsZ9VKm2GoKjG-Gk_2SfB5eqtMta1l3leNV_bjb6wMGjalQYVSVcoLSiojlHqru59OkCNRsNgP3y6FnYsRa98rvijmtUsdDGKLHECgmnCyd0u8f2U638J_j6Av46D4QP4l9itLLVl-nYR1g","imageUrl":"https:\/\/lh6.googleusercontent.com\/-3bzf0-gdsaQ\/AAAAAAAAAAI\/AAAAAAAAAHs\/EzPegZlcVmA\/photo.jpg?sz=50","userId":"114824279230486482278","deviceInfo":{"imei":"NBVV2F0086","device_model":"XT1585","carrier":""}}' localhost:8888/api/v1/users
    public static JSONObject handlePostToQuestions(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        
   
        try {
            jsonData = Utils.getJsonBody(req);
            //jsonData.getString(PARAMETER_QID);
            String userId = jsonData.getString(PARAMETER_USER_ID);
            String userEmail = jsonData.getString(PARAMETER_USER_EMAIL);
            String qInfo = jsonData.getString(PARAMETER_Q);
            String details = jsonData.getString(PARAMETER_Q_DETAILS);
            // String topics = jsonData.getString(PARAMETER_Q_TOPICS);
            QClassifier classer = new QClassifier(qInfo);
            QClassification classiness = classer.classify();
            String topics = classiness.getClassification();
            
            QuestionDataEntity questionData =
                    new QuestionDataEntity()
                      .setUserId(userId)
                      .setEmail(userEmail)
                      .setQInfo(qInfo)
                      .setQDetails(details)
                      .setQTopics(topics);

            OfyService ofyService = OfyService.getInstance();
            ofyService.save(questionData);
            Long qId = questionData.getQuestionId();
            
            jsonResp.put("success", true);
            jsonResp.put(PARAMETER_QID, qId);
            jsonResp.put("message", "user added successfully");
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        
        return jsonResp;
        
    }
    
    public static JSONObject handlePostEditToQuestions(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        try {
            jsonData = Utils.getJsonBody(req);
            String qIdString =  resource.getId();
            String userId = "";
            String userEmail = "";
            String qInfo = "";
            String details = "";
            String topics = "";
            
            if(jsonData.has(PARAMETER_USER_ID)){
            	userId = jsonData.getString(PARAMETER_USER_ID);
            }
            if(jsonData.has(PARAMETER_USER_EMAIL)){
            	userEmail = jsonData.getString(PARAMETER_USER_EMAIL);
            }
            if(jsonData.has(PARAMETER_Q)){
            	qInfo = jsonData.getString(PARAMETER_Q);
            }
            if(jsonData.has(PARAMETER_Q_DETAILS)){
            	details = jsonData.getString(PARAMETER_Q_DETAILS);
            }
            if(jsonData.has(PARAMETER_Q_TOPICS)){
            	topics = jsonData.getString(PARAMETER_Q_TOPICS);
            }
            
            jsonResp.put("success", false);
            
            if(!qIdString.isEmpty()){
                Long qId = Long.parseLong(qIdString, 10);
            	OfyService ofyService = OfyService.getInstance();
            	
            	QuestionDataEntity questionData = ofyService.findById(QuestionDataEntity.class, qId);
            	
            	if(questionData != null){
            		
            		if(!userId.isEmpty()){
            			questionData.setUserId(userId);
            		}
            		if(!userEmail.isEmpty()){
            			questionData.setEmail(userEmail);
            		}
            		if(!qInfo.isEmpty()){
            			questionData.setQInfo(qInfo);
            		}
            		if(!details.isEmpty()){
            			questionData.setQDetails(details);
            		}
            		if(!topics.isEmpty()){
            			questionData.setQTopics(topics);
            		}
                    
            		ofyService.save(questionData);
            		
                    jsonResp.put("success", true);
                    jsonResp.put(PARAMETER_QID, qId);
                    jsonResp.put("message", "user edited successfully");
            	}
            	
            	
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        
        return jsonResp;
        
    }
    
    //  curl  localhost:8888/api/v1/question/12345
    public static JSONObject handleGetQuestion(HttpServletRequest req, HttpServletResponse res, RestRequest resource) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
                
        String qIdString = resource.getId();
        try {
            jsonResp.put("success", false);
            
            if(!qIdString.isEmpty()){
                Long qId = Long.parseLong(qIdString, 10);

            	OfyService ofyService = OfyService.getInstance();
            	
            	QuestionDataEntity questionData = ofyService.findById(QuestionDataEntity.class, qId);
            	
            	Gson gson = new Gson();
            	 
            	String questionDataString = gson.toJson(questionData);
            	
                jsonResp.put("success", true);
                jsonResp.put("item", questionDataString);
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
    public static JSONObject handleGetQuestions(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jsonResp = new JSONObject();
        JSONObject jsonData = new JSONObject();
        
        try {
            jsonResp.put("success", false);
            
        	OfyService ofyService = OfyService.getInstance();
        	
        	List<QuestionDataEntity> listqData = ofyService.fetch(QuestionDataEntity.class, null);
        	
        	Gson gson = new Gson();
        	 
        	String questionDataString = gson.toJson(listqData);
        	
            jsonResp.put("success", true);
            jsonResp.put("items", questionDataString);
            	
            	
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsonResp.put("success", false);
            jsonResp.put("message", e.toString());
        }
        
        return jsonResp;
        
    }
}
	