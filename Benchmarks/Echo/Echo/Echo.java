//package BenchMark.Echo.Echo;
import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

class Echo { 

    static public void main(String args[])
	throws IOException, NumberFormatException { 
	int p = Integer.valueOf(args[0]).intValue();
	int l = Integer.valueOf(args[1]).intValue(); 
	ServerSocket s = new ServerSocket(p);
	while (true) {
	    //      Socket c = s.accept();
	    //Worker w = new Worker(c, l);
	    //w.start();
	    Echo.start(s, l);
	}
    }

    static void start(ServerSocket s,int l) throws IOException {
	Socket c = s.accept();
	Worker w = new Worker(c, l);
	w.start();
    }

}
