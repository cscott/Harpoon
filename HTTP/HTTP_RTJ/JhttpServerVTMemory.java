//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import javax.realtime.VTMemory;
import javax.realtime.RealtimeThread;
import java.net.*;
import java.io.*;

public class JhttpServerVTMemory extends RealtimeThread {

    public boolean logging;
    private int port;

//****************************************************************************
// Constructor: JhttpServer(int)
//****************************************************************************
    public JhttpServerVTMemory(int port, boolean logging)
    {
	super(new VTMemory(10000, 10000));
	this.logging=logging;
	System.out.println("starting...");
	this.port=port;
    }

    private void startWorker(Socket client) throws Exception {
	(new JhttpWorkerVTMemory(client, logging)).start();
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
  
//*****************************************************************************
// Method:  main()
// Purpose: Creates a new JhttpServer based on the parameters.   
//*****************************************************************************

  public static void main(String args[]) throws Exception
  {
      try { // Shouldn't need this!
      if (args.length < 1)
	{
	  System.out.println("Usage:");
	  System.out.println("   java JhttpServer <port> <logging>");
	  System.out.println();
	  System.out.println("Ex: java JhttpServer 10000 0");
	  System.exit(1);
	}
      
      
      (new JhttpServerVTMemory(Integer.parseInt(args[0]),
			       Integer.parseInt(args[1])==1))
	.start();
    } catch (Exception e) {
      System.out.println(e.toString());
      System.exit(1);
    }
  }
}


