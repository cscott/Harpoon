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
	    Lock sema=new Lock();
	    GWorker w1= new GWorker(s.accept(),sema);
	    GWorker w2= new GWorker(s.accept(),sema);

	    w1.init(w2, "Player 1");
	    w2.init(w1, null);

	    w1.start();
	    w2.start();
	}
    }

    static class Lock {
	public boolean lock;
    }

    static class GWorker extends Thread 
    {
	Socket s;
	InputStream is;
	OutputStream os;
	PrintStream ps;
	Lock lock;
	GWorker partner;
	
	public GWorker(Socket s, Lock sema) 
	{
	    this.s= s;
	    try {
		is= s.getInputStream();
		os= s.getOutputStream();
		ps= new PrintStream(os);
		lock=sema;
	    } catch (IOException e) {
		System.err.print("Got: ");
		e.printStackTrace(System.err);
	    }
	}

	public void init(GWorker w, String toPrint){
	  if (toPrint!=null)
	    ps.println(toPrint);
	  partner = w;
	}

	public void run()
	{
	    try {
		byte buffer[]= new byte[buflen];

		while(true) {
		    int length= is.read(buffer, 0, buflen);
		    synchronized (lock) {
			lock.lock=true;
		    }
		    
		    if (length==-1 || partner == null) break;
		    partner.os.write(buffer, 0, length);

		    synchronized (lock) {
			lock.lock=false;
			lock.notify();
		    }
		}

		synchronized(lock) {		
		    while(lock.lock==true)
			try {
			    lock.wait();
			} catch (InterruptedException ee) {}
		    if (partner != null)
			partner.partner= null;
		}

	        s.close();

	    } catch (IOException e) {
		synchronized(lock) {		
		    while(lock.lock==true)
			try {
			    lock.wait();
			} catch (InterruptedException ee) {}
		    if (partner != null)
			partner.partner= null;
		}
		try {
		    s.close();
		} catch (Exception ee) {}
	    }
	}
    }
}
