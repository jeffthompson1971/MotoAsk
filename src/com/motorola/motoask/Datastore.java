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
import java.util.HashMap;
import java.util.List;
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

    //private static final String DEVICE_TRHEEBD_ID_PROPERTY = "tbdId";

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

        //Filter extSysEqFilter = new FilterPredicate(Constants.EXT_SYS_ACCT_PROP_ID, 
       //         FilterOperator.EQUAL, sysId.ordinal());

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
//    public static JSONArray findAllUsers(EXT_SYSTEM sysId) {
//
//        logger.info("findAllUsers()");
//
//        JSONArray retList = new JSONArray();
//
//        Filter extSysEqFilter = new FilterPredicate(Constants.EXT_SYS_ACCT_PROP_ID, 
//                FilterOperator.EQUAL, sysId.ordinal());
//
//        Query query = new Query(Constants.EXT_SYS_ACCT_ENTITY_NAME).setFilter(extSysEqFilter);
//
//        PreparedQuery preparedQuery = datastore.prepare(query);
//
//        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
//
//        for (Entity entity : entities) {
//
//            JSONObject tmpObj = Utils.entity2JSONObject(entity);
//            retList.put(tmpObj);
//        }
//
//        logger.info("returning users: " + retList.toString());    
//        return retList;
//
//    }
    
     
    /*
     * Get a list of all the users of an external system that used the given  credentials
     * @param sysId - id for the external system
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
//    public static JSONArray findAllUsersGivenCreds(EXT_SYSTEM sysId, String creds) {
//
//        logger.fine("findAllUsersGivenCreds()");
//
//        JSONArray retList = new JSONArray();
//
//        Filter extSysEqFilter = new FilterPredicate(Constants.EXT_SYS_ACCT_PROP_ID, 
//                FilterOperator.EQUAL, sysId.ordinal());
//        
//        Filter credEqFilter = new FilterPredicate(Constants.EXT_SYS_ACCT_PROP_CREDS, 
//                FilterOperator.EQUAL, creds);
//
//        Query query = new Query(Constants.EXT_SYS_ACCT_ENTITY_NAME).
//                setFilter(extSysEqFilter).setFilter(credEqFilter);
//
//        PreparedQuery preparedQuery = datastore.prepare(query);
//
//        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
//
//        for (Entity entity : entities) {
//            JSONObject tmpObj = Utils.entity2JSONObject(entity);
//            //Long id = entity.getKey().getId();
//            //tmpObj.put("key", id);
//            retList.put(tmpObj);
//        }
//        logger.info("found users: " + retList.toString());
//        return retList;
//
//    }

    
    /*
     * Update a user record's properties 
     * @param sysId - id for the external system
     * @param creds - credentials used base64 encoded ("username:password")
     * 
     * @return - JSONArray of users.
     */
    public static void updateUser(String email, String creds, HashMap<String,String> props) {

        logger.info("findAllUsers()");

        Transaction txn = datastore.beginTransaction();
      //  JSONArray names = newConfig.names();
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
//                 
//            for (int i = 0, size = names.length(); i < size; i++) {
//                
//                String name = names.getString(i);
//                String value = newConfig.getString(name); 
//                // set the     
//                theConfig.setProperty(name, value);
//
//                mMyCache.put(name, value);
//               
//            }
            
           // String test = mMyCache.get(names.getString(0));
            //logger.info("found value for MODE: " + test);
           
            datastore.put(theConfig);
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
