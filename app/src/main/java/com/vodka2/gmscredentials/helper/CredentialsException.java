package com.vodka2.gmscredentials.helper;

public class CredentialsException extends Exception {
    CredentialsException(String str, Throwable cause){
        super(str, cause);
    }

    CredentialsException(String str){
        super(str);
    }
}
