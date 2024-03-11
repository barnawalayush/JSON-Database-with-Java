package server;

import com.google.gson.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
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
                            //response = m3(request);
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

//    private static Response m3(Request request) {
//
//        File file = new File("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");
//
//        Response response = new Response();
//
//        System.out.println(request.getKey());
//        System.out.println(request.getValue());
//
//        try (FileReader reader = new FileReader(file)) {
//            Gson gson = new Gson();
//            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
//
//            if(jsonObject.has("key") && jsonObject.get("key").getAsString().equals(request.getKey())){
//                jsonObject.remove("key");
//                jsonObject.remove("value");
//                response.setResponse("OK");
//
//                try (FileWriter writer = new FileWriter(file)) {
//                    gson.toJson(jsonObject, writer);
//                }
//
//            }else{
//                response.setResponse("ERROR");
//                response.setReason("No such key");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return response;
//
//    }

    private static Response m2(Request request) {
        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Request r1 = new Request();
        r1.setKey(request.getKey());
        r1.setValue(request.getValue());

        Response response = new Response();

        JsonElement keySet = request.getKey();

        if(keySet.isJsonPrimitive()){

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

            response.setResponse("OK");
            return response;
        }else{

            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonObject j1 = jsonObject;
                JsonArray keyArray = keySet.getAsJsonArray();

                for(int i=0; i<keyArray.size()-1; i++){
                    String k = String.valueOf(keyArray.get(i));
                    k = k.substring(1, k.length()-1);
                    if(jsonObject.has(k) && jsonObject.get(k).isJsonObject()){
                        jsonObject = jsonObject.get(k).getAsJsonObject();
                    }
                }

                String key = String.valueOf(keyArray.get(keyArray.size()-1));
                key = key.substring(1, key.length()-1);
                System.out.println(jsonObject.get(key));
                jsonObject.add(key, request.getValue());

                response.setResponse("OK");
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static Response m1(Request request) {

        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        JsonElement keySet = request.getKey();

        if(keySet.isJsonPrimitive()){

            try (FileReader reader = new FileReader(path.toString())) {
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

            return response;

        }else{
            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonArray keyArray = keySet.getAsJsonArray();

                for(int i=0; i<keyArray.size()-1; i++){
                    String k = String.valueOf(keyArray.get(i));
                    k = k.substring(1, k.length()-1);
                    if(jsonObject.has(k) && jsonObject.get(k).isJsonObject()){
                        jsonObject = jsonObject.get(k).getAsJsonObject();
                    }
                }

                String key = String.valueOf(keyArray.get(keyArray.size()-1));
                key = key.substring(1, key.length()-1);
                JsonElement value = jsonObject.get(key);

                response.setResponse("OK");
                response.setValue(value);
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        //return response;



//        Response response = new Response();
//
//        try (FileReader reader = new FileReader(path.toString())) {
//            // Convert JSON to Java object
//            Request myObject = new Gson().fromJson(reader, Request.class);
//
//            if(request.getKey().equals(myObject.getKey())){
//                response.setResponse("OK");
//                response.setValue(myObject.getValue());
//                return response;
//            }else{
//                response.setResponse("ERROR");
//                response.setReason("No such key");
//                return response;
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return response;
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
