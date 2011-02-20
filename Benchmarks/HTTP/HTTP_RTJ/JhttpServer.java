//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.net.*;
import java.io.*;

public class JhttpServer {
//*****************************************************************************
// Method:  main()
// Purpose: Creates a new JhttpServer based on the parameters.   
//*****************************************************************************

  public static boolean stats = false;
  
  public static void main(String args[]) throws Exception
  {
      try { // Shouldn't need this!
      if (args.length < 3)
	{
	  System.out.println("Usage:");
	  System.out.println("   java JhttpServer <port> <logging> <noRTJ | CT | VT> [stats | nostats] [ctsize]");
	  System.out.println();
	  System.out.println("Ex: java JhttpServer 10000 0 noRTJ");
	  System.exit(1);
	}
      
      if (args[2].equalsIgnoreCase("noRTJ")) {
	(new JhttpServerNoRTJ(Integer.parseInt(args[0]),
			      Integer.parseInt(args[1])==1)).start();
      } else if (args[2].equalsIgnoreCase("CT")) {
	(new JhttpServerRTJ(new javax.realtime.CTMemory(Long.parseLong(args[4])),
			    Integer.parseInt(args[0]),
			    Integer.parseInt(args[1])==1)).start();
	stats = args[3].equalsIgnoreCase("stats");
      } else if (args[2].equalsIgnoreCase("VT")) {
	(new JhttpServerRTJ(new javax.realtime.VTMemory(),
			    Integer.parseInt(args[0]),
			    Integer.parseInt(args[1])==1)).start();
	stats = args[3].equalsIgnoreCase("stats");
      } else {
	System.out.println("Wrong MemoryArea type!");
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      System.exit(1);
    }
  }
}
    
class JhttpServerRTJ extends javax.realtime.RealtimeThread {

    public boolean logging;
    private int port;
    private static int connectionsNum = 0;

    public JhttpServerRTJ(javax.realtime.MemoryArea ma, int port, boolean logging)
    {
	super(ma);
	this.logging=logging;
	System.out.println("starting...");
	this.port=port;
    }

    private void startWorker(Socket client) throws Exception {
	(new JhttpWorker(client, logging)).start();
	//	System.out.println("accepted connection.");
    }

    public void run(){
	ServerSocket server = null;
	try {
	    System.out.println("creating the port");
	    server = new ServerSocket(port);
	}
	catch (IOException e){
	    System.err.println(e);
	    System.exit(1);
	}

	// infinite loop 
	while (true) {
	    try {
		startWorker(server.accept());
		if (JhttpServer.stats) {
		    connectionsNum++;
		    if (connectionsNum > 950) {
			Thread.sleep(60000);  // Sleep for a minute for all threads to stop.
			javax.realtime.Stats.print();
			System.exit(1);
		    }
		}
	    }
	    catch (Exception e){
		System.err.println(e);
	    }
	}
    }
}

class JhttpServerNoRTJ extends java.lang.Thread {

    public boolean logging;
    private int port;

    public JhttpServerNoRTJ(int port, boolean logging)
    {
	super();
	this.logging=logging;
	System.out.println("starting...");
	this.port=port;
    }

    private void startWorker(Socket client) throws Exception {
	(new JhttpWorker(client, logging)).start();
	//	System.out.println("accepted connection.");
    }

    public void run(){
	ServerSocket server = null;
	try{
	    System.out.println("creating the port");
	    server = new ServerSocket(port);
	}
	catch (IOException e){
	    System.err.println(e);
	    System.exit(1);
	}

	// infinite loop 
	while (true) {
	    try {
		startWorker(server.accept());
	    }
	    catch (Exception e){
		System.err.println(e);
	    }
	}
    }
}



