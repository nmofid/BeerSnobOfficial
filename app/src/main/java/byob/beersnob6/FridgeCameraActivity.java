package byob.beersnob6;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/*
 *  Activity to show the last image taken of the fridge contents.
 *
 *  Uses Amazon AWS CLI Android SDK to fetch a pre-signed URL of the latest fridge contents.
 *  This URL is then used to display the fridge contents image.
 */

public class FridgeCameraActivity extends Activity {
    private Bitmap imageBitmap;
    private String imageURL;
    private String bucketName, fileName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridgecamera);

        bucketName = "iotminifridge-userfiles-mobilehub-456530050";
        fileName = "public/image.jpg";

        performNetworkTask networkTask = new performNetworkTask();
        try {
            String result = (String) (networkTask.execute().get());
        }
        catch(Exception e){
            Log.d("ProcessFinish", "Fetching has finished or was cancelled");
        }
        Log.d("ProcessFinish", "FINISHED!");
        Log.d("ProcessFinish", "My URL : "+imageURL);
        if(imageURL != null) setContentView(new myView(this, imageURL));
        else Log.d("ProcessFinish", "URL was NULL!");
    }

    /*
     * Performs the network tasks necessary on a separate thread from the UI thread.
     */
    class performNetworkTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            imageURL = getS3ImageURLBitmap();
            Log.d("ProcessFinish", "My imageURL : "+imageURL.toString());
            cancel(true);
            return null;
        }

        @Override
        protected void onPostExecute(Object result){
            Log.d("ProcessFinish", "POST EXECUTE");
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            setContentView(R.layout.fridgecamera);
        }
    }

    /*
     *  Function to get pre-signed URL of fridge content image in Amazon S3 using Amazon AWS CLI.
     *  Also sets the image's bitmap using the pre-signed URL.
     */
    public String getS3ImageURLBitmap() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 1);
        Date expiration = cal.getTime();

        AmazonS3 s3 = new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider());
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, fileName);
            generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            generatePresignedUrlRequest.setExpiration(expiration);

            URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);

            Log.d("ProcessFinish", "My URL : "+url.toString());

            try {
                InputStream is = (InputStream) new URL(url.toString()).getContent();
                imageBitmap = BitmapFactory.decodeStream(is);
            }
            catch(Exception e){
                Log.d("ProcessFinish", "ERROR LOADING IMAGE URL BITMAP : "+e.toString());
            }

            return url.toString();
        } catch (AmazonServiceException exception) {
            Log.d("ProcessFinish", "Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            Log.d("ProcessFinish", "Error Message: " + exception.getMessage());
            Log.d("ProcessFinish", "HTTP  Code: "    + exception.getStatusCode());
            Log.d("ProcessFinish", "AWS Error Code:" + exception.getErrorCode());
            Log.d("ProcessFinish", "Error Type:    " + exception.getErrorType());
            Log.d("ProcessFinish", "Request ID:    " + exception.getRequestId());
        } catch (AmazonClientException ace) {
            Log.d("ProcessFinish", "Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            Log.d("ProcessFinish", "Error Message: " + ace.getMessage());
        }
        return null;
    }

    /*
     * myView Class - Takes Bitmap of the fridge content image and draws it to the screen.
     *                If there was an error with the URL, draws the error image.
     */
    private class myView extends View {
        private String url;

        public myView(Context c, String myUrl){
            super(c);
            url = myUrl;

        }

        @Override
        protected void onDraw(Canvas canvas){
            Bitmap b;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = (int) ((double) displayMetrics.heightPixels - 0.05 * displayMetrics.heightPixels);
            int width = (int) ((double) displayMetrics.widthPixels - 0.02 * displayMetrics.widthPixels);
            try {
                Bitmap tryout = imageBitmap;
                b = Bitmap.createScaledBitmap(tryout, width, height, false);
            } catch (Exception e) {
                Log.d("ProcessFinish", "Exception: "+e.toString());
                Bitmap tryout = BitmapFactory.decodeResource(getResources(), R.drawable.fridgecamfetcherror);
                b = Bitmap.createScaledBitmap(tryout, width, height, false);
            }
            int startHeight = (int) ((double) displayMetrics.heightPixels * 0.01);
            int startWidth = (int) ((double) displayMetrics.widthPixels * 0.01);
            canvas.drawBitmap(b, startWidth, startHeight, null);
        }

    }
}
