package com.motorola.motoask;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;


/**
 * RestRequest - This object enables us to use RESTful routes in our application.  
 * @author jthomps1
 * 
 * @param pathInfo - this is the path of the incoming request.  it has the info we need
 * @param resourcePattern - string uses as a regexp pattern to find what resource is being requested
 *          example: "/(session|orders)$"  - this would support resources 'session' and 'orders'
 * @param resourceWithIdPattern - used to to see if we are adding an ID to the reqest
 *          example "/(session|orders)/([A-Za-z0-9]*)" looks for <host>/session/KEY
 */
public class RestRequest {

    // Accommodate two requests, one for all resources, another for a specific resource
    private String id;

    private String resource;

    private Pattern regExAllPattern;

    private Pattern regExIdPattern;

    private static final Logger log = Logger.getLogger(RestRequest.class.getName());

    public RestRequest(String pathInfo, String resourcePattern,  String resourceWithIdPattern) 
            throws ServletException {

        Matcher matcher;
        regExAllPattern = Pattern.compile( resourcePattern);
        regExIdPattern = Pattern.compile(resourceWithIdPattern);

        // Check for ID case first, since the All pattern would also match
        matcher = regExIdPattern.matcher(pathInfo);
        if (matcher.find()) {
            resource = matcher.group(1);
            id = matcher.group(2);
            log.info("resource: " + resource);
            return;
        }

        matcher = regExAllPattern.matcher(pathInfo);
        if (matcher.find()) {
            resource = matcher.group(1);
            return;
        }

        throw new ServletException("Invalid URI");
    }

    public String getResource() {
        return resource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}