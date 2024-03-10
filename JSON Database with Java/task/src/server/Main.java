package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    private static final int PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {

        String receivedMessage = "";

        System.out.println("Server started!");

        try(
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS));
        ){

            while(!server.isClosed()){

                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream());


                Thread t = new ClientHandler(input, output, server);

                // Invoking the start() method
                t.start();

//                receivedMessage = input.readUTF();
//
//                Request request = new Gson().fromJson(receivedMessage, Request.class);
//                String received = request.getType() + " " + request.getKey() + " " + request.getValue();
//
//                Response r1 = new Response();
//                if(request.getType().equals("get")){
//                    r1 = m1(request);
//                }else if(request.getType().equals("set")){
//                    r1 = m2(request);
//                }else if(request.getType().equals("delete")){
//                    r1 = m3(request);
//                }else{
//                    r1.setResponse("OK");
//                }
//
//
////                Response response = new Response();
////                String sendMessage = getResult(received, database, response);
//                String sendMessage = new Gson().toJson(r1);
//                output.writeUTF(sendMessage);
//
//                if(request.getType().equals("exit"))break;

            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static class ClientHandler extends Thread{

        DataInputStream input;
        DataOutputStream output;
        ServerSocket server;

        public ClientHandler(DataInputStream input, DataOutputStream output, ServerSocket server){
            this.input = input;
            this.output = output;
            this.server = server;
        }


        @Override
        public void run() {

            Response response = new Response();
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
            Lock readLock = lock.readLock();
            Lock writeLock = lock.writeLock();
            Boolean flag = false;

//            while (!flag){
                try {
                    Request request = new Gson().fromJson(input.readUTF(), Request.class);

                    switch (request.getType()){
                        case "get":
                            readLock.lock();
                            response = m1(request);
                            readLock.unlock();
                            break;
                        case "set":
                            writeLock.lock();
                            response = m2(request);
                            writeLock.unlock();
                            break;
                        case "delete":
                            writeLock.lock();
                            response = m3(request);
                            writeLock.unlock();
                            break;
                        case "exit":
                            response.setResponse("OK");
                            output.writeUTF(new Gson().toJson(response));
                            server.close();
                            return;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    try {
//                        server.close();
                        output.writeUTF(new Gson().toJson(response));
                        flag = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
//            }


        }
    }

    private static Response m3(Request request) {

        File file = new File("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        System.out.println(request.getKey());
        System.out.println(request.getValue());

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

//            if (jsonObject.has("key") && jsonObject.get("key").getAsString().equals(request.getKey())) {
//                // Remove the object from the file
//                file.delete();
////            }

            if(jsonObject.has("key") && jsonObject.get("key").getAsString().equals(request.getKey())){
                jsonObject.remove("key");
                jsonObject.remove("value");
                response.setResponse("OK");

                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(jsonObject, writer);
                }

            }else{
                response.setResponse("ERROR");
                response.setReason("No such key");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;

//        String existingJson = readJsonFromFile(path.toString());
//
//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<Request>>(){}.getType();
//        List<Request> dataList = gson.fromJson(existingJson, listType);
//
//        Boolean isPresent = false;
//
//        Iterator<Request> iterator = dataList.iterator();
//        while (iterator.hasNext()) {
//            Request dataObject = iterator.next();
//            if (dataObject.getKey().equals(request.getKey())) {
//                iterator.remove(); // Remove the object
//                response.setResponse("OK");
//                isPresent = true;
//                break;
//            }
//        }
//
//        String updatedJson = gson.toJson(dataList);
//
//        // Write the updated JSON content back to the file
//        writeJsonToFile(updatedJson, path.toString());
//
//        if(isPresent)return response;
//
//        response.setResponse("ERROR");
//        response.setReason("No such key");
//        return response;

    }

    private static Response m2(Request request) {
        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Request r1 = new Request();
        r1.setKey(request.getKey());
        r1.setValue(request.getValue());

        Response response = new Response();

        try (FileReader reader = new FileReader(path.toString())) {
            // Convert JSON to Java object
            Request myObject = new Gson().fromJson(reader, Request.class);

            if(request.getKey().equals(myObject.getKey())){
                myObject.setValue(request.getValue());
            }else{
                myObject.setKey(request.getKey());
                myObject.setValue(request.getValue());
            }

            try (FileWriter writer = new FileWriter(path.toString())) {
                new Gson().toJson(myObject, writer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        String existingJson = readJsonFromFile(path.toString());
//
//        // Parse existing JSON array into a list of objects
//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<Request>>(){}.getType();
//        List<Request> dataList = gson.fromJson(existingJson, listType);
//
//        Boolean isPresent = false;
//        // Find and update the value with key "2"
//        for (Request dataObject : dataList) {
//            if (dataObject.getKey().equals(request.getKey())) {
//                dataObject.setValue(request.getValue());
//                isPresent = true;
//                break; // Stop after finding and updating the value
//            }
//        }
//
//        if(!isPresent){
//            dataList.add(r1);
//        }
//
//        // Convert the updated list back to JSON
//        String updatedJson = gson.toJson(dataList);
//
//        // Write the updated JSON content back to the file
//        writeJsonToFile(updatedJson, path.toString());


        response.setResponse("OK");
        return response;

    }

    private static Response m1(Request request) {

        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        try (FileReader reader = new FileReader(path.toString())) {
            // Convert JSON to Java object
            Request myObject = new Gson().fromJson(reader, Request.class);

            if(request.getKey().equals(myObject.getKey())){
                response.setResponse("OK");
                response.setValue(myObject.getValue());
                return response;
            }else{
                response.setResponse("ERROR");
                response.setReason("No such key");
                return response;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        String existingJson = readJsonFromFile(path.toString());
//
//        // Parse existing JSON array into a list of objects
//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<Request>>(){}.getType();
//        List<Request> dataList = gson.fromJson(existingJson, listType);
//
//        Boolean isPresent = false;
//
//        for (Request dataObject : dataList) {
//            if (dataObject.getKey().equals(request.getKey())) {
//                response.setResponse("OK");
//                response.setValue(dataObject.getValue());
//                isPresent = true;
//                break; // Stop after finding and updating the value
//            }
//        }
//
//        String updatedJson = gson.toJson(dataList);
//
//        // Write the updated JSON content back to the file
//        writeJsonToFile(updatedJson, path.toString());
//
//        if(isPresent){
//            return response;
//        }

        return response;
    }

    private static void writeJsonToFile(String json, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readJsonFromFile(String filePath) {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


}
