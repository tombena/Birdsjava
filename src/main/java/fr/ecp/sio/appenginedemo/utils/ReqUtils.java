package fr.ecp.sio.appenginedemo.utils;

import fr.ecp.sio.appenginedemo.api.ApiException;
import fr.ecp.sio.appenginedemo.api.JsonServlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by tomb on 14/12/15.
 */
public class ReqUtils {

    // give {id} from /users/{id} or /users/{id}/*
    public static long reqId(HttpServletRequest req){
        String pathInfo=req.getPathInfo();
        String current_id = pathInfo.replace("/", "");
        if (current_id.equals("me")){
            return Long.MAX_VALUE; // represents "me", id is not attributed to anyone
        } else {
            current_id= current_id.replaceAll("[^\\d.]", ""); //remove all letters
            return Long.parseLong(current_id);
        }
    }



}
