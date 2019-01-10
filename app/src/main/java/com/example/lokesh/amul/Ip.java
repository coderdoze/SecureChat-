package com.example.lokesh.amul;

/**
 * Created by lokesh on 27/10/18.
 */

public class Ip {
    private String mName;
    private String mIp;

    Ip(String name,String ip){
        mName = name;
        mIp = ip;
    }
    public String getDevice(){
        return mName;
    }
    public String getIp(){
        return mIp;
    }
}
