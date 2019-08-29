package com.cloud.server.protocol;

import java.util.HashMap;

public class LoginMap {

    static HashMap<String, String> user = new HashMap<>(5);

    public static HashMap<String, String> getUser() {
        return user;
    }

    static {
        user.put("login1", "password1");
        user.put("login2", "password2");
        user.put("login3", "password3");
        user.put("login4", "password4");
        user.put("login5", "password5");
    }

}
