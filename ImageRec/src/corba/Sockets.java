// Sockets.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec.corba;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import imagerec.util.ImageDataManip;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import imagerec.graph.ImageData;

import imagerec.graph.Alert;

import Img.ImageHeader;

/** {@link Sockets} provides a simple socket-based transport mechanism
 *  for use with the image recognition program.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Sockets implements CommunicationsModel {
    /** The port on which we should connect. */
    private static final int MAX_CLIENTS = 50;

    public Sockets() {}

    protected OutputStream setupClient(String name) throws Exception {
	int idx = name.indexOf(':');
	String host = name.substring(0, idx);
	int port = Integer.parseInt(name.substring(idx+1));
	try {
	    Socket clientSocket = new Socket(host, port);
	    return clientSocket.getOutputStream();
	} catch (UnknownHostException e) {
	    throw new RuntimeException("Unknown host: "+e);
	} catch (IOException e) {
	    throw new IOException("Difficulty connecting to "+host+": "+e);
	}
    }

    protected InputStream runServer(String name) {
	int port = Integer.parseInt(name);
	ServerSocket listenSocket  = null;
	InputStream is = null;
	int count = 0;
	boolean success = false;
	while (!success) {
	    try {
		listenSocket = new ServerSocket(port, MAX_CLIENTS);
		success = true;
	    } catch (IOException exc) {
		System.err.println("Unable to listen on port " + port + ": " + exc);
		if (count == 10)
		    System.exit(-1);
	    }
	    count++;
	    try {
		Thread.currentThread().sleep(1000);
		System.out.println("Waiting 1 second.");
	    }
	    catch (InterruptedException e) {
	    }

	}
	try {
	    Socket serverSocket = listenSocket.accept();
	    is=serverSocket.getInputStream();
	} catch (IOException exc) {
	    System.err.println("Failed I/O: "+exc);
	    System.exit(-1);
	} catch (RuntimeException e) {
	    System.err.println(e.toString());
	    System.exit(-1);
	}
	return is;
    }

    public CommunicationsAdapter setupIDClient(String name) throws Exception {
	final DataOutputStream out = new DataOutputStream(setupClient(name));
	return new CommunicationsAdapter() {
		public void process(ImageData id) {
		    ImageDataManip.writeSerial(id, out);
		}
	    };
    }

    public void runIDServer(String name, CommunicationsAdapter out) throws Exception {
	final DataInputStream is = new DataInputStream(runServer(name));
	while (true) {	    
	    out.process(ImageDataManip.readSerial(is));
	}
    }

    public CommunicationsAdapter setupAlertClient(String name) throws Exception {
	final DataOutputStream out = new DataOutputStream(setupClient(name));
	return new CommunicationsAdapter() {
		public void alert(float c1, float c2, float c3, long time, ImageHeader header) {
		    try {
			out.writeFloat(c1);
			out.writeFloat(c2);
			out.writeFloat(c3);
			out.writeLong(time);
			if (header == null) {
			    out.writeBoolean(false);
			} else {
			    out.writeBoolean(true);
			    out.writeLong(header.timestamp);
			    out.writeInt(header.importance);
			    out.writeInt(header.id);
			    out.writeInt(header.seq_num);
			}
			out.flush();
		    } catch (IOException e) {
			throw new Error(e.toString());
		    }
		}
	    };
    }

    public void runAlertServer(String name, CommunicationsAdapter out) throws Exception {
	final DataInputStream is = new DataInputStream(runServer(name));
	while (true) {
	    try {
		out.alert(is.readFloat(), is.readFloat(), is.readFloat(), is.readLong(), 
			  is.readBoolean()?new ImageHeader(is.readLong(), is.readInt(), is.readInt(),
							   is.readInt()):null);
	    } catch (IOException e) {
		throw new Error(e.toString());
	    }
	}
    }

    public CommunicationsAdapter setupATRClient(String name) throws Exception {
	final OutputStream out = setupClient(name);
	return new CommunicationsAdapter() {
		public void process(ImageData id) {
		    ImageDataManip.writePPM(id, out, false);
		}
	    };
    }

    public void runATRServer(String name, CommunicationsAdapter out) throws Exception {
	InputStream serverIS = runServer(name);
	while (true) {
	    out.process(ImageDataManip.readPPM(serverIS));
	}
    }
}
