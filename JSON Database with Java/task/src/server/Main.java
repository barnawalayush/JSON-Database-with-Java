package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final int PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {

        String[] database = new String[1000];
        Arrays.fill(database, "");

        String receivedMessage = "";

        System.out.println("Server started!");

        try(
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS));
                ){

            while(true){
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream());

                receivedMessage = input.readUTF();
                // System.out.println("Received: " + receivedMessage);

                String sendMessage = getResult(receivedMessage, database);
//            String sendMessage = "hii" + receivedMessage;
                output.writeUTF(sendMessage);
                // System.out.println("Sent: " + sendMessage);
                if(receivedMessage.split(" ")[0].equals("exit"))break;
            }

        }catch (IOException e){
            e.printStackTrace();
        }

//        Scanner sc = new Scanner(System.in);


    }

    private static String getResult(String receivedMessage, String[] database) {

        String[] input = receivedMessage.split(" ");

        if(input[0].equals("exit")) return "OK";

        String command = input[0];
        int index = Integer.parseInt(input[1])-1;
        StringBuilder text = new StringBuilder();

        if(command.equals("get")){
            if(database[index].isEmpty()){
                return "Error";
            }else{
                return database[index];
            }
        } else if(command.equals("set")){
            int ind = 2;
            while(ind < input.length){
                text.append(input[ind]);
                if(ind != input.length-1)text.append(" ");
                ind++;
            }
            if(index < 0 || index >= 100){
                return "Error";
            }else{
                database[index] = text.toString();
                return "OK";
            }
        } else if(command.equals("delete")){
            if(index < 0 || index >= 100){
                return "Error";
            }else{
                database[index] = "";
                return "OK";
            }
        }

        return "";
    }
}
