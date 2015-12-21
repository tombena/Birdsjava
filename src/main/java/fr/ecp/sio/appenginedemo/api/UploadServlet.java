package fr.ecp.sio.appenginedemo.api;


import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.cloudstorage.*;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.ByteBuffer;
import fr.ecp.sio.appenginedemo.model.User;

/**
 * Created by Tom on 16/12/15.
 */
public class UploadServlet extends JsonServlet {


    @Override
    protected String doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        //Authenticate user
        User user=getAuthenticatedUser(req);
        String user_id = Long.toString(user.id); // constant unique number that describes user

        //Obtain file from request
        InputStream input = req.getInputStream();
        byte[] buffer = new byte[input.available()];
        int n = - 1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ( (n = input.read(buffer)) != -1) //stop when end of stream reached
        {
            output.write(buffer, 0, n);
        }
        output.close();

        //Store file to GCS
        GcsFilename fileName = new GcsFilename("bird-avatars", user_id); // everybody should be able to access it
        GcsFileOptions fileOptions= new GcsFileOptions.Builder()
                .acl("public-read")
                .mimeType("image/jpeg")
                .build();

        GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

        @SuppressWarnings("resource")
        GcsOutputChannel outputChannel =
                gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
        outputChannel.write(ByteBuffer.wrap(buffer));
        outputChannel.close();


        String urlImage = imagesService.getServingUrl(ServingUrlOptions.Builder
                .withGoogleStorageFileName("/gs/bird-avatars/" + user_id));

        //Attach file's URL to user
        user.avatar=urlImage;

        // Save it
        ObjectifyService.ofy()
                .save()
                .entity(user)
                .now()
                .getId();

        return urlImage;
    }
}