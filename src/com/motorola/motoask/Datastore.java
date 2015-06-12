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
package com.motorola.motoask;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.motorola.motoask.Constants;
import com.motorola.motoask.Utils;
import com.motorola.motoask.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Simple implementation of a data store using standard Java collections.
 * <p>
 * This class is neither persistent (it will lost the data when the app is
 * restarted) nor thread safe.
 */
public final class Datastore {

    static final int MULTICAST_SIZE = 1000;
    private static final String CONFIG_ENTITY_NAME = "Config";
    
    private static final String DEVICE_REG_ID_PROPERTY = "regId";

    private static final String MULTICAST_TYPE = "Multicast";

    private static final String MULTICAST_REG_IDS_PROPERTY = "regIds";

    private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder
           .withPrefetchSize(MULTICAST_SIZE).chunkSize(MULTICAST_SIZE);

    private static final Logger logger = Logger.getLogger(Datastore.class.getName());

    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private static final ICache mMyCache = PreFrontalLobe.getInstance();
       
    
    private Datastore() {

        throw new UnsupportedOperationException();
    }

    /*
     * Returns a list of all users of the specified external system
     *  used given credentials
     *  
     * @param sysId - id for the external system
     * @return - JSONArray of users.
     */    
    public static JSONArray getUsers(int offset, int records) {

        logger.info("findAllUsers()");

        JSONArray retList = new JSONArray();

        Query query = new Query(Constants.USERS_ENTITY_NAME);

        PreparedQuery preparedQuery = datastore.prepare(query);

        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);

        for (Entity entity : entities) {
            JSONObject tmpObj = Utils.entity2JSONObject(entity);
            retList.put(tmpObj);
        }
        return retList;

    }
    /*
     * Returns a list of all users of the specified external system
     *  used given credentials
     *  
     * @param sysId - id for the external system
     * @return - JSONArray of users.
     */    
    public static JSONArray getMotoSmes(int offset, int records) {

        logger.info("getMotoSmes()");

        JSONArray retList = new JSONArray();

        Query query = new Query(Constants.MOTOCROWD_ENTITY_NAME);

        PreparedQuery preparedQuery = datastore.prepare(query);

        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);

        for (Entity entity : entities) {
            JSONObject tmpObj = Utils.entity2JSONObject(entity);
            retList.put(tmpObj);
        }
        return retList;

    }
    
    public static List<String> getMotoSmes(JSONArray topics) {
        
        List smeList = new ArrayList<String>();
        
        JSONArray allSmes = getMotoSmes(0,0);
        
        JSONObject smeJson = null;
        
        Iterator smeIt=allSmes.iterator();
        while (smeIt.hasNext()) {
          //artifactDeployerEntries.add(populateAndGetEntry((JSONObject)it.next()));
            smeJson = new JSONObject((String)smeIt.next());
            JSONArray topicList = new JSONArray((String)smeIt.next());
          //  String topic = (String)it.next();
           // smeList.add((String)it.next());
            
        
        
        }
        
        
        Iterator it=topics.iterator();
        while (it.hasNext()) {
          //artifactDeployerEntries.add(populateAndGetEntry((JSONObject)it.next()));
            String topic = (String)it.next();
           // smeList.add((String)it.next());
            
        
        }
        
        
        return smeList;
        
    }
    
    private static Entity findUserByEmail(String email) {
        Query query = new Query(Constants.USERS_ENTITY_NAME);
        Filter equalFilter = new FilterPredicate(UserServlet.PARAMETER_EMAIL, FilterOperator.EQUAL,
                email);
        // .addFilter(DEVICE_REG_ID_PROPERTY, FilterOperator.EQUAL, regId);
        query.setFilter(equalFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Entity entity = null;
        if (!entities.isEmpty()) {
            entity = entities.get(0);
        }
        int size = entities.size();
        if (size > 0) {
            logger.info("Found " + size + " entities for email " + email + ": " + entities);
        }
        return entity;
    }
    private static Entity findMotoSmesByEmail(String email) {
        Query query = new Query(Constants.MOTOCROWD_ENTITY_NAME);
        Filter equalFilter = new FilterPredicate(UserServlet.PARAMETER_EMAIL, FilterOperator.EQUAL,
                email);
        // .addFilter(DEVICE_REG_ID_PROPERTY, FilterOperator.EQUAL, regId);
        query.setFilter(equalFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Entity entity = null;
        if (!entities.isEmpty()) {
            entity = entities.get(0);
        }
        int size = entities.size();
        if (size > 0) {
            logger.info("Found " + size + " entities for email " + email + ": " + entities);
        }
        return entity;
    }
    
    /*
     * Returns a list of all questions in the specified state.
     *  
     * @param sysId - id for the external system
     * @return - JSONArray of users.
     */    
    public static JSONArray getQuestionsInState(String state) {
        logger.info("getQuestionsInState()");
        Query query = new Query(Constants.QUESTION_ENTITY_NAME);
        Filter equalFilter = new FilterPredicate(QuestionServlet.PARAMETER_Q_STATE, FilterOperator.EQUAL,
                state);
        query.setFilter(equalFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        int size = entities.size();
        if (size > 0) {
            logger.info("Found " + size + " questions in state " + state + ": " + entities);
        }

        JSONArray retList = new JSONArray();
        for (Entity entity : entities) {
            JSONObject tmpObj = Utils.entity2JSONObject(entity);
            retList.put(tmpObj);
        }
        return retList;
    }


    /*
     * Update a user record's properties 
     * @param sysId - id for the external system
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
    public static void createMotoSme(String name, String email, JSONArray topics) {
      
        logger.info("createMotoSme()");

        Entity entity = findMotoSmesByEmail(email);

        if (entity != null) {
            logger.info(email + " is already a user ... ignoring.");

        } else {

            Transaction txn = datastore.beginTransaction();
            try {
     
                entity = new Entity(Constants.MOTOCROWD_ENTITY_NAME);
                entity.setProperty(UserServlet.PARAMETER_NAME, name);
                entity.setProperty(UserServlet.PARAMETER_EMAIL, email);
                entity.setProperty(UserServlet.PARAMETER_TOPICS, topics.toString());
                entity.setProperty("creationdate", new Date());

                datastore.put(entity);
                txn.commit();

            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                    logger.severe("failed to create user for :" + name);
                }
            }
        }
    }
    
     
    /*
     * Update a user record's properties 
     * @param sysId - id for the external system
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
    public static JSONObject createUser(String id, String name, String email, String regId,
            String imageUrl, String devInfo) {
        
        logger.info("createUser()");
        JSONObject jsonResp = new JSONObject();
        

        Entity entity = findUserByEmail(email);

        if (entity != null) {
            logger.info(email + " is already a user ... ignoring.");
            jsonResp.put("success", false);
            jsonResp.put("message", "User already registered.");
            
        } else {

            Transaction txn = datastore.beginTransaction();
            try {
                
                entity = new Entity(Constants.USERS_ENTITY_NAME);
                entity.setProperty(UserServlet.PARAMETER_USER_ID, id);
                entity.setProperty(UserServlet.PARAMETER_NAME, name);
                entity.setProperty(UserServlet.PARAMETER_EMAIL, email);
                entity.setProperty(UserServlet.PARAMETER_REG_ID, regId);
                entity.setProperty(UserServlet.PARAMETER_AVATAR, imageUrl);
                entity.setProperty(UserServlet.PARAMETER_DEVINFO, devInfo);      
                entity.setProperty("creationdate", new Date());

                datastore.put(entity);
                
                txn.commit();
                jsonResp.put("success", true);
                
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                    logger.severe("failed to create user for :" + name);
                    jsonResp.put("success", false);
                    jsonResp.put("message", "database failure - failed to create user");
                    
                }
            }
        }
        return jsonResp;
    }
    
    /*
     * Update a user record's properties 
     * @param email - email for the user
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
    public static JSONObject updateMotoSme(String userId, String email, HashMap<String,String> props) {

        logger.info("updateMotoSme()");
        JSONObject jsonResp = new JSONObject();
        Transaction txn = datastore.beginTransaction();
       
        Entity theSme = null;
        
        try {
          
            Query query = new Query(Constants.MOTOCROWD_ENTITY_NAME);    
          
            if (userId != null) {
                Filter equalFilter = new FilterPredicate(UserServlet.PARAMETER_USER_ID, FilterOperator.EQUAL, userId);
                query.setFilter(equalFilter);
            } else {
                Filter equalFilter = new FilterPredicate(UserServlet.PARAMETER_EMAIL, FilterOperator.EQUAL, email);
                query.setFilter(equalFilter);
            }
       
            PreparedQuery preparedQuery = datastore.prepare(query);

            List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
          
            if (!entities.isEmpty()) {
                theSme = entities.get(0);
            }
           
            // If it doesn't exist, create it...
            if (theSme == null) {
                jsonResp.put("success", false);
                jsonResp.put("message", "sme does not exist");
                return jsonResp;
             
                //theSme = new Entity(CONFIG_ENTITY_NAME);
            }
           
            Iterator it = props.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                logger.info(pair.getKey() + " = " + pair.getValue());
                String key = (String) pair.getKey();
                if (key.equals(UserServlet.PARAMETER_NEW_POINTS)) {
                    int points = Integer.parseInt((String)pair.getValue());
                    theSme.setProperty(key, points);
                } else
                    theSme.setProperty(key, pair.getValue());

                it.remove(); // avoids a ConcurrentModificationException
            }
            theSme.setProperty("userId", userId);
            
            datastore.put(theSme);
            txn.commit();
            jsonResp.put("success", true);
        }
     
        finally {
            if (txn.isActive()) {
                txn.rollback();
                jsonResp.put("success", false);
                
            }
        }
        return jsonResp;
    }
    
    /*
     * Update a user record's properties 
     * @param email - email for the user
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
    public static void updateUser(String userId, HashMap<String,String> props) {

        logger.info("updateUser()");
        JSONObject jsonResp = new JSONObject();
        Transaction txn = datastore.beginTransaction();
       
        Entity theUser = null;
        
        try {
          
            Query query = new Query(Constants.USERS_ENTITY_NAME);               
         
            Filter equalFilter = new FilterPredicate(UserServlet.PARAMETER_USER_ID, FilterOperator.EQUAL, userId);
            query.setFilter(equalFilter);
                     
            PreparedQuery preparedQuery = datastore.prepare(query);
            
            List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
          
            if (!entities.isEmpty()) {
                theUser = entities.get(0);
                
            } else {
                jsonResp.put("success", false);
                jsonResp.put("message", "Don't have user in datastore: " + userId);

            }
           
            datastore.put(theUser);
            txn.commit();
        }
     
        finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Updates the config id of a device.
     */
    public static void updateConfig(JSONObject newConfig) {
       // logger.info("Updating " + oldId + " to " + newId);
        Transaction txn = datastore.beginTransaction();
        JSONArray names = newConfig.names();
        Entity theConfig = null;
        
        try {
          
            Query query = new Query(CONFIG_ENTITY_NAME);               
            PreparedQuery preparedQuery = datastore.prepare(query);

            List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
          
            if (!entities.isEmpty()) {
                theConfig = entities.get(0);
            }
           
            // If it doesn't exist, create it...
            if (theConfig == null) {
             
                theConfig = new Entity(CONFIG_ENTITY_NAME);
            }
                 
            for (int i = 0, size = names.length(); i < size; i++) {
                
                String name = names.getString(i);
                String value = newConfig.getString(name); 
                // set the     
                theConfig.setProperty(name, value);

                mMyCache.put(name, value);
               
            }
            
            String test = mMyCache.get(names.getString(0));
            logger.info("found value for MODE: " + test);
           
            datastore.put(theConfig);
            txn.commit();
        }
     
        finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }           
    }
}
