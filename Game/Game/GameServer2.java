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
	    GWorker w1= new GWorker(s.accept());
	    GWorker w2= new GWorker(s.accept());

	    w1.init(w2, "Player 1");
	    w2.init(w1, null);

	    w1.start();
	    w2.start();
	}
    }

    static class GWorker extends Thread 
    {
	Socket s;
	InputStream is;
	OutputStream os;
	PrintStream ps;

	GWorker partner;
	
	public GWorker(Socket s) 
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
