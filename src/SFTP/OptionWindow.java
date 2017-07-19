import com.jcraft.jsch.*;

import java.util.Scanner;
import java.util.Vector;
import java.io.File;

/**
 * Created by Sean on 7/15/2017.
 */
public class OptionWindow {
    public static Scanner in = null;
    public static JSch client = null;
    public static Session session = null;
    public static Channel channel = null;
    public static ChannelSftp channelSftp = null;
    public OptionWindow(Scanner in, JSch client, Session session, Channel channel, ChannelSftp channelSftp){
        this.in = in;
        this.client = client;
        this.session = session;
        this.channel = channel;
        this.channelSftp = channelSftp;
    }
    public void CaseWindow(){
        String command;
        while(true){
            System.out.print("$ ");
            command = null;
            command = in.nextLine();
            if(command.equals("ls")){
                ls();
            }
            else if (command.equals("mkdir")) {
                mkdir();
            }
            else if (command.equals("logoff")){
                return;
            }
        }
    }

    private static void upload(){
        String target = null;
        System.out.println("Select a local file to upload");
    }

    private static void download(){

    }

    private static void lslocal(){
        //https://stackoverflow.com/questions/15598657/how-to-use-ls-c-command-in-java
        //https://docs.oracle.com/javase/7/docs/api/java/io/File.html

    }

    // How to handle overwriting an existing directory: Current solution is to output an error message.
    // How to handle invalid directory names: Same approach as above.
    private static void mkdir(){
        String newdir = null;
        System.out.println("Enter the name of the new directory:");
        newdir = in.nextLine();

        // Check for invalid characters before building the File object using a blacklist.
        // indexOf will return -1 if the input does not exist in the string array.
        if(newdir.indexOf("<") != - 1 || newdir.indexOf(">") != -1 || newdir.indexOf("%") != -1 || newdir.indexOf(":") != -1
                || newdir.contentEquals(".") || newdir.contentEquals("..")){
            System.out.println("Invalid characters are in your new directory name, directory creation aborted.");
            return;
        }

        File check = new File(newdir);

        if(check.exists()){
            System.out.println("That directory already exists.");
            return;
        }
        else {
            try {
                channelSftp.mkdir(newdir);
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }
    }

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
}
