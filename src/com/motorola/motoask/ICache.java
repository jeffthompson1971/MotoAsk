package com.motorola.motoask;

import java.util.Set;

public interface ICache {

    public boolean put (String key, String value);
   
    public String get (String key);
    
    public boolean containsKey(String key);

    public void  remove(String key);

}
