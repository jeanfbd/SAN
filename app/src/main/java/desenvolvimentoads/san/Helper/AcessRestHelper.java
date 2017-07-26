package desenvolvimentoads.san.Helper;

import android.os.StrictMode;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by jeanf on 25/07/2017.
 */

public class AcessRestHelper {
    private int TIMEOUT_MILLISEC = 3000;

    public String Get(String uri){

        HttpClient httpClient = new DefaultHttpClient();

        HttpGet callGet = new HttpGet(uri);
        String callReturn = "";

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(callGet, responseHandler);

            callReturn = responseBody;


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return callReturn;
    }

}
