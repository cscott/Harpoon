
//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.net.*;
import java.io.*;

public class JhttpServer extends Thread{

    private ServerSocket server;
    RoleI rolei;

//****************************************************************************
// Constructor: JhttpServer(int)
//****************************************************************************
    public JhttpServer(int port)
    {
	System.out.println("starting...");
	try{
	    System.out.println("creating the port");
	    server = new ServerSocket(port);
	}
	catch (IOException e){
	    System.err.println(e);
	    System.exit(1);
	}
	rolei=new RoleI();
    }

    private void startWorker(Socket client) throws Exception {
	(new JhttpWorker(client,false,rolei)).start();
	//	System.out.println("accepted connection.");
    }

    public void run(){
	// infinite loop 
	while (true){
	    try{
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
      System.out.println("   java JhttpServer <port>");
      System.out.println();
      System.out.println("Ex: java JhttpServer 10000");
      System.exit(1);
    }
    (new JhttpServer(Integer.parseInt(args[0]))).start();
  }
}




