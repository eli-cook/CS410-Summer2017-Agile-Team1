package com.pdx.cs410.sftp;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import static com.jcraft.jsch.ChannelSftp.RESUME;
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
	public static int timeoutSeconds = 60;

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
			TimerTask idleTimeoutExit = new TimerTask()
            {
                public void run()
                {
                    System.out.println("You have been idle for not entering any commands for " + timeoutSeconds + " seconds.");
                    System.out.println("Exiting Program.");
                    session.disconnect();
                    System.exit(0);
                }
            };
			Timer timer = new Timer();
			timer.schedule( idleTimeoutExit, timeoutSeconds*1000 );
            System.out.print("$ ");
            command = null;
            command = in.nextLine();
			timer.cancel();
            if (command.equals("ls")) {
                ls();
            } else if (command.equals("cls")) {
                cls();
            } else if (command.startsWith("mkdir ") && (command.length() > 6)) {
                mkdir(command.substring(6));
            } else if (command.equals("mkdir")) {
                mkdir();

            } else if (command.equals("resume")){
                resume();
            }
            else if (command.startsWith("chmod "))
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
                } else if (command.equals("put")) {
                    put(command2);
                } else if(command.equals("rm")) {
                    rm(command2);
                } else if (command.equals("rename")){
                    rename(command2);
                } else if (command.equals("crename")){
                    crename(command2);
                } else if (command.equals("multiget")){
                    multiget(command2);
                } else if (command.equals("multiput")){
                    multiput(command2);
                } else if (command.equals("cp")){
                    copyDir(command2);
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
            System.out.println("Could not retrieve " + toFind + " to " + path + " (File does not exist).");
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
            channelSftp.put(myDirectory.getAbsolutePath()+"\\"+toFind, path);
        } catch (SftpException e) {
            e.printStackTrace();
            System.out.println("Could not move "+ toFind + " to " + path + " (File doesn't exist).");
        }
    }
    // Used to create a directory within the current directory.
    private static void mkdir() {
        String newdir;
        System.out.println("Enter the name of the new directory:");
        newdir = in.nextLine();

        mkdir(newdir);
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

    //get multiple files from a remove server
    public static void multiget(String toGet){
        int indexOf = 0;
        List<String> Files = new ArrayList<String>();
        if(!toGet.contains(" ")){
            get(toGet);
        }
        else{
            while(!toGet.equals("")){
                    indexOf = toGet.indexOf(" ");
                    Files.add(toGet.substring(0, indexOf));
                    toGet = toGet.substring(indexOf + 1);
                    if(!toGet.contains(" ")){
                        Files.add(toGet);
                        toGet = "";
                    }
            }
            for(int i = 0;i < Files.size();++i){
                get(Files.get(i));
            }
        }
    }
    //put multiple files on a remote server
    public static void multiput(String toPut){
        int indexOf = 0;
        List<String> Files = new ArrayList<String>();
        if(!toPut.contains(" ")){
            put(toPut);
        }
        else{
            while(!toPut.equals("")){
                indexOf = toPut.indexOf(" ");
                Files.add(toPut.substring(0, indexOf));
                toPut = toPut.substring(indexOf + 1);
                if(!toPut.contains(" ")){
                    Files.add(toPut);
                    toPut = "";
                }
            }
            for(int i = 0;i < Files.size();++i){
                put(Files.get(i));
            }
        }
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

    //Copy Directory from remote to remote
    public static void copyDir(String arguments) {
        String [] parameters = arguments.split(" ");
        if(parameters.length != 2){
            System.out.println("Invalid Parameters for copy command");
            return;
        }

        String src = parameters[0];
        String dest = parameters[1];
        Channel downloadChannel = null;
        ChannelSftp downloadChannelSftp = null;

        //Check that both source and dest exists and are directories
        try{
            SftpATTRS srcStat = channelSftp.stat(src);
            SftpATTRS destStat = channelSftp.stat(dest);
            if(!srcStat.isDir()){
                System.out.println("Source path is not a directory.");
                return;
            }
            if(!destStat.isDir()){
                System.out.println("Destination path is not a directory.");
                return;
            }

            downloadChannel = session.openChannel("sftp");
            downloadChannel.connect();
            downloadChannelSftp = (ChannelSftp) downloadChannel;

            downloadChannelSftp.cd(channelSftp.pwd());
            downloadChannelSftp.cd(src);

            String savedDir = channelSftp.pwd();
            String[] temp = downloadChannelSftp.pwd().split("/");

            channelSftp.cd(dest);
            channelSftp.mkdir(temp[temp.length-1]);
            channelSftp.cd(temp[temp.length-1]);
            copyDir(downloadChannelSftp);
            channelSftp.cd(savedDir);
        }
        catch(JSchException e){
            e.printStackTrace();
            System.out.println(e.toString());
        }

        catch(SftpException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
        if(downloadChannelSftp != null)
            downloadChannelSftp.disconnect();
        return;
    }


    private static void copyDir(ChannelSftp downloadChannelSftp) {
        try {
            Vector filelist = downloadChannelSftp.ls(downloadChannelSftp.pwd());
            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                String filename = entry.getFilename();
                if (filename.charAt(0) != '.') {
                    //Case for a file
                    if (!entry.getAttrs().isDir()) {
                        InputStream tempInput = downloadChannelSftp.get(filename);
                        channelSftp.put(tempInput, filename);
                    }
                    else {
                        //Case for directories
                        downloadChannelSftp.cd(downloadChannelSftp.pwd() + "/" + filename);

                        channelSftp.mkdir(filename);
                        channelSftp.cd(filename);
                        copyDir(downloadChannelSftp);

                        channelSftp.cd("..");
                        downloadChannelSftp.cd("..");
                    }
                }
            }
        }
        catch (SftpException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
    // Resume an upload or download depending on input.
    private void resume(){
        String choice;
        String target;
        String dest;

        System.out.println("What sort of task would you like to resume?\nEnter 'put' or 'get': ");
        choice = in.nextLine();

        System.out.println("Enter the name of the target file or directory to be copied: ");
        target = in.nextLine();

        // Attempt to resume the process.
        // Test with mnist_train.csv, showing a file size of 109,575,994 bytes using 'ls -l mnist_train.csv' on the usual
        // linux client and a rounded size of 107,008 kB on your local machine.
        // Quickly finishes if the file already exists on both ends.
        // Complains if the target file does not exist.
        // If using put without part of the file on the remote end, will basically function as a standard put command.
        try{
            // Currently does not use a progress monitor, can implement in a later edition to add a tracker.
            if(choice.equalsIgnoreCase("get"))
                channelSftp.get(target, myDirectory.getAbsolutePath(), null, RESUME);
            else if(choice.equalsIgnoreCase("put"))
                channelSftp.put(myDirectory.getAbsolutePath() + "\\" + target, channelSftp.pwd(), RESUME);
        }
        catch (SftpException e)
        {
            e.printStackTrace();
            System.out.print(target + " could not be copied from.");
        }
        return;
    }
}

