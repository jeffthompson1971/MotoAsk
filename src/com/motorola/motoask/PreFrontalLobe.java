package com.motorola.motoask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;


public class PreFrontalLobe implements ICache {
    private static final Logger log = Logger.getLogger(PreFrontalLobe.class.getName());
    private static  net.sf.jsr107cache.Cache mCache;

    //private static DatastoreService
    static {

        Map props = new HashMap();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        //props.put(GCacheFactory.EXPIRATION_DELTA, 3600);
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            mCache = cacheFactory.createCache(props);

        } catch (CacheException e) {
           log.severe(e.toString());
        }
    }

    
    private static PreFrontalLobe instance = null;

    
    protected PreFrontalLobe() {
        // Exists only to defeat instantiation.
    }

    public static PreFrontalLobe getInstance() {
        if(instance == null) {
           instance = new PreFrontalLobe();
        }
        return instance;
     }

    @Override
    public synchronized boolean put(String key, String value) {
        // TODO error handling
        mCache.put(key, value);
        log.info("cache put() - setting \"" + key + "\" to \"" + value + "\"");
        return true;
    }

    @Override
    public synchronized String get(String key) {
       // TODO error handling
       String val =  (String) mCache.get(key);
       log.info("cache get() - " + key + " = " + val );
       return val;
    }
    
    @Override
    public synchronized void remove(String key) {
       // TODO error handling
        mCache.remove(key);
      // log.info("cache get() - " + key + " = " + val );
      // return val;
    }
    
    
   
    @Override
    public synchronized boolean containsKey(String key) {
       
        return (mCache.containsKey(key));
        
    }
}
