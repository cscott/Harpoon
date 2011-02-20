// Server.java, created Sun Jan 23 17:36:20 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test2;


import java.util.Vector;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;

/**
 * <code>Server</code> is a test for the PA algorithm taken
 from the second example of the first paper on compositional PA
 written by John Whaley and Martin Rinard (Section 2.2 Thread Private
 Objects).

 <code>Server.run</code> is the interesting method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Server.java,v 1.2 2002-02-25 21:07:29 cananian Exp $
 */
public class Server extends Thread{

    ServerSocket serverSocket;
    int duplicateConnections;

    /** Creates a <code>Server</code>. */
    Server(ServerSocket s){
	serverSocket = s;
	duplicateConnections = 0;
    }

    public void run(){
	try{
	    Vector connectorList = new Vector();
	    while(true){
		Socket clientSocket = serverSocket.accept();
		new ServerHelper(clientSocket).start();
		InetAddress addr =
		    clientSocket.getInetAddress();
		if(connectorList.indexOf(addr) < 0)
		    connectorList.addElement(addr);
		else duplicateConnections++;
	    }
	}catch(IOException e){}
    }

    // We need some top level procedure to create at least one Server
    // object, otherwise the call graph simply ignores its methods.
    // We hope no dead code elimination is done before the PA ...
    public static void main(String[] params){
	try{
	    Server server = new Server(new ServerSocket(50000));
	    server.start();
	}catch(java.io.IOException exc){}
    }
}

class ServerHelper extends Thread{
    Socket clientSocket;

    ServerHelper(Socket clientSocket){
	this.clientSocket = clientSocket;
    }

    public void run(){
	return;
    }
}


