package com.utnapp.instafood.Api;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class MyAuthenticator implements Authenticator {
    public MyAuthenticator(Object p0) {
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        //TODO - Cuando tengamos autenticacion
        return null;
    }
}
