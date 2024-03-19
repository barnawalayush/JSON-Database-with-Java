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
import java.util.stream.Stream;

public class Main {

    private static final int PORT = 23457;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {

        System.out.println("Server started!");

        try (
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS));
        ) {

            while (!server.isClosed()) {

                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                Thread clientThread = new ClientHandler(input, output, server);
                clientThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static class ClientHandler extends Thread {

        DataInputStream input;
        DataOutputStream output;
        ServerSocket server;

        public ClientHandler(DataInputStream input, DataOutputStream output, ServerSocket server) {
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

            try {
                Request request = new Gson().fromJson(input.readUTF(), Request.class);

                switch (request.getType()) {
                    case "get":
                        readLock.lock();
                        response = processGetRequest(request);
                        readLock.unlock();
                        break;
                    case "set":
                        writeLock.lock();
                        response = processSetRequest(request);
                        writeLock.unlock();
                        break;
                    case "delete":
                        writeLock.lock();
                        response = processDeleteRequest(request);
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
            } finally {
                try {
                    output.writeUTF(new Gson().toJson(response));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Response processDeleteRequest(Request request) {

        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        JsonElement keySet = request.getKey();

        if (keySet.isJsonPrimitive()) {

            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

                jsonObject = new JsonObject();

                try (FileWriter writer = new FileWriter(path.toString())) {
                    new Gson().toJson(jsonObject, writer);
                }

                response.setResponse("OK");
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {

            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonObject originalJsonObject = jsonObject;
                JsonArray keyArray = keySet.getAsJsonArray();

                class JsonObjectClass {
                    JsonObject jsonObject;

                    public JsonObjectClass(JsonObject jsonObject) {
                        this.jsonObject = jsonObject;
                    }
                }
                JsonObjectClass jsonObjectClass = new JsonObjectClass(jsonObject);

                Stream.iterate(0, index -> index < keyArray.size() - 1, index -> index + 1).forEach(index -> {
                    String key = String.valueOf(keyArray.get(index));
                    key = key.substring(1, key.length() - 1);
                    if (jsonObjectClass.jsonObject.has(key) && jsonObjectClass.jsonObject.get(key).isJsonObject()) {
                        jsonObjectClass.jsonObject = jsonObjectClass.jsonObject.get(key).getAsJsonObject();
                    }
                });
                jsonObject = jsonObjectClass.jsonObject;

                String key = String.valueOf(keyArray.get(keyArray.size() - 1));
                key = key.substring(1, key.length() - 1);
                jsonObject.remove(key);

                try (FileWriter writer = new FileWriter(path.toString())) {
                    new Gson().toJson(originalJsonObject, writer);
                }

                response.setResponse("OK");
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static Response processSetRequest(Request request) {
        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        JsonElement keySet = request.getKey();

        if (keySet.isJsonPrimitive()) {

            try {
                String key = String.valueOf(keySet);
                key = key.substring(1, key.length() - 1);

                JsonObject newJsonObject = new JsonObject();
                newJsonObject.add(key, request.getValue());

                try (FileWriter writer = new FileWriter(path.toString())) {
                    new Gson().toJson(newJsonObject, writer);
                }

                response.setResponse("OK");
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        } else {

            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonObject originalJsonObject = jsonObject;
                JsonArray keyArray = keySet.getAsJsonArray();

                class JsonObjectClass {
                    JsonObject jsonObject;

                    public JsonObjectClass(JsonObject jsonObject) {
                        this.jsonObject = jsonObject;
                    }
                }
                ;
                JsonObjectClass jsonObjectClass = new JsonObjectClass(jsonObject);
                Stream.iterate(0, index -> index < keyArray.size() - 1, index -> index + 1).forEach(index -> {
                    String key = String.valueOf(keyArray.get(index));
                    key = key.substring(1, key.length() - 1);
                    if (jsonObjectClass.jsonObject.has(key) && jsonObjectClass.jsonObject.get(key).isJsonObject()) {
                        jsonObjectClass.jsonObject = jsonObjectClass.jsonObject.get(key).getAsJsonObject();
                    }
                });
                jsonObject = jsonObjectClass.jsonObject;

                String key = String.valueOf(keyArray.get(keyArray.size() - 1));
                key = key.substring(1, key.length() - 1);
                jsonObject.add(key, request.getValue());

                try (FileWriter writer = new FileWriter(path.toString())) {
                    new Gson().toJson(originalJsonObject, writer);
                }

                response.setResponse("OK");
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static Response processGetRequest(Request request) {

        Path path = Paths.get("/Users/abarnawal/Java Intellijec Projects/JSON Database with Java/JSON Database with Java/task/src/server/data/db.json");

        Response response = new Response();

        JsonElement keySet = request.getKey();

        if (keySet.isJsonPrimitive()) {

            try (FileReader reader = new FileReader(path.toString())) {
                Request myObject = new Gson().fromJson(reader, Request.class);

                if (request.getKey().equals(myObject.getKey())) {
                    response.setResponse("OK");
                    response.setValue(myObject.getValue());
                    return response;
                } else {
                    response.setResponse("ERROR");
                    response.setReason("No such key");
                    return response;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;

        } else {
            try {
                String jsonString = new String(Files.readAllBytes(path));
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                JsonArray keyArray = keySet.getAsJsonArray();

                class JsonObjectClass {
                    JsonObject jsonObject;

                    public JsonObjectClass(JsonObject jsonObject) {
                        this.jsonObject = jsonObject;
                    }
                }
                JsonObjectClass jsonObjectClass = new JsonObjectClass(jsonObject);

                Stream.iterate(0, index -> index < keyArray.size() - 1, index -> index + 1).forEach(index -> {
                    String key = String.valueOf(keyArray.get(index));
                    key = key.substring(1, key.length() - 1);
                    if (jsonObjectClass.jsonObject.has(key) && jsonObjectClass.jsonObject.get(key).isJsonObject()) {
                        jsonObjectClass.jsonObject = jsonObjectClass.jsonObject.get(key).getAsJsonObject();
                    }
                });
                jsonObject = jsonObjectClass.jsonObject;

                String key = String.valueOf(keyArray.get(keyArray.size() - 1));
                key = key.substring(1, key.length() - 1);
                JsonElement value = jsonObject.get(key);

                response.setResponse("OK");
                response.setValue(value);
                return response;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
