package com.pdx.cs410.sftp;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public OptionWindow(Scanner in, JSch client, Session session, Channel channel, ChannelSftp channelSftp) {
        this.in = in;
        this.client = client;
        this.session = session;
        this.channel = channel;
        this.channelSftp = channelSftp;
    }

    public void CaseWindow() {
        String command;
        myDirectory = new File(".");//Use myDirectory to traverse the local machines files
        while (true) {
            System.out.print("$ ");
            command = null;
            command = in.nextLine();
            if (command.equals("ls")) {
                ls();
            } else if (command.equals("cls")) {
                cls();
            } else if (command.startsWith("mkdir ") && (command.length() > 6)) {
                mkdir(command.substring(6));
            } else if (command.equals("mkdir")) {
                mkdir();
            } else if (command.equals("logoff")) {
                return;
            } else if (command.contains(" ")) {
                String command2;
                command2 = command.substring(command.indexOf(" ")+1);
                command = command.substring(0, command.indexOf(" "));
                if (command.equals("cd")) {
                    cd(command2);
                } else if (command.equals("ccd")) {
                    ccd(command2);
                } else if (command.equals("get")) {
                    get(command2);
                } else if (command.equals("put")) {
                    put(command2);
                } else if(command.equals("rm")) {
                    rm(command2);
                } else if (command.equals("rename")){
                    rename(command2);
                } else if (command.equals("crename")){
                    crename(command2);
                }
            }
        }
    }

    //Used to display the current directory of the server
    private static void ls() {
        try {
            int counter = 0;
            Vector filelist = channelSftp.ls(channelSftp.pwd());
            System.out.println("These are the files and directories currently in " + channelSftp.pwd() + ":");
            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                if (entry.getFilename().charAt(0) != '.') {
                    System.out.print(entry.getFilename() + '\t');
                    if (counter == 3) {
                        System.out.println("\n");
                        counter = 0;
                    } else ++counter;
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    //Used to display the current directory of the local machine
    private static void cls() {
        int counter = 0;
        File directoryFiles[] = myDirectory.listFiles();
        try {
            System.out.println("Files within " + myDirectory.getCanonicalPath() + ":");
            for (File temp : directoryFiles) {
                System.out.print(temp.getName() + "\t");
                if (counter == 3) {
                    counter = 0;
                    System.out.println();
                } else
                    ++counter;
            }
        } catch (IOException e) {
            System.out.println("Could not print your current directory.");
        }
        System.out.println();
    }

    //Used to traverse to a different directory on the server
    private static void cd(String toFind) {
        try {
            channelSftp.cd(toFind.trim());
        }
        catch(SftpException e){
            System.out.println("cd failed: " + e.toString());
        }
    }

    //Used to traverse to a different directory on the local machine
    private static void ccd(String toFind) {
        if(toFind.equals("..")){
            try {
                myDirectory = new File(myDirectory.getCanonicalPath());
                myDirectory = new File(myDirectory.getParent());
                return;
            }catch(IOException e){}
        }
        Path myDirectoryPath = myDirectory.toPath();
        Path toFindPath = Paths.get(toFind.trim());

        if(toFindPath.isAbsolute()){
            myDirectoryPath = toFindPath;
        }
        else{
            myDirectoryPath = myDirectoryPath.resolve(toFindPath);
        }

        try{
            myDirectoryPath.toRealPath();
        }
        catch(IOException e) {
            System.out.println("IOException: " + e.toString());
        }
        catch(SecurityException e) {
            System.out.println("SecurityException: " + e.toString());
        }

        File temp = myDirectoryPath.toFile();

        if(temp.isDirectory()){
            myDirectory = temp;
        }
        else {
            System.out.println("Cannot find directory:" + myDirectoryPath.toString());
        }
    }
    //Used to retrieve a file from remote server and put it on local machine
    private static void get(String toFind) {
        String path = myDirectory.getAbsolutePath();
        try {
            channelSftp.get(toFind, path);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
   //Puts a file onto the server
    private static void put(String toFind) {
        String path = null;
        try {
            path = channelSftp.pwd();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        try {
            channelSftp.put(myDirectory.getAbsolutePath()+"\\"+toFind, path, toFind.length());
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
    // Used to create a directory within the current directory.
    private static void mkdir() {
        String newdir = null;
        System.out.println("Enter the name of the new directory:");
        newdir = in.nextLine();

        // Check for invalid characters before building the File object using a blacklist.
        // indexOf will return -1 if the input does not exist in the string array.
        if (newdir.indexOf("<") != -1 || newdir.indexOf(">") != -1 || newdir.indexOf("%") != -1 || newdir.indexOf(":") != -1
                || newdir.contentEquals(".") || newdir.contentEquals("..")) {
            System.out.println("Invalid characters are in your new directory name, directory creation aborted.");
            return;
        }

        try {
            channelSftp.lstat(newdir);
        } catch (SftpException e) {
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

    // mkdir variation that accepts a string in the same manner as the mkdir linux command.
    public static void mkdir(String newdir) {
        // Check for invalid characters before building the File object using a blacklist.
        // indexOf will return -1 if the input does not exist in the string array.
        if (newdir.indexOf("<") != -1 || newdir.indexOf(">") != -1 || newdir.indexOf("%") != -1 || newdir.indexOf(":") != -1
                || newdir.contentEquals(".") || newdir.contentEquals("..")) {
            System.out.println("Invalid characters are in your new directory name, directory creation aborted.");
            return;
        }

        try {
            channelSftp.lstat(newdir);
        } catch (SftpException e) {
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


    // used to remove a single file or directory from the remote server
    private static void rm (String target) {

        boolean isDir = false;

        // strip any whitespace from the target String (if remove multiple is included, this will need to be removed)
        target = target.replaceAll("\\s", "");

        try {
            isDir = channelSftp.stat(target).isDir();

            if (isDir) {
                channelSftp.rmdir(target);
            } else {
                channelSftp.rm(target);
            }

        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
    
    // rename a file on the server side
    public static void rename(String FileNames) {
        String newName;
        if(!FileNames.contains(" ")){
            System.out.println("Command not of the correct form.");
            return;
        }
        newName = FileNames.substring(FileNames.indexOf(" ")+1);
        FileNames = FileNames.substring(0, FileNames.indexOf(" "));
        try{
            channelSftp.lstat(FileNames);
        }catch(SftpException e) {
            System.out.println("Filename does not exist.");
            return;
        }
        try {
            channelSftp.rename(FileNames, newName);
        }catch(SftpException e) {
            System.out.println("Could not change filename.");
        }

    }
    // rename a file  on the local machine
    public static void crename(String FileNames){
        String newName;
        if(!FileNames.contains(" ")){
            System.out.println("Command not of the correct form.");
            return;
        }
        newName = FileNames.substring(FileNames.indexOf(" ")+1);
        FileNames = FileNames.substring(0, FileNames.indexOf(" "));
            File nameToChange = new File(myDirectory.getAbsolutePath()+ "\\" + FileNames);
            File nameToChangeTo = new File(myDirectory.getAbsolutePath() + "\\" + newName);
        if(nameToChangeTo.exists()){
            System.out.println("Desired new file name ");
            return;
        }
        nameToChange.renameTo(nameToChangeTo);
    }
}
