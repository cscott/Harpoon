//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.net.*;
import java.io.*;

public class JhttpServer extends Thread{

    private ServerSocket server;
    int num;
    boolean log;

//****************************************************************************
// Constructor: JhttpServer(int)
//****************************************************************************
    public JhttpServer(int port, int num, boolean log)
    {
	this.num=num;
	this.log=log;
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


    public void run(){
	// infinite loop 
	long count=0;
	long time=System.currentTimeMillis();
	while (true){
	    try{
		Socket client = server.accept();
		(new JhttpWorker(client, log)).start();
//		System.out.println("accepted connection.");
		count++;
		if ((count%num)==0) {
			long ntime=System.currentTimeMillis();
			System.out.println((double)num/(ntime-time)+" connections/mS");
			time=ntime;
		}
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
      System.out.println("   java JhttpServer <port> <#stats> <log files>");
      System.out.println();
      System.out.println("Ex: java JhttpServer 10000 1000 1");
      System.exit(1);
    }

    (new JhttpServer(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2])==1)).start();
  }
}


