// PhoneClient.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test20;

import java.io.*;
import java.net.*;

/**
 * <code>PhoneClient</code> process requests to the phone server.
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: PhoneClient.java,v 1.1 2000-04-02 22:37:43 govereau Exp $
 */
class PhoneClient {

	public static void main(String args[]) {
		int port = 8000;
		String name = null;
		String number = null;
		String host = "localhost";
		
		System.out.println("args.length = " + args.length);
		switch(args.length) {
			case 1:
				name = args[0];
				break;

			case 2:
				name = args[0];
				number = args[1]; // this is ambiguous
				break;

			case 4:
				host = args[0];
				port = Integer.parseInt(args[1]);
				name = args[2];
				number = args[3]; // this is ambiguous
				break;
				
			default:
				System.out.println("Usage:\n PhoneClient [host port] <name> [number]\n");
				System.exit(1);
		}
		System.out.println("Contacting " + host + ":" + port + " with " + name + "," + number);
		try {
			PhoneProtocol req = new PhoneProtocol(name, number);
			Socket s = new Socket(InetAddress.getByName(host), port);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			System.out.println("" + req.request);
			out.write(req.request, 0, req.request.length());
			out.newLine();
			System.out.println("Response:" + in.readLine());
			in.close();
			out.close();
			s.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
