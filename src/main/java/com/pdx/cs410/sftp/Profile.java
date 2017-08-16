//CS410-Summer2017-Agile-Team1
package com.pdx.cs410.sftp;

public class Profile {
    String hostname;
    int port;
    String name;

    public Profile(String hostname, int port, String name) {
        this.hostname = hostname;
        this.port = port;
        this.name = name;
    }

    public boolean isSameAs(Profile otherProfile) {
        return hostname.equals(otherProfile.hostname) && port == otherProfile.port && name.equals(otherProfile.name);
    }
}
