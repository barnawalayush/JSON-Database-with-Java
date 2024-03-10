package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import server.Request;
import server.Request;

import static java.nio.channels.FileChannel.open;

public class Main {

    private static final int PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    @Parameter(names = "-t", description = "Type Of Operation")
    String typeOfOperation;
    @Parameter(names = "-k", description = "key")
    String index;
    @Parameter(names = "-v", description = "value")
    String text;
    @Parameter(names = "-in", description = "file name")
    String fileName;

    public static void main(String[] args) {

        String operation = "";
        Request request;

        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        try(
                Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream());
        ){
            System.out.println("Client started!");


            if(main.fileName == null){
                request = new Request(main.typeOfOperation, main.index, main.text);
            }else{
                String content = new String(Files.readAllBytes(Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/client/data/" + main.fileName)));
                request = new Gson().fromJson(content, Request.class);
            }


//            String sendMessage = main.typeOfOperation + " " + main.index + " " + main.text;
            String sendMessage = new Gson().toJson(request);
            output.writeUTF(sendMessage);
            System.out.println("Sent: " + sendMessage);

            String receivedMessage = input.readUTF();
            System.out.println("Received: " + receivedMessage);
//                if(main.typeOfOperation.equals("exit"))break;
//            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}