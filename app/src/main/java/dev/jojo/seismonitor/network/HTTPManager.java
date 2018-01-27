package dev.jojo.seismonitor.network;

import java.io.IOException;
import java.util.List;

import dev.jojo.seismonitor.objects.HTTPRequestObject;
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
    private List<HTTPRequestObject> HTTPParams;

    public HTTPManager(String url, List<HTTPRequestObject> fields){
        this.HTTPUrl = url;
        this.HTTPParams = fields;
    }

    public String performRequest(){

        int param_len = HTTPParams.size();

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder mb = new MultipartBody.Builder();

        mb.setType(MultipartBody.FORM);

        for(int i=0;i<param_len;i++){
            mb.addFormDataPart(HTTPParams.get(i).PARAM,HTTPParams.get(i).VALUE);
        }

        RequestBody requestBody = mb.build();

        Request request = new Request.Builder()
                .url(this.HTTPUrl)
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
