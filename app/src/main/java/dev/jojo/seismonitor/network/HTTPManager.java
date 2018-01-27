package dev.jojo.seismonitor.network;

import java.io.IOException;
import java.util.List;

import dev.jojo.seismonitor.objects.NotificationQuake;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by myxroft on 27/01/2018.
 */

public class HTTPManager {

    private String HTTPUrl;
    private List<NotificationQuake> HTTPParams;

    public HTTPManager(String url, List<NotificationQuake> fields){
        this.HTTPUrl = url;
        this.HTTPParams = fields;
    }

    public String performRequest(){

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("somParam", "someValue")
                .build();

        Request request = new Request.Builder()
                .url("")
                .post(requestBody)
                .build();

        request.body();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
