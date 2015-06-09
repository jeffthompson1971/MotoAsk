package com.motorola.motoask;

import net.sf.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.util.DateTime;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;

/* stuff for token verification */
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.*;
import net.sf.json.util.JSONUtils;

//import com.threebd.apps.common.WebFlowContext;
//import com.threebd.apps.swfinancial.*;

import org.apache.commons.codec.binary.Base64;

public class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    private static final String BLOBKEY_PATTERN = "/.*(encoded_gs_key.*)>/";

    private static final Set<Class<?>> GAE_SUPPORTED_TYPES = DataTypeUtils.getSupportedTypes();

    private static InetAddress ip;

    
    public static String getNewDataPointId() {
        
       return Long.toString( System.currentTimeMillis());
    }
    
     public InetAddress getIp() {
        try {

            ip = InetAddress.getLocalHost();
            return ip;
            // System.out.println("Current IP address : " +
            // ip.getHostAddress());

        } catch (UnknownHostException e) {
            log.severe(e.toString());
            // e.e.printStackTrace();

        }
        return null;

    }

    public static boolean isValidJSON(String jsonString) {
        boolean valid = false;
        try {
            JSONArray.fromObject(jsonString);
            valid = true;
        } catch (JSONException ex) {
            valid = false;
        }
        return valid;
    }

    // in format username:password
    public static String[] decodeCredentials(String authHeader) {

        String[] credBits = null;

        try {
            byte[] tmp = Base64.decodeBase64(authHeader.getBytes());
            String creds = new String(tmp, "UTF-8");
            log.info("Decoded bytes: " + creds);
            credBits = creds.split(":");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.severe(e.toString());
            // e.printStackTrace();
        }
        return credBits;
    }

  
  

    public void printHeader(HttpServletRequest request, HttpServletResponse response) {
        try {

            String headers = null;
            String htmlHeader = "<HTML><HEAD><TITLE> Request Headers</TITLE></HEAD><BODY>";
            String htmlFooter = "</BODY></HTML>";

            response.setContentType("text/html");

            PrintWriter out = response.getWriter();
            Enumeration e = request.getHeaderNames();

            out.println(htmlHeader);
            out.println("<TABLE ALIGN=CENTER BORDER=1>");
            out.println("<tr><th> Header </th><th> Value </th>");

            while (e.hasMoreElements()) {
                headers = (String) e.nextElement();
                if (headers != null) {
                    out.println("<tr><td align=center><b>" + headers + "</td>");
                    out.println("<td align=center>" + request.getHeader(headers) + "</td></tr>");
                }
            }
            out.println("</TABLE><BR>");
            out.println(htmlFooter);
        } catch (Exception e) {

        }
    }

    public static DateTime getDateWithOffset(int offset) {

        Date today = new Date();
        Date theDate = new Date(today.getTime() + (offset * 86400000));
        return new DateTime(theDate, TimeZone.getTimeZone("UTC"));

    }

    public static DateTime getDateFromSimpleString(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        DateTime startDate = null;
        try {
            startDate = new DateTime(sdf.parse(dateString));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return startDate;

    }

    public static String getImageUrlFromBlobkey(String blobKeyRaw) {
        log.info("getImageUrlFromBlobkey(" + blobKeyRaw + ")");
        String url = new String();
        Pattern pattern = Pattern.compile(BLOBKEY_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(blobKeyRaw);
        // using Matcher find(), group(), start() and end() methods
        while (matcher.find()) {
            log.info("Found the text \"" + matcher.group() + "\" starting at " + matcher.start()
                    + " index and ending at index " + matcher.end());
        }
        return url;

    }

    public static Map<String, String> getHeadersInfo(HttpServletRequest request) {

        Map<String, String> map = new HashMap<String, String>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    public static HttpURLConnection getConnection(String protocol, String host, String path,
            String query) throws Exception {

        log.info("getConnection: " + protocol + "://" + host + path);
        String pathAndQuery = path + "?" + query;
        URI uri;
        URL url;
        HttpURLConnection conn;

        if (query == null) {
            uri = new URI(protocol, host, path, null);
            url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
        } else {

            String urlStr = protocol + "://" + host + "/" + path;
            java.net.URLConnection connection = new URL(urlStr + "?" + query).openConnection();
            conn = (HttpURLConnection) connection;
        }

        conn.setConnectTimeout(15000); // timeout after 15 seconds

        conn.setRequestProperty("Accept", "text/html, */*");

        conn.setRequestProperty("Host", host);

        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        return conn;
    }

  

    public static JSONObject getJsonBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        JSONObject retJson = (JSONObject) JSONSerializer.toJSON(body);
        return retJson;
    }

    /**
     * Sets the properties of the specified entity by the specified json object.
     * 
     * @param entity
     *            the specified entity
     * @param jsonObject
     *            the specified json object
     * @throws JSONException
     *             json exception
     */
    public static void setProperties(final Entity entity, final JSONObject jsonObject)
            throws JSONException {
        @SuppressWarnings("unchecked")
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = jsonObject.get(key);

            if (!GAE_SUPPORTED_TYPES.contains(value.getClass()) && !(value instanceof Blob)) {
                throw new RuntimeException("Unsupported type[class=" + value.getClass().getName()
                        + "]");
            }

            if (value instanceof String) {
                final String valueString = (String) value;
                if (valueString.length() > DataTypeUtils.MAX_STRING_PROPERTY_LENGTH) {
                    final Text text = new Text(valueString);

                    entity.setProperty(key, text);
                } else {
                    entity.setProperty(key, value);
                }
            } else if (value instanceof Number || value instanceof Date || value instanceof Boolean
                    || GAE_SUPPORTED_TYPES.contains(value.getClass())) {
                entity.setProperty(key, value);
            } else if (value instanceof Blob) {
                final Blob blob = (Blob) value;
                entity.setProperty(key,
                        new com.google.appengine.api.datastore.Blob(blob.getBytes()));
            }
        }
    }

    // convert an entity to jsonObject - very handy!
    public static JSONObject entity2JSONObject(final Entity entity) {
        final Map<String, Object> properties = entity.getProperties();
        final Map<String, Object> jsonMap = new HashMap<String, Object>();
        JSONObject retJson = new JSONObject();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            final String k = property.getKey();
            final Object v = property.getValue();
            if (v instanceof Text) {
                final Text valueText = (Text) v;
                retJson.put(k, valueText.getValue());
                jsonMap.put(k, valueText.getValue());
            } else if (v instanceof com.google.appengine.api.datastore.Blob) {
                final com.google.appengine.api.datastore.Blob blob = (com.google.appengine.api.datastore.Blob) v;
                retJson.put(k, new Blob(blob.getBytes()));
                jsonMap.put(k, new Blob(blob.getBytes()));
            } else {
                retJson.put(k, v);
                jsonMap.put(k, v);
            }
        }
        return retJson;
    }

};
