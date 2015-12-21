package fr.ecp.sio.appenginedemo.api;

import fr.ecp.sio.appenginedemo.data.MessagesRepository;
import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.ReqUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * A servlet to handle all the requests on a specific user
 * All requests with path matching "/users/*" where * is the id of the user are handled here.
 */
public class UserServlet extends JsonServlet {

    private static final Logger LOG = Logger.getLogger(UserServlet.class.getSimpleName());

    // A GET request should simply return the user
    @Override
    protected User doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Extract the id of the user from the last part of the path of the request
        // TODO: Check if this id is syntactically correct

        long id = ReqUtils.reqId(req);
        if (id == Long.MAX_VALUE){ // id is "me"
            return getAuthenticatedUser(req); //Authenticate user
        }
        else{
            return UsersRepository.getUser(id);
        }


        // TODO: Not found?
        // TODO: Add some mechanism to hide private info about a user (email) except if he is the caller

    }

    // A POST request could be used by a user to edit its own account
    // It could also handle relationship parameters like "followed=true"
    @Override
    protected User doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Get the user as below
        // TODO: Apply some changes on the user (after checking for the connected user)

        //Relationship modification
        String follow = req.getParameter("follow");
        long id_url=ReqUtils.reqId(req);

        //Authenticate user
        if(getAuthenticatedUser(req)==null){
            throw new ApiException(401, "invalidToken", "User not authorized, token is invalid");
        }

        if(id_url==Long.MAX_VALUE || getAuthenticatedUser(req).id==id_url){
            throw new ApiException(401, "invalidFollow", "You can't follow nor unfollow yourself");
        }

        if (follow!=null) {
            UsersRepository.setUserFollowed(getAuthenticatedUser(req).id, id_url, Boolean.parseBoolean(follow));
        }

        // TODO: Handle special parameters like "followed=true" to create or destroy relationships
        // TODO: Return the modified user

        return getAuthenticatedUser(req);
    }

    // A user can DELETE its own account
    @Override
    protected Void doDelete(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Security checks
        // TODO: Delete the user, the messages, the relationships

        //Authenticate user
        if(getAuthenticatedUser(req)==null){
            throw new ApiException(401, "invalidToken", "User not authorized, token is invalid");
        } else{//make sure user wants to delete his account and not somebody else's account
            if(ReqUtils.reqId(req)!=Long.MAX_VALUE){
                throw new ApiException(401, "wrongUser", "User not authorized to delete this account, try /users/me");
            }
        }


        // A DELETE request shall not have a response body
        return null;
    }

}