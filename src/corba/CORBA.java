// CORBA.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import FrameManip.Processor;
import FrameManip.ProcessorPOA;
import FrameManip.ProcessorHelper;
import FrameManip.Frame;

import ATRManip.ATRSysCond;
import ATRManip.ATRSysCondPOA;
import ATRManip.ATRSysCondHelper;
import ATRManip.Coordinate;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import omg.org.CosPropertyService.Property;

import imagerec.graph.ImageData;

import imagerec.util.ImageDataManip;

import imagerec.graph.Alert;
import imagerec.graph.ATR;

/** {@link CORBA} provides a transport mechanism for
 *  <a href="http://www.omg.org/gettingstarted/corbafaq.htm">CORBA</a> servers
 *  and clients used in the image recognition program.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class CORBA implements CommunicationsModel {
    private String[] args;

    /** Construct a new {@link CORBA} {@link CommunicationsModel}
     *
     *  @param args should contain the command-line arguments to the program.
     *              It should have an -ORBInitRef NameService=X
     *              where X is the location of the name service and could be 
     *              file://home/wbeebee/.jacorb, for example.
     */
    public CORBA(String[] args) {
	this.args=args;
	setupJacORB();
    }

    /** Sets up the environment variables necessary to run JacORB 1.3.30.
     */
    public static void setupJacORB() {
	System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
	System.setProperty("org.omg.CORBA.ORBSingletonClass", 
			   "org.jacorb.orb.ORBSingleton");
    }
    
    /** Return the naming context associated with an {@link ORB}.
     *
     *  @param orb The orb to find the naming context.
     *  @return A naming context to resolve references.
     */
    protected NamingContextExt namingContext(ORB orb) throws Exception {
	return NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
    }

    /** Create an object that connects to the service bound to <code>name</code> 
     *  in the CORBA NameService.
     *
     *  @param name The name of the client to look up.
     *  @return The object that provides that provides the connection.
     */
    protected org.omg.CORBA.Object setupClient(String name) throws Exception {
	return namingContext(ORB.init(args, null))
	    .resolve(new NameComponent[] {new NameComponent(name, "")});
    }

    /** Run a CORBA server.
     *
     *  @param name The name to bind in the name service.
     *  @param servant The servant that does the work when a request comes in.
     */
    protected void runServer(String name, Servant servant) throws Exception {
	ORB orb = ORB.init(args, null);
	POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	poa.the_POAManager().activate();
	NamingContextExt namingContext = namingContext(orb);
	namingContext.rebind(namingContext.to_name(name),
			     poa.servant_to_reference(servant));
	orb.run();
    }

    /**
     *  Set up a client for sending {@link ImageData}s to the server via JacORB.
     *  @param name The name that's bound to the NameService of the server to
     *              connect to.
     *  @return a {@link CommunicationsAdapter} which wraps the client method to be invoked.
     */
    public CommunicationsAdapter setupIDClient(final String name) 
	throws Exception {
	final ClientServer cs = ClientServerHelper.narrow(setupClient(name));
	return new CommunicationsAdapter() {
	    public void process(ImageData id) {
		cs.process(id);
	    }
	};
    }

    /**
     *  Set up a server to receive {@link ImageData}s 
     *  (used primarily between image recognition components) via JacORB.
     *
     *  @param name The name to bind with the NameService.
     *  @param out  An adapter to send <code>process</code> requests to.
     */
    public void runIDServer(final String name, final CommunicationsAdapter out) 
	throws Exception {
	runServer(name, new ClientServerPOA() {
	    public void process(ImageData id) {
		out.process(id);
	    }
	});
    }

    /**
     *  Run a client that can connect to the BBN UAV OEP object tracker
     *  (via JacORB).
     *  
     *  @param name The name bound by the Alert server
     *  @return a {@link CommunicationsAdapter} which wraps the UAV Alert RMI call.
     */

    public CommunicationsAdapter setupAlertClient(String name) 
	throws Exception {
	final ATRSysCond atr = ATRSysCondHelper.narrow(setupClient(name));
	return new CommunicationsAdapter() {
	    public void alert(float c1, float c2, float c3, long time) {
		atr.send_coordinate(new Coordinate(c1, c2, c3, time));
	    }
	};
    }

    /**
     *  Run an {@link Alert} server that can catch alerts coming out of the ATR.
     *  
     *  @param name The name bound by the Alert server in the CORBA name service.
     *  @param out a {@link CommunicationsAdapter} to send alerts to.
     */
    public void runAlertServer(String name, final CommunicationsAdapter out) 
	throws Exception {
	runServer(name, new ATRSysCondPOA() {
	    public void send_coordinate(Coordinate c) {
		out.alert(c.c1, c.c2, c.c3, c.timestamp);
	    }
	    
	    public boolean isReady() { return true; }
	    public int getLong() { return 0; }
	    public void setLong(int arg) {}
	    public boolean booleanValue() { return false; }
	    public void booleanValue(boolean newValue) {}
	    public byte octetValue() { return 0; }
	    public void octetValue(byte newValue) {}
	    public char charValue() { return 'a'; }
	    public void charValue(char newValue) {}
	    public short shortValue() { return 0; }
	    public void shortValue(short newValue) {}
	    public int longValue() { return 0; }
	    public void longValue(int newValue) {}
	    public long longlongValue() { return 0; }
	    public void longlongValue(long newValue) {}
	    public float floatValue() { return 0; }
	    public void floatValue(float newValue) {}
	    public double doubleValue() { return 0; }
	    public void doubleValue(double newValue) {}
	    public String stringValue() { return ""; }
	    public void stringValue(String newValue) {}
	    public String getName() { return ""; }
	    public void setNotificationThreshold(double d) {}
	});
    }

    /**
     *  Setup a client that can send images to the ATR.
     *
     *  @param name The name of the ATR to connect
     *  @return a {@link CommunicationsAdapter} that can process images by 
     *          sending them to the {@link ATR}.
     */
    public CommunicationsAdapter setupATRClient(String name) 
	throws Exception {
	final Processor p = ProcessorHelper.narrow(setupClient(name));
	return new CommunicationsAdapter() {
	    public void process(ImageData id) {
		p.process(new Frame(id.time, new Property[0], ImageDataManip.writePPM(id)));
	    }
	};
    }

    /**
     *  Run a server that the BBN UAV receiver can connect to (via JacORB).
     *
     *  @param name The name bound by the ATR client (from the receiver node).
     *  @param out a {@link CommunicationsAdapter} to send the process requests to.
     */
    public void runATRServer(String name, final CommunicationsAdapter out) 
	throws Exception {
	runServer(name, new ProcessorPOA() {
	    public Frame transform(Frame f) {
		return null; // Not currently used.
	    }

	    private int idNum = 0;

	    public void process(Frame f) {
		ImageData id = ImageDataManip.readPPM(f.data);
		id.id = idNum++;
		id.time = f.timestamp;
		out.process(id);
	    }
	});
    }
}
