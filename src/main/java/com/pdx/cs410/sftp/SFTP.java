package com.pdx.cs410.sftp;

import com.jcraft.jsch.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.processing.FilerException;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class SFTP {
    public static Scanner in = new Scanner(System.in);
    public static JSch client = new JSch();
    public static Session session = null;
    public static Channel channel = null;
    public static ChannelSftp channelSftp = null;
    public static String hostname = null;
    public static String username = null;
    public static String password = null;
    public static int PORT = 0;
    public static String saveInfo = null;
    public static String useSavedInfo = null;
    public static boolean isUsingSavedInfo = false;
    public static ArrayList<Profile> profiles;

    public static void main(String[] args){
            JSch.setConfig("StrictHostKeyChecking", "no");
            if(login()){
                new OptionWindow(in,client,session,channel,channelSftp).CaseWindow();
            }
            session.disconnect();
            return;
    }

    private static boolean login(){
        String answer;
        boolean lockDown = false;
        while(!lockDown){

            // check if a file with connection info exists
            if (loadExistingConnectionInfo()) {
                System.out.println("Would you like to use your saved connection info (" + username + "@" + hostname + ")? (y/n)");
                useSavedInfo = in.nextLine();

                // check if the user wants to use the saved info
                if (useSavedInfo.toLowerCase().equals("y")) {
                    isUsingSavedInfo = true;
                }
            }

            if (!isUsingSavedInfo) {
                System.out.print("Please enter a hostname: ");
                hostname = in.nextLine();
                System.out.print("Enter a port number: ");
                PORT = in.nextInt();
                in.nextLine();
                System.out.print("Enter your username: ");
                username = in.nextLine();
            }

            System.out.print("Enter your password: ");
            password = in.nextLine();

            try{
                session = client.getSession(username, hostname, PORT);
                session.setPassword(password);
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                lockDown = true;

                if (!isUsingSavedInfo) {
                    // ask if user wants to save this new connection info
                    System.out.println("Would you like to save your connection info? (y/n): ");
                    saveInfo = in.nextLine();

                    if (saveInfo.toLowerCase().equals("y")) {

                        // save the info
                        if (saveConnectionInfo()) {
                            System.out.println("Your connection info (" + username + "@" + hostname + ") was saved successfully.");
                        } else {
                            System.out.println("ERROR: Your connection info was not saved.");
                        }
                    }
                }

            }catch(JSchException e){
                System.out.println("You were not logged into " + hostname + "due to a login error.");
                System.out.print("Would you like to try again? (Y/N) ");
                answer = in.nextLine();
                if(answer.contentEquals("no"))
                    return lockDown;
            }
        }
        return lockDown;
    }

    // saves the connection info (hostname, PORT, and username) to a file
    // currently this will overwrite any existing file
    private static boolean saveConnectionInfo() {

        File file = new File("profiles.json");

        Profile newProfile = new Profile(hostname, PORT, username);
        profiles.add(newProfile);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);

            JSONArray array = new JSONArray();

            for(Profile profile : profiles) {
                JSONObject obj = new JSONObject();
                obj.put("hostname", profile.hostname);
                obj.put("port", profile.port);
                obj.put("name", profile.name);

                array.add(obj);
            }

            // write connection info to the file
            writer.write(array.toJSONString());
            writer.close();

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // checks if connection info already exists in a file, and if so it loads the info
    private static boolean loadExistingConnectionInfo() {

        File file = new File("profiles.json");

        if (file.exists()) {

            JSONParser parser = new JSONParser();
            profiles = new ArrayList<>();

            try {
                Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
                JSONArray array = (JSONArray) obj;

                for(int i = 0; i < array.size(); i++) {
                    JSONObject jsonProfile = (JSONObject) array.get(i);

                    String host = (String) jsonProfile.get("hostname");
                    String name = (String) jsonProfile.get("name");
                    int port = Integer.parseInt((jsonProfile.get("port")).toString());

                    hostname = host;
                    username = name;
                    PORT = port;

                    profiles.add(new Profile(hostname, port, name));
                }


            } catch (ParseException | IOException e) {
                e.printStackTrace();
                System.out.println("Failed to parse profiles file");
                return false;
            }

            return true;
        } else {
            profiles = new ArrayList<>();
        }
        return false;
    }

}
