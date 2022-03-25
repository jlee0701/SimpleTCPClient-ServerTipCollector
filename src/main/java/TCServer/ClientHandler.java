package TCServer;

import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private Socket socket;
    private int node_1_port = 7000;
    private int node_2_port = 7001;
    private String host = "localhost";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public JSONObject requestChange() {
        JSONObject object = new JSONObject();
        object.put("request","get_vote");
        JSONObject response1 = NetworkUtils.send(host, node_1_port, object);
        if (response1.has("error")) {
            return response1;
        }

        JSONObject response2 = NetworkUtils.send(host, node_2_port, object);
        if (response2.has("error")) {
            return response2;
        }

        JSONObject globalRequest = new JSONObject();
        globalRequest.put("request","update_status");
        if(response1.getInt("value") != 1 && response2.getInt("value") != 1) {
            globalRequest.put("status","commit");
            globalRequest.put("message","Both of Node1 and Node2 like the tip :)");
        } else if(response1.getInt("value") == 1 && response2.getInt("value") != 1){
            globalRequest.put("status","abort");
            globalRequest.put("message","Node1 doesn't like the tip :(");
        } else if(response1.getInt("value") != 1 && response2.getInt("value") == 1){
            globalRequest.put("status","abort");
            globalRequest.put("message","Node2 doesn't like the tip :(");
        } else if(response1.getInt("value") == 1 && response2.getInt("value") == 1){
            globalRequest.put("status","abort");
            globalRequest.put("message","Both Node1 and Node2 don't like the tip :(");
        } else {
            globalRequest.put("error", "error");
        }
        return globalRequest;
    }

    public JSONObject updateStatus(JSONObject object) {
        JSONObject response1 = NetworkUtils.send(host, node_1_port, object);
        if (response1.has("error")) {
            return response1;
        }

        JSONObject response2 = NetworkUtils.send(host, node_2_port, object);
        if (response2.has("error")) {
            return response2;
        }

        JSONObject status = new JSONObject();
        if(response1.get("response").equals("list_updated") && response2.get("response").equals("list_updated")) {
            status.put("response","nodes_updated");
        } else if(response1.get("response").equals("list_not_updated") || response2.get("response").equals("list_not_updated")) {
            status.put("response","nodes_updated");
        } else {
            status.put("error","cannot update nodes");
        }
        status.put("data_n1",response1.get("value"));
        status.put("data_n2",response2.get("value"));
        status.put("message",object.get("message"));
        return status;

    }

    public JSONObject getList(JSONObject object) {
        JSONObject response1 = NetworkUtils.send(host, node_1_port, object);
        if (response1.has("error")) {
            return response1;
        }

        JSONObject response2 = NetworkUtils.send(host, node_2_port, object);
        if (response2.has("error")) {
            return response2;
        }

        JSONObject response = new JSONObject();
        if(!response1.get("response").equals("current list") || !response2.get("response").equals("current list")) {
            response.put("error","an error occurred within the server...");
        }
        response.put("response","current_list");
        response.put("message","You have retrieved the most updated list from both nodes!");
        response.put("data_n1",response1.get("value"));
        response.put("data_n2",response2.get("value"));
        return response;
    }

    public JSONObject error(String error) {
        JSONObject ret = new JSONObject();
        ret.put("error", error);
        return ret;
    }

    public JSONObject quit() {
        JSONObject json = new JSONObject();
        json.put("response", "quit");
        json.put("message", "Disconnecting from the server...");
        return json;
    }


    public void run() {
        boolean quit = false;
        try {
            System.out.println("Server connected to client " + socket.getPort());
            while (!quit) {
                try {
                    JSONObject root = NetworkUtils.read(socket);
                    System.out.println("DEBUG: request received from client: " + root.toString(2));

                    JSONObject status = new JSONObject();
                    JSONObject response = new JSONObject();
                    String choice = root.getString("service");
                    System.out.println("DEBUG: choice = " + choice);
                    switch (choice) {
                        case ("1"):
                            System.out.println("DEBUG: switched with 1");
                            status = requestChange();
                            System.out.println("DEBUG: response = " + status.toString(2));
                            status.put("data", root.getString("data"));
                            response = updateStatus(status);
                            System.out.println("DEBUG: final response = " + status.toString(2));
                            break;
                        case ("2"):
                            status.put("request", "current_list");
                            response = getList(status);
                            break;
                        case ("0"):
                            response = quit();
                            quit = true;
                            break;
                        default:
                            response = error("Invalid selection: " + choice
                                    + " is not an option");
                            break;
                    }
                    // we are converting the JSON object we have to a byte[]
                    NetworkUtils.respond(socket, response);
                    socket.close();
                } catch (SocketException | EOFException e) {
                    // expected on timeout
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    // cleanup, just in case
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            // close the resource
            System.out.println("close the resources of client " + socket.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
