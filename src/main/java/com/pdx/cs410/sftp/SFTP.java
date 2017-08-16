//CS410-Summer2017-Agile-Team1
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

import static java.lang.Integer.parseInt;

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

            // check if a file with connection info exists and load in the info if so
            if (loadExistingConnectionInfo()) {

                // ask the user which profile they would like to select (if there are any)
                if (selectProfile()) {
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
                            System.out.println("Your connection info (" + username + "@" + hostname + ":" + PORT + ") was saved successfully.");
                        }
                    }
                }

            } catch(JSchException e){
                System.out.println("You were not logged into " + hostname + " due to a login error.");
                System.out.print("Would you like to try again? (Y/N) ");
                answer = in.nextLine();
                if(answer.contentEquals("no"))
                    return lockDown;
            }
        }
        return lockDown;
    }

    // saves the connection info (hostname, PORT, and username) to a file
    private static boolean saveConnectionInfo() {

        File file = new File("profiles.json");

        Profile newProfile = new Profile(hostname, PORT, username);

        // check if the user's new info already exists within the profiles, if so alert the user and abort
        for (Profile profile : profiles) {
            if (profile.isSameAs(newProfile)) {
                System.out.println("This connection info has already saved.");
                return false;
            }
        }

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
                    int port = parseInt((jsonProfile.get("port")).toString());

                    //hostname = host;
                    //username = name;
                    //PORT = port;

                    profiles.add(new Profile(host, port, name));
                }


            } catch (ParseException | IOException e) {
                e.printStackTrace();
                System.out.println("Failed to parse the profiles file: profiles.json");
                return false;
            }

            return true;

        } else {
            profiles = new ArrayList<>();
        }

        return false;
    }

    // has the user select one of the loaded profiles and then sets the connection info to it
    private static boolean selectProfile() {

        int selection = 0;

        // verify that we have at least one profile loaded
        if (profiles.size() < 1) {
            return false;
        }

        System.out.println("Would you like to use one of the following saved profiles? Please enter the corresponding option:");

        System.out.println("0: None.");

        for (int i = 0; i < profiles.size(); i++) {
            Profile currProfile = profiles.get(i);
            System.out.println((i + 1) + ": " + currProfile.name + "@" + currProfile.hostname + ":" + currProfile.port);
        }

        selection = parseInt(in.nextLine());

        // handle the user's selection and load a profile if one was selected
        if (selection > 0 && selection <= profiles.size()) {

            // load the selected profile into our connection info
            Profile selectedProfile = profiles.get(selection - 1);
            hostname = selectedProfile.hostname;
            username = selectedProfile.name;
            PORT = selectedProfile.port;

            return true;

        } else {
            return false;
        }

    }

}
