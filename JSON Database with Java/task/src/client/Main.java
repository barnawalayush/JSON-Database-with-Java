package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;

public class Main {

    private static final int PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {

        try(
                Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream());
        ){
            System.out.println("Client started!");

            String sendMessage = "Give me a record # 12";
            output.writeUTF(sendMessage);
            System.out.println("Sent: " + sendMessage);

            String receivedMessage = input.readUTF();
            System.out.println("Received: " + receivedMessage);

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
