/**
 * Created by Sean on 7/13/2017.
 */

import com.jcraft.jsch.*;

import java.util.Scanner;

public class UI {
    public static Scanner in = new Scanner(System.in);
    public static JSch client = new JSch();
    public static Session session = null;
    public static Channel channel = null;
    public static ChannelSftp channelSftp = null;
    public static String hostname = null;
    public static String username = null;
    public static String password = null;
    public static int PORT = 0;
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
            System.out.print("Please enter a hostname: ");
            hostname = in.nextLine();
            System.out.print("Enter a port number:");
            PORT = in.nextInt();
            in.nextLine();
            System.out.print("Enter your username: ");
            username = in.nextLine();
            System.out.print("Enter your password: ");
            password = in.nextLine();
            try{
                session = client.getSession("pater2", "linux.cs.pdx.edu", 22);
                session.setPassword("Kayle077r");
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                lockDown = true;
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

}
