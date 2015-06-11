package com.motorola.motoask;

import java.util.Date;

public final class Constants {

    public static final String VERSION = "0.0.1";
    
    public static boolean DEBUG = true;
    
   
    public static enum DEVICE_OS {
        ANDROID, IOS
    }

    public static enum ERROR {

        AUTHENTICATION, AUTHORIZATION, APPLICATION
    }
    
    
    // ** THIS IS KEY - need to be sure we store data with consistent IDs
    public static enum CACHE_KEY {
        
        CONFIG

    }
    
//    public static final String OAUTHIO_PUBLIC_KEY = "mPCNxHkGqD0uSP7M7gaPMcclDYU";
//    
//    public static final String OAUTHIO_SECRET_KEY = "AAjpZa473V_MwbESCeRI0sv3tZs";
//    
   // public static final String API_KEY = "AIzaSyCRjlUu6p-YaUBMyLsD3z_BBhZcS8NCm3k";
   
    public static final String API_KEY = "AIzaSyCb7m0g9jzpdIR89ubH_GjYJop2bmX3dIs";
    
    // Entity names across all packages / servlets
  
     
    // SW financial constants TODO - remove this ...
    //public static final String AMC_ACCOUNT_ENTITY_NAME = "AmcAccount";
    
    public static final String COMMON_PROP_CREATIONDATE = "creationdate";
    
    public static final String COMMON_PROP_USERID = "userId";
   
    public static final String USERS_ENTITY_NAME = "Users";
    
    public static final String MOTOCROWD_ENTITY_NAME = "MotoCrowd";
    
    //public static final String USER_ENTITY_NAME = "User";
    
    public static final String SERVICE_ACCT_EMAIL = "566930563926-h65ofam6m6blidcqvcjubus7qj9f4dvf@developer.gserviceaccount.com";
//    
    public static final String SERVICE_ACCT_CLIENT_ID = "566930563926-h65ofam6m6blidcqvcjubus7qj9f4dvf.apps.googleusercontent.com";
//    
//    public static final String DEV_CERT_FILE_PATH = "OKRPDevelopment.p12";
 
}
