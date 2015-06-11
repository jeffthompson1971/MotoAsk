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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.EnumUtils;

import net.sf.json.JSONObject;

//import com.motorola.motoask.swfinancial.RestHandlers;

import java.util.logging.Logger;

import com.motorola.motoask.Constants;
/**
 * Servlet that registers a device, whose registration id is identified by
 * {@link #PARAMETER_REG_ID}.
 *
 * <p>
 * The client app should call this servlet every time it receives a
 * {@code com.google.android.c2dm.intent.REGISTRATION C2DM} intent without an
 * error or {@code unregistered} extra.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

    protected static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_USER_ID = "userId";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_AVATAR = "photo";
    
    private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        JSONObject jsonResp = new JSONObject();
        String regId, userId, email, os;
      
        try {
            regId = getParameter(req, PARAMETER_REG_ID);
            userId = getParameter(req, PARAMETER_USER_ID);
            email = getParameter(req, PARAMETER_EMAIL);
          
            
           // boolean temp = EnumUtils.isValidEnum(Constants.DEVICE_OS.class, os);
            
            if (regId == null || userId == null || regId.length() == 0 || userId.length() == 0
                    || email == null || email.length() == 0){
                setFail(resp);
                jsonResp.put("success", false);
                jsonResp.put("message", "bad or missing param");
                resp.getWriter().write(jsonResp.toString());
                resp.flushBuffer();
                return;
            }
            log.info("registering device for " + email);
           
          
        if (Datastore.register(userId, email, regId)) {
            setSuccess(resp);
            jsonResp.put("success", true);
            
        } else  {
            setFail(resp);
            jsonResp.put("success", false);
        
        }
        resp.getWriter().write(jsonResp.toString());
        resp.flushBuffer();
    
        } catch (Exception e) {
            resp.setStatus(400);
            log.severe("fail to register device: " + e.toString());
          
        }

    }
}
