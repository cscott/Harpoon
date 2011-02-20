import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

class Time { 
  static public void main(String args[]) throws IOException, NumberFormatException { 
    int p = Integer.valueOf(args[0]).intValue();
    int l = Integer.valueOf(args[1]).intValue(); 
    ServerSocket s = new ServerSocket(p);
    while (true) { 
	Time.start(s,l);
    }
  }
    static public void start(ServerSocket s, int l) throws java.io.IOException {
	Socket c = s.accept();
	TWorker w = new TWorker(c, l);
	w.start();
    }
}
