// ConnectionManager.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/** ConnectionManager manages the network interaction between schedulers
 *  using a number of different possible transport mechanisms 
 *  (JacORB, ZEN, Sockets).
 *
 *  @author Wes Beebee (<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>)
 */

public class ConnectionManager {
    /** Use JacORB v1.3.30 as the CORBA implementation. */
    public static final int JACORB = 0;

    /** Use ZEN as the CORBA implementation. */
    public static final int ZEN = 1;

    /** Use sockets instead of a CORBA implementation. */
    public static final int SOCKETS = 2;

    /** Set this so that javac can statically know which implementation is used. */
    public static final int implementation = SOCKETS;

    /** Initialization string for CORBA implementations. */
    private String[] args = new String[] { "-ORBInitRef",
					   "NameService="+
					   "file://home/wbeebee/Harpoon/ImageRec/.jacorb" };

    /** How many clients can connect maximum to this server SOCKET */
    private static final int MAX_CLIENTS = 50;

    String schedulerName;
    Scheduler scheduler;
    Object destination;
    long messageID;
    byte[] data;

    private RealtimeThread serverThread;
    private RealtimeThread clientThread;

    /** Construct a new <code>ConnectionManager</code> and set up the environment
     *  of the transport mechanism.
     */
    public ConnectionManager() {
	switch (implementation) {
	case JACORB: {
	    System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
	    System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
	    break;
	}
	case ZEN: {
	    System.setProperty("org.omg.CORBA.ORBClass", "edu.uci.ece.zen.orb.ORB");
	    System.setProperty("org.omg.CORBA.ORBSingletonClass", "edu.uci.ece.zen.orb.ORBSingleton");
	    break;
	}
	case SOCKETS: {
	    break;
	}
	default: { 
	    NoHeapRealtimeThread.print("Invalid transport mechanism chosen!\n"); 
	    System.exit(-1);
	}
	}
	
	if (implementation == SOCKETS) {
	    serverThread = new RealtimeThread() {
		    public synchronized void run() {
			wait();
			int port = Integer.parseInt(ConnectionManager.this.schedulerName);
			ServerSocket listenSocket = null;
			DataInputStream is = null;
			try {
			    listenSocket = new ServerSocket(port, MAX_CLIENTS);
			} catch (IOException exc) {
			    NoHeapRealtimeThread.print("Unable to listen on port ");
			    NoHeapRealtimeThread.print(port);
			    NoHeapRealtimeThread.print(": ");
			    NoHeapRealtimeThread.print(exc.toString());
			    NoHeapRealtimeThread.print("\n");
			}
			while (true) {
			    try {
				is = new DataInputStream(listenSocket.accept().getInputStream());
				String name = is.readUTF();
				long messageID = is.readLong();
				byte[] data = new byte[is.readInt()];
				is.readFully(data);
				scheduler.handleDistributedEvent(name, messageID, data);
			    } catch (IOException exc) {
				NoHeapRealtimeThread.print("Failed I/O: ");
				NoHeapRealtimeThread.print(exc.toString());
				NoHeapRealtimeThread.print("\n");
				System.exit(-1);
			    } catch (RuntimeException e) {
				NoHeapRealtimeThread.print(e.toString());
				NoHeapRealtimeThread.print("\n");
				System.exit(-1);
			    }
			}
		    }
		};

	    clientThread = new RealtimeThread() {
		    public synchronized void run() {
			while (true) {
			    wait();
			    String name = (String)ConnectionManager.this.destination;
			    int idx = name.indexOf(':');
			    String host = name.substring(0, idx);
			    int port = Integer.parseInt(name.substring(idx+1));
			    DataOutputStream d = null;
			    try {
				Socket clientSocket = new Socket(host, port);
				d = new DataOutputStream(clientSocket.getOutputStream());
				d.writeUTF(ConnectionManager.this.schedulerName);
				d.writeLong(ConnectionManager.this.messageID);
				d.writeInt(ConnectionManager.this.data.length);
				d.write(ConnectionManager.this.data, 0, ConnectionManager.this.data.length);
				d.flush();
			    } catch (UnknownHostException e) {
				NoHeapRealtimeThread.print("Unknown host: ");
				NoHeapRealtimeThread.print(e.toString());
				System.exit(-1);
			    } catch (IOException e) {
				NoHeapRealtimeThread.print("Difficulty connecting to ");
				NoHeapRealtimeThread.print(host);
				NoHeapRealtimeThread.print(": ");
				NoHeapRealtimeThread.print(e.toString());
				System.exit(-1);
			    }
			}
		    }
		};
	} else {
	    serverThread = new RealtimeThread() {
		    public synchronized void run() {
			try {
			    wait();
			    ORB orb = ORB.init(ConnectionManager.this.args, null);
			    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			    poa.the_POAManager().activate();
			    NamingContextExt namingContext = 
				NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
			    namingContext.rebind(namingContext.to_name(ConnectionManager.this.schedulerName),
						 poa.servant_to_reference(new SchedulerCommPOA() {
							 public void handleDistributedEvent(String name, 
											    long messageID, byte[] data) {
							     ConnectionManager.this.scheduler.handleDistributedEvent(name, messageID, data);
							 }				
						     }));
			    orb.run();
			} catch (Exception e) {
			    NoHeapRealtimeThread.print(e.toString());
			    System.exit(-1);
			}
		    }
		};

	    clientThread = new RealtimeThread() {
		    public synchronized void run() {
			wait();
			((SchedulerComm)destination).handleDistributedEvent(ConnectionManager.this.schedulerName, 
									    ConnectionManager.this.messageID, 
									    ConnectionManager.this.data);
		    }
		};
	}
    }

    /** Start the server/client threads. */
    public void start() {
	serverThread.start();
	clientThread.start();
    }

    /** Bind the current scheduler to <code>name</code> in the CORBA name service
     *  or start a server to listen at the socket for this scheduler.
     *
     *  @param name the name to bind
     *  @return thread id of thread which handles network calls.
     */
    public long bind(String name, Scheduler scheduler) {
	this.scheduler = scheduler;
	this.schedulerName = name;
	serverThread.notify();
	return serverThread.getUID();
    }

    /** Resolve the <code>name</code> in the name service to a 
     *  CORBA stub which represents the destination scheduler.
     *
     *  @param name The name to resolve.
     *  @return The object bound in the name server.
     */    
    public Object resolve(String name) {
	if (implementation == SOCKETS) {
	    return name;
	} else {
	    try {
		ORB orb = ORB.init(args, null);
		NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
		return SchedulerCommHelper.narrow(nc.resolve(new NameComponent[] {new NameComponent(name, "")}));
	    } catch (Exception e) {
		NoHeapRealtimeThread.print(e.toString());
		System.exit(-1);
	    }
	}
	return null;
    }

    /** Create an event on the <code>destination</code> scheduler
     *  using the transport mechanism.
     *
     *  This calls <code>addThread</code> to add the event.
     *
     *  <code>chooseThread</code> should switch to the thread to 
     *  generate the event.
     *
     *  <code>removeThread</code> will be called when or if the
     *  event has been handled.
     *
     *  @param destination The scheduler to send the message to
     *  @param messageID The id to send
     *  @param data The data to send with the message
     * 
     *  @return The thread which is handling the event.
     */
    public long generateDistributedEvent(Object destination,
					 long messageID, byte[] data) {
	this.destination = destination;
	this.messageID = messageID;
	this.data = data;
	clientThread.notify();
	return clientThread.getUID();
    }
}

