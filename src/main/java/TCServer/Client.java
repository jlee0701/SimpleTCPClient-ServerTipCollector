package TCServer;

import java.io.*;
import java.net.Socket;
import java.util.*;

import org.json.JSONObject;

public class Client {
  /**
   * Thread that declares the lambda and then initiates the work
   */

  public static int message_id = 0;
  private static BufferedReader stdin;

  public static JSONObject add() {
    String strToSend = null;
    JSONObject req = new JSONObject();
    req.put("service", "1");
    try {
      System.out.print("Please share a tip for SER 321: ");
      strToSend = stdin.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    req.put("data", strToSend);
    return req;
  }

  public static JSONObject getList() {
    JSONObject req = new JSONObject();
    req.put("service", "2");
    req.put("data", "");
    return req;
  }

  public static JSONObject quit() {
    JSONObject request = new JSONObject();
    request.put("service", "0");
    request.put("data", ".");
    return request;
  }

  public static void main(String[] args) {

    String host;
    int port;
    Socket sock;
    stdin = new BufferedReader(new InputStreamReader(System.in));
    try {
      if (args.length != 2) {
        // gradle runClient -Phost=localhost -Pport=9099 -q --console=plain
        System.out.println("Usage: gradle runClient -Phost=localhost -Pport=9099");
        System.exit(0);
      }

      host = args[1];
      port = -1;
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException nfe) {
        System.out.println("[Port] must be an integer");
        System.exit(2);
      }

      sock = new Socket(host, port);
      OutputStream out = sock.getOutputStream();
      InputStream in = sock.getInputStream();
      Scanner input = new Scanner(System.in);
      int choice = 0;
      boolean done = false;
      while(!done) {
        System.out.println();
        // TODO: you will need to change the menu based on the tasks for this assignment, see Readme!
        System.out.println("Client Menu");
        System.out.println("Please select a valid option (1 or 2). 0 to diconnect the client");
        System.out.println("1. add a tip - adds a SER 321 tip to the server");
        System.out.println("2. display - see a list of the existing tips for SER 321");
        System.out.println("0. quit");
        System.out.println();
        boolean isValid = false;
        while (!isValid) {
          try {
            choice = input.nextInt(); // what if not int.. should error handle this
            isValid = true;
          } catch (InputMismatchException e) {
            System.out.println("Please enter a valid option!");
            input.next();
          }
        }

        JSONObject response = null;
        switch (choice) {
          case (1):
            response = NetworkUtils.send(host, port, add());
            break;
          case (2):
            response = NetworkUtils.send(host, port, getList());
            break;
          case (0):
            response = NetworkUtils.send(host, port, quit());
            break;
          default:
            System.out.println("Please select a valid option (0-2).");
            break;
        }
        if (response != null) {

          System.out.println(response);

          if (response.has("error")) {
            System.out.println(response.getString("error"));
          } else {
            if (response.getString("response").equals("quit")) {
              System.out.println(response.getString("message"));
              sock.close();
              out.close();
              in.close();
              break;
            }
            System.out.println("The response from the server: ");
            System.out.println(response.getString("message"));
            System.out.println("Node1 list:");
            System.out.println(response.getString("data_n1"));
            System.out.println("Node2 list:");
            System.out.println(response.getString("data_n2"));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
