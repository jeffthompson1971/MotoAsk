package com.motorola.motoask;

import com.google.common.annotations.VisibleForTesting;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.Query;
import com.googlecode.objectify.util.Closeable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

public class OfyService {
	  /** Register the classes to persist with Objectify. */

	  static {
		  ObjectifyService.factory().register(QuestionDataEntity.class);
	  }

	  public static OfyService ofyService = null;
	  public static Closeable closeable = null;

	  @VisibleForTesting
	  public OfyService() {}

	  public static OfyService getInstance() {
		if(ofyService == null){
			ofyService = new OfyService();
			closeable = ObjectifyService.begin();
			
			// need to call closeable.close() when objectify is no longer needed
		}
	    return ofyService;
	  }

	  /** Return current objectify instance. */
	  public Objectify ofy() {
	    return ObjectifyService.ofy();
	  }

	  //private static final Logger logger = Logger.getLogger(OfyService.class.getName());



	  /** Save entities to the datastore and return their keys. */
	  public <T> Map<Key<T>, T> save(Iterable<T> entities) {
	    return ofy().save().entities(entities).now();
	  }

	  /** Save an entity to the datastore and return its key. */
	  public <T> Key<T> save(T entity) {
	    return ofy().save().entity(entity).now();
	  }

	  /** Save an entity to the datastore with transaction. */
	  public <T> void saveWithTransaction(final T entity) {
	    saveWithTransaction(Collections.singletonList(entity));
	  }

	  /** Save an entities to the datastore with transaction. */
	  public <T> void saveWithTransaction(final Iterable<T> entities) {
	    ofy().transact(new VoidWork() {
	      @Override
	      public void vrun() {
	        ofy().save().entities(entities).now();
	      }
	    });
	  }

	  /** Immediately delete specified entity identified by class and id. */
	  public void delete(Class<?> kindClass, String id) {
	    ofy().delete().type(kindClass).id(id).now();
	  }

	  /** Immediately delete specified entities identified by class and ids. */
	  public void delete(Class<?> kindClass, Iterable<String> ids) {
	    ofy().delete().type(kindClass).ids(ids).now();
	  }

	  /** Immediately delete entities whose keys are specified. */
	  public void delete(Iterable<? extends Key<?>> keys) {
	    ofy().delete().keys(keys).now();
	  }

	  /** Immediately delete specified entities. */
	  public void delete(Collection<?> entities) {
	    ofy().delete().entities(entities).now();
	  }

	  /** Returns list of objects of specified class that match specified filters. */
	  public <T> List<T> fetch(Class<T> kindClass, Map<String, Object> filters) {
	    return fetch(kindClass, filters, null);
	  }
	  

	  /** Returns list of objects of specified class that match the specified filters and ids. */
	  public <T> List<T> fetch(Class<T> kindClass, @Nullable Map<String, Object> filters,
	      @Nullable Iterable<String> ids) {
	    Query<T> query = query(kindClass, filters, ids);
	    return query.list();
	  }
	  

	  /** Returns the object of specified class and id. */
	  @Nullable
	  public <T> T findById(Class<T> kindClass, String id) {
		  ofy().clear();
		  return ofy().load().type(kindClass).id(id).now();
	  }

	  /** Returns the object of the specified class and id. */
	  @Nullable
	  @SuppressWarnings("unused") // TODO(scott): remove once used or write a test for this method
	  public <T> T findById(Class<T> kindClass, Long id) {
	    return ofy().load().type(kindClass).id(id).now();
	  }

	  /** Returns objects of specified class and ids. */
	  public <T> Collection<T> findByIds(Class<T> kindClass, Iterable<String> ids) {
	    return ofy().load().type(kindClass).ids(ids).values();
	  }

	  /** Returns a typed query handle for specified class.*/
	  public <T> Query<T> query(Class<T> kindClass) {
	    return ofy().load().type(kindClass);
	  }
	  

	  /**
	   * Returns a typed query handle for the specified class.
	   * The query will match the specified filters and ids and can be executed async.
	   */
	   
	  public <T> Query<T> query(Class<T> kindClass, @Nullable Map<String, Object> filters,
	      @Nullable Iterable<String> ids) {
	    Query<T> query = query(kindClass);
	    if (ids != null) {
	      List<Key<T>> keys = new ArrayList<Key<T>>();
	      for (String id : ids) {
	        //LoggerUtil.info(logger, "id {0}", id);
	        keys.add(Key.create(kindClass, id));
	      }
	      query = query.filterKey("in", keys);
	    }

	    if (filters != null) {
	      for (Map.Entry<String, Object> entry : filters.entrySet()) {
	        //LoggerUtil.info(logger, "entry getKey: {0}, entry.getValue: {1}",
	        //    entry.getKey(), entry.getValue());
	        query = query.filter(entry.getKey(), entry.getValue());
	      }
	    }
	    return query;
	  }
	  
	  
	  
}
