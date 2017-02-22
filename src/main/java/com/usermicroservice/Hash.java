package com.usermicroservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by dani on 2017-02-22.
 */
public class Hash {
    public static String BcryptEncrypt(String string){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(string);
    }
}
