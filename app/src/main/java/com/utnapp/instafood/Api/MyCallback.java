package com.utnapp.instafood.Api;

public interface MyCallback {
    void success(String responseBody);

    void error(String responseBody);

    void unhandledError(Exception e);
}
