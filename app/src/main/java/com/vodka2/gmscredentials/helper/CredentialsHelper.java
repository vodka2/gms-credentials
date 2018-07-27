package com.vodka2.gmscredentials.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Scanner;

public class CredentialsHelper {
    private XmlPullParser _xpp;
    public CredentialsHelper() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
         _xpp = factory.newPullParser();
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String catFile(String file) throws CredentialsException {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outStrm = process.getOutputStream();
            outStrm.write(("cat " + file).getBytes());
            outStrm.close();
            InputStream errStrm = process.getErrorStream();
            InputStream inStrm = process.getInputStream();
            process.waitFor();
            if(process.exitValue() != 0){
                String errRes = convertStreamToString(errStrm);
                throw new CredentialsException("Got " + process.exitValue() + " code and " + errRes + " when reading " + file);
            }
            String res = convertStreamToString(inStrm);
            inStrm.close();
            errStrm.close();
            return res;
        } catch (IOException e) {
            throw new CredentialsException("Can't read file " + file, e);
        } catch (InterruptedException e) {
            throw new CredentialsException("Wait for file " + file + " was interrupted", e);
        }
    }

    @SuppressLint("SdCardPath")
    private String parseToken() throws CredentialsException {
        try {
            _xpp.setInput(new StringReader( catFile("/data/data/com.google.android.gsf/shared_prefs/CheckinService.xml") ) );
            int eventType = _xpp.getEventType();
            String token = null;
            boolean tokenFound = false;
            while (!tokenFound && eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG){
                    boolean thisTag = false;
                    boolean valueFound = false;
                    String value = null;
                    for(int i = 0; i < _xpp.getAttributeCount(); i++){
                        if(_xpp.getAttributeName(i).equals("value")){
                            value = _xpp.getAttributeValue(i);
                            valueFound = true;
                        }
                        if(_xpp.getAttributeName(i).equals("name") && _xpp.getAttributeValue(i).equals("CheckinTask_securityToken")){
                            thisTag = true;
                        }
                    }
                    if(thisTag){
                        if(valueFound) {
                            tokenFound = true;
                            token = value;
                        } else {
                            throw new CredentialsException("Tag without a value found when retrieving token");
                        }
                    }
                }
                eventType = _xpp.next();
            }
            if(!tokenFound){
                throw new CredentialsException("Token was not found");
            }
            return token;
        } catch (XmlPullParserException e) {
            throw new CredentialsException("Error parsing xml when retrieving token", e);
        } catch (IOException e) {
            throw new CredentialsException("Error when retrieving token", e);
        }
    }

    @SuppressLint("SdCardPath")
    private String parseId() throws CredentialsException {
        try {
            _xpp.setInput(new StringReader(catFile("/data/data/com.google.android.gms/shared_prefs/Checkin.xml")));
            int eventType = _xpp.getEventType();
            String id = null;
            boolean idFound = false;
            while (!idFound && eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG){
                    for(int i = 0; i < _xpp.getAttributeCount(); i++){
                        if(_xpp.getAttributeName(i).equals("name") && _xpp.getAttributeValue(i).equals("android_id")){
                            idFound = true;
                            eventType = _xpp.next();
                            if(eventType == XmlPullParser.TEXT){
                                id = _xpp.getText();
                                break;
                            } else {
                                throw new CredentialsException("Expected text when retrieving id");
                            }
                        }
                    }
                }
                eventType = _xpp.next();
            }
            if(!idFound){
                throw new CredentialsException("Id was not found");
            }
            return id;
        } catch (XmlPullParserException e) {
            throw new CredentialsException("Error parsing xml when retrieving id", e);
        } catch (IOException e) {
            throw new CredentialsException("Error when retrieving id", e);
        }
    }

    public Credentials getCredentials() throws CredentialsException {
        return new Credentials(parseId(), parseToken());
    }
}
