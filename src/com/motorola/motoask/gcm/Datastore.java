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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.EnumUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.motorola.motoask.Constants;
//import com.threebd.apps.Constants.DEVICE_OS;
import com.motorola.motoask.Utils;

/**
 * Simple implementation of a data store using standard Java collections.
 * <p>
 * This class is neither persistent (it will lost the data when the app is
 * restarted) nor thread safe.
 */
public final class Datastore {

    static final int MULTICAST_SIZE = 1000;
    private static final String DEVICE_ENTITY = "Device";
    private static final String DEVICE_REG_ID_PROPERTY = "regId";
    private static final String TBD_ID_PROPERTY = "tbdId";
    private static final String EMAIL_PROPERTY = "email";
    private static final String OS_PROPERTY = "os";
    // private static final String CREDS_PROPERTY = "c";

    private static final String MULTICAST_TYPE = "Multicast";
    private static final String MULTICAST_REG_IDS_PROPERTY = "regIds";

    private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder
            .withPrefetchSize(MULTICAST_SIZE).chunkSize(MULTICAST_SIZE);

    private static final Logger logger = Logger.getLogger(Datastore.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Datastore() {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers a device.
     * 
     * @param regId
     *            device's registration id.
     */
    public static boolean register(String tbdId, String email, String regId) {
        logger.info("Registering device: " + regId + "for user " + email);

        int osOrdinal = 0;
        boolean success = true;
        Entity entity = findDeviceByRegId(regId);

        if (entity != null) {
            logger.info(regId + " is already registered ... ignoring.");

        } else {

            Transaction txn = datastore.beginTransaction();
            try {

              
                entity = new Entity(DEVICE_ENTITY);
               
                entity.setProperty(EMAIL_PROPERTY, email);
                entity.setProperty(DEVICE_REG_ID_PROPERTY, regId);
                entity.setProperty(TBD_ID_PROPERTY, tbdId);
                entity.setProperty("creationdate", new Date());

                datastore.put(entity);
                txn.commit();

            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                    success = false;
                }
            }
        }
        return success;
    }
    
    /**
     * Registers a device.
     * 
     * @param regId
     *            device's registration id.
     */
    public static boolean addUser(String id, String email, String regId, String devInfo, 
            String photo) {
        
        logger.info("Registering device: " + regId + "for user " + email);

        int osOrdinal = 0;
        boolean success = true;
        Entity entity = findDeviceByRegId(regId);

        if (entity != null) {
            logger.info(regId + " is already registered ... ignoring.");

        } else {

            Transaction txn = datastore.beginTransaction();
            try {

              
                entity = new Entity(DEVICE_ENTITY);
               
                entity.setProperty(EMAIL_PROPERTY, email);
                entity.setProperty(DEVICE_REG_ID_PROPERTY, regId);
                entity.setProperty(TBD_ID_PROPERTY, tbdId);
                entity.setProperty("creationdate", new Date());

                datastore.put(entity);
                txn.commit();

            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Unregisters a device.
     * 
     * @param regId
     *            device's registration id.
     */
    public synchronized static void unregister(String regId) {
        logger.info("Unregistering " + regId);
        Transaction txn = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(regId);
            if (entity == null) {
                logger.warning("Device " + regId + " already unregistered");
            } else {
                Key key = entity.getKey();
                datastore.delete(key);
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Updates the registration id of a device.
     */
    public static void updateRegistration(String oldId, String newId) {
        logger.info("Updating " + oldId + " to " + newId);
        Transaction txn = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(oldId);
            if (entity == null) {
                logger.warning("No device for registration id " + oldId);
                return;
            }
            entity.setProperty(DEVICE_REG_ID_PROPERTY, newId);
            datastore.put(entity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Gets all registered devices.
     */
    public static List<String> getDevices() {
        List<String> devices;
        Transaction txn = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_ENTITY);
            Iterable<Entity> entities = datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
            devices = new ArrayList<String>();
            for (Entity entity : entities) {
                String device = (String) entity.getProperty(DEVICE_REG_ID_PROPERTY);
                devices.add(device);
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        return devices;
    }

    /**
     * Gets all registered devices - can return only the IDs if needed by using
     * idOnly para
     */
    public static JSONArray getDevices(boolean idOnly) {
        // List<String> devices;
        Transaction txn = datastore.beginTransaction();
        JSONArray respJson = new JSONArray();

        try {
            Query query = new Query(DEVICE_ENTITY);
            Iterable<Entity> entities = datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
            // = new ArrayList<String>();

            for (Entity entity : entities) {
                if (idOnly) {
                    String device = (String) entity.getProperty(DEVICE_REG_ID_PROPERTY);

                    // devices.add(device);
                    respJson.put(device);

                } else {
                    // send the full records with all properties
                    JSONObject recAsJson = Utils.entity2JSONObject(entity);
                    respJson.put(recAsJson);

                }

            }
            txn.commit();

        } finally {
            if (txn.isActive()) {
                logger.severe("failed to read devices!");
                txn.rollback();
            }
        }
        return respJson;
    }

    /**
     * Gets the number of total devices.
     */
    public synchronized static int getTotalDevices() {
        Transaction txn = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_ENTITY).setKeysOnly();
            List<Entity> allKeys = datastore.prepare(query).asList(DEFAULT_FETCH_OPTIONS);
            int total = allKeys.size();
            logger.fine("Total number of devices: " + total);
            txn.commit();
            return total;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public synchronized static List<String> getDevicesByTbdId(String tbdId) {
        List<String> devices;
        Transaction txn = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_ENTITY);
            Filter equalFilter = new FilterPredicate(TBD_ID_PROPERTY, FilterOperator.EQUAL, tbdId);
            query.setFilter(equalFilter);

            Iterable<Entity> entities = datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
            devices = new ArrayList<String>();
            for (Entity entity : entities) {
                String device = (String) entity.getProperty(DEVICE_REG_ID_PROPERTY);
                devices.add(device);
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        return devices;
    }

    public synchronized static List<String> getDevicesByCreds(String creds) {
        List<String> devices;
        Transaction txn = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_ENTITY);
            Filter equalFilter = new FilterPredicate(Constants.EXT_SYS_ACCT_PROP_CREDS,
                    FilterOperator.EQUAL, creds);
            query.setFilter(equalFilter);

            Iterable<Entity> entities = datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
            devices = new ArrayList<String>();
            for (Entity entity : entities) {
                String device = (String) entity.getProperty(DEVICE_REG_ID_PROPERTY);
                devices.add(device);
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        return devices;
    }

    private static Entity findDeviceByRegId(String regId) {
        Query query = new Query(DEVICE_ENTITY);
        Filter equalFilter = new FilterPredicate(DEVICE_REG_ID_PROPERTY, FilterOperator.EQUAL,
                regId);
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
            logger.info("Found " + size + " entities for regId " + regId + ": " + entities);
        }
        return entity;
    }

    /**
     * Creates a persistent record with the devices to be notified using a
     * multicast message.
     * 
     * @param devices
     *            registration ids of the devices.
     * @return encoded key for the persistent record.
     */
    public static String createMulticast(List<String> devices) {
        logger.info("Storing multicast for " + devices.size() + " devices");
        String encodedKey;
        Transaction txn = datastore.beginTransaction();
        try {
            Entity entity = new Entity(MULTICAST_TYPE);
            entity.setProperty(MULTICAST_REG_IDS_PROPERTY, devices);
            datastore.put(entity);
            Key key = entity.getKey();
            encodedKey = KeyFactory.keyToString(key);
            logger.fine("multicast key: " + encodedKey);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        return encodedKey;
    }

    /**
     * Gets a persistent record with the devices to be notified using a
     * multicast message.
     * 
     * @param encodedKey
     *            encoded key for the persistent record.
     */
    public static List<String> getMulticast(String encodedKey) {
        Key key = KeyFactory.stringToKey(encodedKey);
        Entity entity;
        Transaction txn = datastore.beginTransaction();
        try {
            entity = datastore.get(key);
            @SuppressWarnings("unchecked")
            List<String> devices = (List<String>) entity.getProperty(MULTICAST_REG_IDS_PROPERTY);
            txn.commit();
            return devices;
        } catch (EntityNotFoundException e) {
            logger.severe("No entity for key " + key);
            return Collections.emptyList();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Updates a persistent record with the devices to be notified using a
     * multicast message.
     * 
     * @param encodedKey
     *            encoded key for the persistent record.
     * @param devices
     *            new list of registration ids of the devices.
     */
    public static void updateMulticast(String encodedKey, List<String> devices) {
        Key key = KeyFactory.stringToKey(encodedKey);
        Entity entity;
        Transaction txn = datastore.beginTransaction();
        try {
            try {
                entity = datastore.get(key);
            } catch (EntityNotFoundException e) {
                logger.severe("No entity for key " + key);
                return;
            }
            entity.setProperty(MULTICAST_REG_IDS_PROPERTY, devices);
            datastore.put(entity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Deletes a persistent record with the devices to be notified using a
     * multicast message.
     * 
     * @param encodedKey
     *            encoded key for the persistent record.
     */
    public static void deleteMulticast(String encodedKey) {
        Transaction txn = datastore.beginTransaction();
        try {
            Key key = KeyFactory.stringToKey(encodedKey);
            datastore.delete(key);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
