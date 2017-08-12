package com.pdx.cs410.sftp;

/**
 * Created by Eli on 8/12/2017.
 */
public class Profile {
    String hostname;
    int port;
    String name;

    public Profile(String hostname, int port, String name) {
        this.hostname = hostname;
        this.port = port;
        this.name = name;
    }
}
