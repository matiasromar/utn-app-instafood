package com.utnapp.instafood.Api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.utnapp.instafood.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.RequestBody.create;

public class Api {
    private Context context;

    public Api(Context context) {
        this.context = context;
    }

    private void executeAsyncCall(Request request, Boolean authenticationRequired, final MyCallback callback) {
        OkHttpClient client = getOkHttpClient(authenticationRequired);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.unhandledError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    callback.success(response.body().toString());
                    response.body().close();
                } else{
                    callback.error(response.body().toString());
                    response.body().close();
                }
            }
        });
    }


    public void executeSyncCall(Request request, Boolean authenticationRequired, MyCallback callback) {
        OkHttpClient client = getOkHttpClient(authenticationRequired);

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            callback.unhandledError(e);
        }

        if(response.isSuccessful()){
            callback.success(response.body().toString());
            response.body().close();
        } else {
            callback.error(response.body().toString());
            response.body().close();
        }
    }

    @NonNull
    private OkHttpClient getOkHttpClient(Boolean authenthicationRequired) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if(authenthicationRequired){
            clientBuilder.authenticator(new MyAuthenticator(context));
        }

        return clientBuilder.build();
    }

    public Request getPostRequest(String relativeUrl, String jsonContent, boolean requireAuthentication) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = create(JSON, jsonContent);

        Request.Builder requestBuilder = new Request.Builder()
                .url(context.getString(R.string.api_uri) + relativeUrl)
                .header("Connection", "close")
                .post(body);

        if(requireAuthentication){
            //TODO - Cuando haya autenticacion
            //requestBuilder.header("Authorization", "Basic " + MySecurityManager.getCurrentUserToken(context));
        }

        return requestBuilder.build();
    }


    public Request getDeleteRequest(String jsonContent, String relativeUrl, Boolean authenthicationRequired) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = create(JSON, jsonContent);

        Request.Builder requestBuilder = new Request.Builder()
                .url(context.getString(R.string.api_uri) + relativeUrl)
                .header("Connection", "close")
                .delete(body);

        if(authenthicationRequired){
            //TODO - Cuando haya autenticacion
            //requestBuilder.header("Authorization", "Basic " + MySecurityManager.getCurrentUserToken(context));
        }

        return requestBuilder.build();
    }

    public Request getGetRequest(String relativeUrl, Boolean authenthicationRequired) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(context.getString(R.string.api_uri) + relativeUrl)
                .header("Connection", "close")
                .get();

        if(authenthicationRequired){
            //TODO - Cuando haya autenticacion
            //requestBuilder.header("Authorization", "Basic " + MySecurityManager.getCurrentUserToken(context));
        }

        return requestBuilder.build();
    }

    public Request getPutRequest(String jsonContent, String relativeUrl, Boolean authenthicationRequired) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = create(JSON, jsonContent);

        Request.Builder requestBuilder = new Request.Builder()
                .url(context.getString(R.string.api_uri) + relativeUrl)
                .header("Connection", "close")
                .put(body);

        if(authenthicationRequired){
            //TODO - Cuando haya autenticacion
            //requestBuilder.header("Authorization", "Basic " + MySecurityManager.getCurrentUserToken(context));
        }

        return requestBuilder.build();
    }
}
