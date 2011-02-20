// PhoneWorker.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.


import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;

import java.net.Socket;

/**
 * <code>PhoneWorker</code> process requests to the phone server.
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: PhoneWorker.java,v 1.1 2000-07-18 22:18:32 bdemsky Exp $
 */
public class PhoneWorker extends Thread {
    private Str2StrMap theMap;
    private Socket client;
    private String name;
    private String number;
    
    public PhoneWorker(Str2StrMap theMap, Socket client){
	this.theMap = theMap;
	this.client = client;
    }
    
    public void run() {
	BufferedReader in  = null;
	BufferedWriter out = null;
	try {
	    in  = new BufferedReader(
			new InputStreamReader(client.getInputStream()));
	    out = new BufferedWriter(
		        new OutputStreamWriter(client.getOutputStream()));
	    
	    String request = in.readLine();
	    PhoneProtocol req = new PhoneProtocol(request);
	    // System.out.println("received request: " + req.name + " -- " + 
	    //	       req.number);			
	    if (request != null) {
		if (req.type == PhoneProtocol.PUT) {
		    theMap.put(req.name, req.number);
		} else if (req.type == PhoneProtocol.GET) {
		    req.setNumber(theMap.get(req.name));
		} else {
		    req.setError();
		}
	    } else {
		req.setError();
	    }
	    // send the response back to the client
	    out.write(req.request, 0, req.request.length());
	    out.newLine();
	}
	catch(IOException e) {
	    System.err.println(e);
	    return;
	}
	finally {
	    try {
		out.flush();
		out.close();
		in.close();
		client.close();
	    } catch (IOException ex) {
		System.err.println(ex);
	    }
	}
    }
}

