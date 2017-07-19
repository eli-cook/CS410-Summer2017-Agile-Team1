

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
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
        myDirectory = new File(".");
        while(true){
            System.out.print("$ ");
            command = null;
            command = in.nextLine();
            if(command.equals("sls")){
                sls();
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
                if(command.equals("scd")){
                    scd(command2);
                }
                else if(command.equals("ccd")){
                    ccd(command2);
                }
            }
        }
    }
    //Used to display the current directory of the server
    private static void sls(){
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
        try {
            System.out.println(myDirectory.getCanonicalPath());
        }catch(IOException e){
            System.out.println("Could not print your current directory.");
        }
    }
    //Used to traverse to a different directory on the server
    private static void scd(String toFind){

    }
    //Used to traverse to a different directory on the local machine
    private static void ccd(String toFind){

    }
}
