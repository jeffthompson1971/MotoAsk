package com.motorola.motoask;

import java.util.Date;

public final class Constants {

    public static final String VERSION = "0.0.6";
    
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
    
    public static final String OAUTHIO_PUBLIC_KEY = "mPCNxHkGqD0uSP7M7gaPMcclDYU";
    
    public static final String OAUTHIO_SECRET_KEY = "AAjpZa473V_MwbESCeRI0sv3tZs";
    
    public static final String API_KEY = "AIzaSyDsAcyc5WOw0IqvR93VR8cVsWCHJ8ZoDq4";
    // 3BD SHIT

    // Entity names across all packages / servlets
    public static final String MRED_ENTITY = "MredListing";
    
    
    // SW financial constants
    public static final String SWF_ORDER_ENTITY = "SWFOrder";
     
    // SW financial constants TODO - remove this ...
    //public static final String AMC_ACCOUNT_ENTITY_NAME = "AmcAccount";
    
    public static final String COMMON_PROP_CREATIONDATE = "creationdate";
    
    public static final String COMMON_PROP_USERID = "tbdId";
   
    public static final String EXT_SYS_ACCT_ENTITY_NAME = "ExtSysAccount";
    
    public static final String EXT_SYS_ACCT_PROP_ID = "extsysid";
    
    public static final String EXT_SYS_ACCT_PROP_CREDS = "c";
    
    public static final String USERS_ENTITY_NAME = "Users";
    
    public static final String SERVICE_ACCT_EMAIL = "22551269451-a45m7e3k36oppondbibmde7phailh2sp@developer.gserviceaccount.com";
    
    public static final String SERVICE_ACCT_CLIENT_ID = "22551269451-a45m7e3k36oppondbibmde7phailh2sp.apps.googleusercontent.com";
    
    public static final String DEV_CERT_FILE_PATH = "OKRPDevelopment.p12";
 
}
