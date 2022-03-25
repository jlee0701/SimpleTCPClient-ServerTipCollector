package TCServer;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCServer implements Runnable{
  ServerSocket server = null;
  int serverPort = 8080;
  boolean isStopped = false;
  Thread runningThread = null;


  public TCServer(int port){
    this.serverPort = port;
  }

  public void run(){
    synchronized(this){
      this.runningThread = Thread.currentThread();
    }
    openServerSocket();
    while(! isStopped()){
      Socket clientSocket = null;
      try {
        clientSocket = this.server.accept();
        System.out.println("Connection received from " + clientSocket.getPort());
      } catch (IOException e) {
        if(isStopped()) {
          //System.out.println("Server Stopped.") ;
          return;
        }
        throw new RuntimeException(
                "Error accepting client connection", e);
      }
      new Thread(new ClientHandler(clientSocket)).start();
    }
    //System.out.println("Server Stopped.") ;
  }


  private synchronized boolean isStopped() {
    return this.isStopped;
  }

  public synchronized void stop(){
    this.isStopped = true;
    try {
      this.server.close();
    } catch (IOException e) {
      throw new RuntimeException("Error closing server", e);
    }
  }

  private void openServerSocket() {
    try {
      this.server = new ServerSocket(this.serverPort);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open port", e);
    }
  }

  public static void main(String[] args) {
    //new TCServer(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), "3.144.168.62", "3.144.168.62").run();
    //new TCServer(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), "localhost", "localhost").run();
    int port;

    if (args.length != 1) {
      // gradle runServer -Pport=9099 -q --console=plain
      System.out.println("Usage: gradle runServer -Pport=9099 -q --console=plain");
      System.exit(1);
    }
    port = -1;
    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }
    System.out.println("Threaded Server Started...");

    while (true) {
      TCServer server = new TCServer(port);
      new Thread(server).start();

      try {
        Thread.sleep(20 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Stopping thread");
      server.stop();
    }
  }
}

