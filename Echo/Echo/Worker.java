//package BenchMark.Echo.Echo;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;

class Worker extends Thread { 
  Socket clientSocket;
  int bufferLength;
  Worker(Socket s, int l) { 
    clientSocket = s;
    bufferLength = l;
  }
  public void run() { 
    try {
      OutputStream out = clientSocket.getOutputStream();
      InputStream in = clientSocket.getInputStream();
      byte buffer[] = new byte[bufferLength];
      while (true) {
        int length = in.read(buffer, 0, bufferLength);
        if (length == -1) break;
        out.write(buffer, 0, length);
      }
      clientSocket.close();
    } catch (IOException e) {
      System.err.println("IOException in Worker "+e);
    } 
  }
}
