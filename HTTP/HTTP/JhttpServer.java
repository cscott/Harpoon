
//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.net.*;
import java.io.*;

public class JhttpServer extends Thread{

    private ServerSocket server;
    public boolean logging;
//****************************************************************************
// Constructor: JhttpServer(int)
//****************************************************************************
    public JhttpServer(int port, boolean logging)
    {
	this.logging=logging;
	System.out.println("starting...");
	try{
	    System.out.println("creating the port");
	    server = new ServerSocket(port);
	}
	catch (IOException e){
	    System.err.println(e);
	    System.exit(1);
	}
    }

    private void startWorker(Socket client) throws Exception {
	(new JhttpWorker(client,logging)).start();
	//	System.out.println("accepted connection.");
    }

    public void run(){
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
    if (args.length < 1)
    {
      System.out.println("Usage:");
      System.out.println("   java JhttpServer <port> <logging>");
      System.out.println();
      System.out.println("Ex: java JhttpServer 10000 0");
      System.exit(1);
    }

    (new JhttpServer(Integer.parseInt(args[0]),
		     Integer.parseInt(args[1])==1))
	.start();
  }
}


