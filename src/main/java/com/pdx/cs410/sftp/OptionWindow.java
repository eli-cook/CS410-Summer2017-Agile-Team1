package com.pdx.cs410.sftp;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Sean on 7/15/2017.
 */
public class OptionWindow {
    public static Scanner in = null;
    public static JSch client = null;
    public static Session session = null;
    public static Channel channel = null;
    public static ChannelSftp channelSftp = null;
    public static File myDirectory = null;
    public OptionWindow(Scanner in, JSch client, Session session, Channel channel, ChannelSftp channelSftp){
        this.in = in;
        this.client = client;
        this.session = session;
        this.channel = channel;
        this.channelSftp = channelSftp;
    }
    public void CaseWindow(){
        String command;
        myDirectory = new File(".");//Use myDirectory to traverse the local machines files
        while(true){
            System.out.print("$ ");
            command = null;
            command = in.nextLine();
            if(command.equals("ls")){
                ls();
            }
            else if (command.equals("cls")){
                cls();
            }
            else if (command.equals("logoff")){
                return;
            }
            else if (command.contains(" ")){
                String command2;
                command2 = command.substring(command.indexOf(" "));
                command = command.substring(0,command.indexOf(" "));
                if(command.equals("cd")){
                    cd(command2);
                }
                else if(command.equals("ccd")){
                    ccd(command2);
                }
                else if(command.equals("get")) {
                    get(command2);
                }
            }
        }
    }
    //Used to display the current directory of the server
    private static void ls(){
        try {
            int counter = 0;
            Vector filelist = channelSftp.ls(channelSftp.pwd());
            System.out.println("These are the files and directories currently in " + channelSftp.pwd() + ":");
            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                if (entry.getFilename().charAt(0) != '.') {
                    System.out.print(entry.getFilename() + '\t');
                    if(counter == 3) {
                        System.out.println("\n");
                        counter = 0;
                    }
                    else ++counter;
                }
            }
        }catch (SftpException e){
            e.printStackTrace();
        }
    }
    //Used to display the current directory of the local machine
    private static void cls(){
        int counter = 0;
        File directoryFiles [] = myDirectory.listFiles();
        try {
            System.out.println("Files within" + myDirectory.getCanonicalPath() + ":");
            for(File temp: directoryFiles){
                System.out.print(temp.getName()+ "\t");
                if(counter == 3) {
                    counter = 0;
                    System.out.println();
                }
                else
                    ++counter;
            }
        }catch(IOException e){
            System.out.println("Could not print your current directory.");
        }
        System.out.println();
    }
    //Used to traverse to a different directory on the server
    private static void cd(String toFind){

    }
    //Used to traverse to a different directory on the local machine
    private static void ccd(String toFind){

    }

    private static void get (String toFind) {
        String path = myDirectory.getAbsolutePath();
        try {
            channelSftp.get(toFind, path);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}
