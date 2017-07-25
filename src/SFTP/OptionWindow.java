

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

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
<<<<<<< HEAD
            else if (command.equals("cls")){
                cls();
            }
            else if(command.equals("mkdir")){
                mkdir();
            }
            else if(command.startsWith("mkdir ") && (command.length() > 6))
            {
                mkdir(command.substring(6));
            }
=======
            else if (command.equals("mkdir")) {
                mkdir();
            }
>>>>>>> d160cf436161079286b9b2f774f3064450e7f923
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
            }
        }
    }

<<<<<<< HEAD
    //Used to display the current directory of the server
=======
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

>>>>>>> d160cf436161079286b9b2f774f3064450e7f923
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

    // Used to create a directory within the current directory.
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

        try{
            channelSftp.lstat(newdir);
        }
        catch(SftpException e) {
            try {
                channelSftp.mkdir(newdir);
            } catch (SftpException f) {
                if(f.id == SSH_FX_NO_SUCH_FILE) {
                    System.out.println("The directory referenced does not exist.");
                    return;
                }

                // Otherwise something else happened.
                f.printStackTrace();
            }

            return;
        }

        // Otherwise the directory or file already exists.
        System.out.println("That directory already exists.");
        return;

    }

    // mkdir variation that accepts a string in the same manner as the mkdir linux command.
    public static void mkdir(String newdir)
    {
        // Check for invalid characters before building the File object using a blacklist.
        // indexOf will return -1 if the input does not exist in the string array.
        if(newdir.indexOf("<") != - 1 || newdir.indexOf(">") != -1 || newdir.indexOf("%") != -1 || newdir.indexOf(":") != -1
                || newdir.contentEquals(".") || newdir.contentEquals("..")){
            System.out.println("Invalid characters are in your new directory name, directory creation aborted.");
            return;
        }

        try{
            channelSftp.lstat(newdir);
        }
        catch(SftpException e) {
            try {
                channelSftp.mkdir(newdir);
            } catch (SftpException f) {
                if (f.id == SSH_FX_NO_SUCH_FILE) {
                    System.out.println("The directory referenced does not exist.");
                    return;
                }

                // Otherwise something else happened.
                f.printStackTrace();
            }

            return;
        }

        // Otherwise the directory or file already exists.
        System.out.println("That directory already exists.");
        return;
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
}
