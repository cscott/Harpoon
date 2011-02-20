// PhoneClient.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.


import java.io.*;
import java.net.*;

/**
 * <code>PhoneClient</code> process requests to the phone server.
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: PhoneClient.java,v 1.1 2000-07-18 22:18:32 bdemsky Exp $
 */
class PhoneClient {
    public static void main(String args[]) {
	String filename = null;		
	int port = 8000;
	String host = "localhost";
	
	BufferedReader input = null;
	InetAddress addr = null;
	String request = null;
	
	switch(args.length) {
	case 1:
	    filename = args[0];
	    break;	    

	case 2:
	    host = args[0];
	    filename = args[1];
	    break;

	case 3:
	    host = args[0];
	    port = Integer.parseInt(args[1]);
	    filename = args[2];
	    break;
	    
	default:
	    System.out.println("Usage:\n\t" + 
			       "PhoneClient [host [port]] <filename>\n");
	    System.exit(1);
	}
	System.out.println("Contacting " + host + ":" + port + 
			   " using input file " + filename);
	try {
	    addr = InetAddress.getByName(host);
	    input = new BufferedReader(new FileReader((filename)));
	}
	catch (Exception e) {
	    System.err.println(e);
	    System.exit(1);
	}
	
	try {
	    while (null != (request = input.readLine())) {
		//System.out.println("Sending request: " + request);
		Socket s = new Socket(addr, port);
		BufferedReader in  = new BufferedReader(
			new InputStreamReader(s.getInputStream()));
		BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(s.getOutputStream()));
		out.write(request, 0, request.length());
		out.newLine();
		out.flush();
		System.out.println(in.readLine());
		in.close();
		out.close();
		s.close();
	    }
	    input.close();
	}
	catch (Exception e) {
	    System.out.println(e);
	}
    }
}
