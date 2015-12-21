package fr.ecp.sio.appenginedemo.api;

import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.MD5Utils;
import fr.ecp.sio.appenginedemo.utils.TokenUtils;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * A servlet to handle all the requests on a list of users
 * All requests on the exact path "/users" are handled here.
 */
public class UsersServlet extends JsonServlet {

    // A GET request should return a list of users
    @Override
    protected UsersRepository.UsersList doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: define parameters to search/filter users by login, with limit, order...
        // TODO: define parameters to get the followings and the followers of a user given its id

        int limit =10;
        String nFollowed = req.getParameter("followedBy"); //followed
        String nFollowers = req.getParameter("followerOf"); //followers

        if (nFollowed!=null){
            if(nFollowed.equals("me")){ //ppl who follow the current user
                return UsersRepository.getUserFollowers(getAuthenticatedUser(req).id);
            } else {
                return UsersRepository.getUserFollowers(Long.parseLong(nFollowed));
            }

        } else if (nFollowers!=null){
            if(nFollowers.equals("me")){ //ppl who follow the current user
                return UsersRepository.getUserFollowed(getAuthenticatedUser(req).id, limit);
            } else {
                return UsersRepository.getUserFollowed(Long.parseLong(nFollowers), limit);
            }
        }


        // if URL is only /users
        return UsersRepository.getUsers();


    }

    // A POST request can be used to create a user
    // We can use it as a "register" endpoint; in this case we return a token to the client.
    @Override
    protected String doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        // The request should be a JSON object describing a new user
        User user = getJsonRequestBody(req, User.class);
        if (user == null) {
            throw new ApiException(400, "invalidRequest", "Invalid JSON body");
        }

        // Perform all the usul checkings
        if (!ValidationUtils.validateLogin(user.login)) {
            throw new ApiException(400, "invalidLogin", "Login did not match the specs");
        }
        if (!ValidationUtils.validatePassword(user.password)) {
            throw new ApiException(400, "invalidPassword", "Password did not match the specs");
        }
        if (!ValidationUtils.validateEmail(user.email)) {
            throw new ApiException(400, "invalidEmail", "Invalid email");
        }

        if (UsersRepository.getUserByLogin(user.login) != null) {
            throw new ApiException(400, "duplicateLogin", "Duplicate login");
        }
        if (UsersRepository.getUserByEmail(user.email) != null) {
            throw new ApiException(400, "duplicateEmail", "Duplicate email");
        }

        // Explicitly give a fresh id to the user (we need it for next step)
        user.id = UsersRepository.allocateNewId();

        // TODO: find a solution to receive an store profile pictures
        // Simulate an avatar image using Gravatar API
        user.avatar = "http://www.gravatar.com/avatar/" + MD5Utils.md5Hex(user.email) + "?d=wavatar";

        // Hash the user password with the id a a salt
        user.password = DigestUtils.sha256Hex(user.password + user.id);

        // Persist the user into the repository
        UsersRepository.saveUser(user);

        // Create and return a token for the new user
        return TokenUtils.generateToken(user.id);

    }

}
