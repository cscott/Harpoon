package harpoon.Test.PA.TestS;

import java.io.*;
import java.net.*;


public class GameServer2 {

    static int port= 4500;
    static int buflen= 20;

    public static void main(String args[]) throws Exception
    {
	switch(args.length) {
	case 2: buflen= Integer.parseInt(args[1]);
	case 1: port= Integer.parseInt(args[0]);
	}
	
	System.out.println("Server ready, port "+port+", buflen "+buflen);

	ServerSocket s= new ServerSocket(port);
	
	while(true) {
	    Worker w1= new Worker(s.accept());
	    Worker w2= new Worker(s.accept());

	    w2.startWith(w1,null);
	    w1.startWith(w2,"Player 1");
	}
    }

    static class Worker extends Thread 
    {

	Socket s;
	InputStream is;
	OutputStream os;
	PrintStream ps;

	Worker partner;
	
	public Worker(Socket s) 
	{
	    this.s= s;
	    try {
		is= s.getInputStream();
		os= s.getOutputStream();
		ps= new PrintStream(os);

	    } catch (IOException e) {
		System.err.print("Got: ");
		e.printStackTrace(System.err);
	    }
	}

	public void startWith(Worker w, String toPrint)
	{
	  if (toPrint!=null)
	    ps.println(toPrint);
	  partner= w;
	  start();
	}

	public void run()
	{
	    try {
		byte buffer[]= new byte[buflen];

		while(true) {
		    int length= is.read(buffer, 0, buflen);
		    synchronized (this) {
			if (length==-1 || partner == null) break;
			partner.os.write(buffer, 0, length);
		    }
		}
		if (partner != null)
		    synchronized(partner) {		
			partner.partner= null;
		    }

	        s.close();

	    } catch (IOException e) {
		System.err.print("Got: ");
		e.printStackTrace(System.err);
	    }
	}
    }
}
