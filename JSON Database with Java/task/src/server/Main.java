package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final int PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {

//        String[] database = new String[1000];
        Map<String, String> database = new HashMap<>();

//        Arrays.fill(database, "");

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

                Request request = new Gson().fromJson(receivedMessage, Request.class);
                String received = request.getType() + " " + request.getKey() + " " + request.getValue();

                Response response = new Response();
                String sendMessage = getResult(received, database, response);
                output.writeUTF(sendMessage);

                if(received.split(" ")[0].equals("exit"))break;

            }

        }catch (IOException e){
            e.printStackTrace();
        }

//        Scanner sc = new Scanner(System.in);


    }

    private static String getResult(String receivedMessage, Map<String, String> database, Response response) {

        String[] input = receivedMessage.split(" ");

        if(input[0].equals("exit")){
            response.setResponse("OK");
            return new Gson().toJson(response);
        }

        String command = input[0];
        String key = input[1];
        StringBuilder value = new StringBuilder();

        if(command.equals("get")){
            if(!database.containsKey(key)){
                response.setResponse("ERROR");
                response.setReason("No such key");
                return new Gson().toJson(response);
            }else{
                response.setResponse("OK");
                response.setValue(database.get(key));
                return new Gson().toJson(response);
            }
        } else if(command.equals("set")){
            int ind = 2;
            while(ind < input.length){
                value.append(input[ind]);
                if(ind != input.length-1)value.append(" ");
                ind++;
            }

                database.put(key, value.toString());
                response.setResponse("OK");
                return new Gson().toJson(response);

        } else if(command.equals("delete")){
            if(!database.containsKey(key)){
                response.setResponse("ERROR");
                response.setReason("No such key");
                return new Gson().toJson(response);
            }else {
                    database.remove(key);
                    response.setResponse("OK");
                    return new Gson().toJson(response);
            }
        }

        return "";
    }
}
