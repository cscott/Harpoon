// IPaqServoController.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package servo;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@alum.mit.edu">wbeebee@alum.mit.edu</a>>
 */

/** Class <code>IPaqServoController</code> provides IO capability with the IPaq serial port at
 *  speeds that the Jameco SV203 serial servo controller can respond to.  This class supports the entire 
 *  Jameco SV203 servo controller command set.  It can be run as a server, a client, or locally.
 *  Running IPaqServoController will create the server.
 */

public class IPaqServoController {
    /** The port on which the servo should listen. */
    private static final int SERVER_PORT = 17010; 
    private static final int MAX_CLIENTS = 50;

    private Socket clientSocket;
    private OutputStream out;
    private String host;
    
    public IPaqServoController() {
	setup();
	clientSocket = null;
	out = null;
	host = null;
    }

    public IPaqServoController(String host) {
	connect(this.host = host);
    }

    private final void connect(String host) {
	try {
	    clientSocket = new Socket(host, SERVER_PORT);
	    out = clientSocket.getOutputStream();
	} catch (UnknownHostException e) {
	    throw new RuntimeException("Unknown host: "+e);
	} catch (IOException e) {
	    throw new RuntimeException("Difficulty connecting to "+host+": "+e);
	}
    }

    private synchronized final void send(String s) {
	try {
	    char c[] = s.toCharArray();
	    for (int i = 0; i<c.length; i++) {
		out.write((int)c[i]);
	    }
	    out.write(13);
	    out.write(10);
	    out.flush();
        } catch (IOException e) {
	    throw new RuntimeException("Can't write output! "+e);
	}
    }

    public void moveLocal(int servo, int position) {
	if ((servo>8)||(servo<1)) {
	    throw new RuntimeException("Servo is out of range.");
	}
	if ((position>255)||(position<1)) {
	    throw new RuntimeException("Position is out of range.");
	}
	sendSerial("BD0SV"+servo+"M"+position);
    }
    
    public void move(int servo, int position) {
	if ((servo>8)||(servo<1)) {
	    throw new RuntimeException("Servo is out of range.");
	}
	if ((position>255)||(position<1)) {
	    throw new RuntimeException("Position is out of range.");
	}
	send("BD0SV"+servo+"M"+position);
    }

    public void moveRelativeLocal(int servo, int increment) {
	if ((servo>8)||(servo<1)) {
	    throw new RuntimeException("Servo is out of range.");
	}
	if ((increment>127)||(increment<-128)) {
	    throw new RuntimeException("Increment is out of range.");
	}
	sendSerial("BD0SV"+servo+"I"+increment);
    }

    public void moveRelative(int servo, int increment) {
	if ((servo>8)||(servo<1)) {
	    throw new RuntimeException("Servo is out of range.");
	}
	if ((increment>127)||(increment<-128)) {
	    throw new RuntimeException("Increment is out of range.");
	}
	send("BD0SV"+servo+"I"+increment);
    }
    
    public void delayLocal(long millis) {
	if ((millis<1)||(millis>65535)) {
	    throw new RuntimeException("Delay out of range.");
	}
	sendSerial("BD0D"+millis);
    }

    public void delay(long millis) {
	if ((millis<1)||(millis>65535)) {
	    throw new RuntimeException("Delay out of range.");
	}
	send("BD0D"+millis);
    }

    private static native final void setup();
    
    private static native final void sendSerial(byte b);
    private static native final byte readSerial();

    private static final synchronized void sendSerial(String s) {
	char c[] = s.toCharArray();
	for(int i=0; i<c.length; i++) {
	    sendSerial((byte)c[i]);
	}
    }

    public static void main(String args[]) {
	ServerSocket listenSocket  = null;
	setup();
	try {
	    listenSocket = new ServerSocket(SERVER_PORT, MAX_CLIENTS);
	} catch (IOException exc) {
	    System.err.println("Unable to listen on port " + SERVER_PORT + ": " + exc);
	    System.exit(-1);
	}
	try {
	    Socket serverSocket = listenSocket.accept();
	    InputStream serverIS = serverSocket.getInputStream();
	    while (true) {
		char c = (char)serverIS.read();
		if (((c>='A')&&(c<='Z'))||((c>='0')&&(c<='9'))||(c==10)||(c==13)||(c=='-')) {
   	            sendSerial((byte)c);
		}
	    }
	} catch (IOException exc) {
	    System.err.println("Failed I/O: "+exc);
	    System.exit(-1);
	} catch (RuntimeException e) {
	    System.err.println(e.toString());
	    System.exit(-1);
	}
    }

}
