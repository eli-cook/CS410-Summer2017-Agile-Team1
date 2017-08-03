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

            } else if (command.startsWith("chmod "))
            {
                chmod(command);
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
                }
                  else if (command.equals("rename")){
                    rename(command2);
                }
                  else if (command.equals("crename")){
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
            System.out.println("Files within" + myDirectory.getCanonicalPath() + ":");
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

    // Change the permissions of a file or directory.
    private void chmod(String compact) {
        // Ideally split the string into 3 parts: the chmod command, the permissions portions (parsed on +),
        // and the file name.
        String[] part = compact.split("\\s+");

        int permissions = 0;
        String octal = null;

        // Either retrieves the current permissions of the file or directory, or throws an error if it doesn't exist.
        SftpATTRS target = null;

        try{
            target = channelSftp.lstat(part[2]);
            octal = Integer.toOctalString(target.getPermissions());
        }
        catch (SftpException e){
            System.out.println("The requested file or directory does not exist.");
            return;
        }
        catch(IndexOutOfBoundsException e){
            System.out.println("No target file or directory detected.");
            return;
        }

        // Attempt to parse the second element to an int before deciding what permissions to change.
        try {
            permissions = Integer.parseInt(part[1], 8);
        }
        catch (NumberFormatException e) {
            // Only gets here if the permissions string was not an integer.
            // Convert the permissions to a decimal representation.
            // May need to use octal instead of decimal.

            String[] fields = part[1].split(",");

            // Flag multipliers for what type of user permissions are changed.
            int multiplierU = 0;
            int multiplierG = 0;
            int multiplierO = 0;

            for (int i = 0; i < fields.length; ++i) {
                permissions = 0;

                // Default the multipliers.
                multiplierU = 0;
                multiplierG = 0;
                multiplierO = 0;

                // Refresh the octal code prior to an iteration.
                try{
                    target = channelSftp.lstat(part[2]);
                    octal = Integer.toOctalString(target.getPermissions());
                }
                catch (SftpException f)
                {
                    f.printStackTrace();
                }

                if (fields[i].contains("u"))
                    multiplierU = 100;

                if (fields[i].contains("g"))
                    multiplierG = 10;

                if (fields[i].contains("o"))
                    multiplierO = 1;

                // Handles setting and overriding existing permissions.
                if (fields[i].contains("="))
                {
                    // Handles the regex for read permissions.
                    if (fields[i].contains("r"))
                        permissions += 4 * (multiplierU + multiplierG + multiplierO);
                    else{
                        if(!fields[i].matches(".*u.*") && octal.matches(".*[4567][01234567][01234567]"))
                            permissions += 400;

                        if (!fields[i].matches(".*g.*") && octal.matches(".*[4567][01234567]"))
                            permissions += 40;

                        if (!fields[i].matches(".*o.*") && octal.matches(".*[4567]"))
                            permissions += 4;
                    }

                    // Handles the regex for write permissions.
                    if (fields[i].contains("w"))
                        permissions += 2 * (multiplierU + multiplierG + multiplierO);
                    else{
                        if(!fields[i].matches(".*u.*") && octal.matches(".*[2367][01234567][01234567]"))
                            permissions += 200;

                        if (!fields[i].matches(".*g.*") && octal.matches(".*[2367][01234567]"))
                            permissions += 20;

                        if (!fields[i].matches(".*o.*") && octal.matches(".*[2367]"))
                            permissions += 2;
                    }

                    // Handles the regex for execute permissions.
                    if (fields[i].contains("e") || fields[i].contains("x"))
                        permissions += multiplierU + multiplierG + multiplierO;
                    else{
                        if (!fields[i].matches(".*u.*") && octal.matches(".*[1357][01234567][01234567]"))
                            permissions += 100;

                        if (!fields[i].matches(".*g.*") && octal.matches(".*[1357][01234567]"))
                            permissions += 10;

                        if (!fields[i].matches(".*o.*") && octal.matches(".*[1357]"))
                            permissions += 1;
                    }
                }
                else if (fields[i].contains("+")) {
                    // Handles the regex for read permissions.
                    if (fields[i].contains("r"))
                        permissions += 4 * (multiplierU + multiplierG + multiplierO);

                    if(!fields[i].matches(".*u.*r.*") && octal.matches(".*[4567][01234567][01234567]"))
                        permissions += 400;

                    if (!fields[i].matches(".*g.*r.*") && octal.matches(".*[4567][01234567]"))
                        permissions += 40;

                    if (!fields[i].matches(".*o.*r.*") && octal.matches(".*[4567]"))
                        permissions += 4;

                    // Handles the regex for write permissions.
                    if (fields[i].contains("w"))
                        permissions += 2 * (multiplierU + multiplierG + multiplierO);

                    if(!fields[i].matches(".*u.*w.*") && octal.matches(".*[2367][01234567][01234567]"))
                        permissions += 200;

                    if (!fields[i].matches(".*g.*w.*") && octal.matches(".*[2367][01234567]"))
                        permissions += 20;

                    if (!fields[i].matches(".*o.*w.*") && octal.matches(".*[2367]"))
                        permissions += 2;

                    // Handles the regex for execute permissions.
                    if (fields[i].contains("e") || fields[i].contains("x"))
                        permissions += multiplierU + multiplierG + multiplierO;

                    if (!fields[i].matches(".*u.*[ex].*") && octal.matches(".*[1357][01234567][01234567]"))
                        permissions += 100;

                    if (!fields[i].matches(".*g.*[ex].*") && octal.matches(".*[1357][01234567]"))
                        permissions += 10;

                    if (!fields[i].matches(".*o.*[ex].*") && octal.matches(".*[1357]"))
                        permissions += 1;
                }
                else if(fields[i].contains("-")){
                    // Handles the removal of permissions by omitting the case where it is contained.
                    if(!fields[i].matches(".*u.*r.*") && octal.matches(".*[4567][01234567][01234567]"))
                        permissions += 400;

                    if (!fields[i].matches(".*g.*r.*") && octal.matches(".*[4567][01234567]"))
                        permissions += 40;

                    if (!fields[i].matches(".*o.*r.*") && octal.matches(".*[4567]"))
                        permissions += 4;

                    if(!fields[i].matches(".*u.*w.*") && octal.matches(".*[2367][01234567][01234567]"))
                        permissions += 200;

                    if (!fields[i].matches(".*g.*w.*") && octal.matches(".*[2367][01234567]"))
                        permissions += 20;

                    if (!fields[i].matches(".*o.*w.*") && octal.matches(".*[2367]"))
                        permissions += 2;

                    if (!fields[i].matches(".*u.*[ex].*") && octal.matches(".*[1357][01234567][01234567]"))
                        permissions += 100;

                    if (!fields[i].matches(".*g.*[ex].*") && octal.matches(".*[1357][01234567]"))
                        permissions += 10;

                    if (!fields[i].matches(".*o.*[ex].*") && octal.matches(".*[1357]"))
                        permissions += 1;
                }
                else
                {
                    System.out.println("Invalid command, operand +, -, =, or octal code omitted.");
                }

                // Try to change the permissions of that file or directory by string conversions.
                try {
                    channelSftp.chmod(Integer.parseInt(Integer.toString(permissions), 8), part[2]);
                } catch (SftpException f) {
                    f.printStackTrace();
                }
            }

            return;
        }

        // Try to change the permissions of that file or directory with the provided integer.
        try {
            channelSftp.chmod(permissions, part[2]);
        } catch (SftpException f) {
            f.printStackTrace();
        }
        return;
    }
}

