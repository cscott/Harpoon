// PhoneServer.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.


//import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/**
 * Main loop for Phone Server -- listens for connections and spawns
 * worker threads
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: PhoneServer.java,v 1.1 2000-07-18 22:18:32 bdemsky Exp $
 */
public class PhoneServer extends Thread {
    private ServerSocket socket;
    private Str2StrMap theMap;
    
    public PhoneServer(int port) {
	try {
	    theMap = new Str2StrMap();
	    socket = new ServerSocket(port);
	}
	catch (IOException e){
	    e.printStackTrace(System.err);
	    System.exit(1);
	}
    }

    private void startWorker(Socket client) {
	PhoneWorker pw = new PhoneWorker(theMap, client);
	pw.start();
    }
    
    public void run() {
	while (true) {
	    try {
		startWorker(socket.accept());
	    }
	    catch (Exception e){
		System.err.println(e);
	    }
	}
    }
    
    public static void main(String args[]) {
	int port = 0;
	System.out.println("args.length = " + args.length);
	switch(args.length) {
	case 0:
	    port = 8000;
	    break;
				
	case 1:
	    port = Integer.parseInt(args[0]);
	    break;
	    
	default:
	    System.out.println("Usage:\n PhoneServer <port>\n");
	    System.exit(1);
	}
	System.out.println("Starting Phone Server on port " + port);
	PhoneServer ps = new PhoneServer(port);
	ps.start();
    }
}

