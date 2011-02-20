// Alert.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CommunicationsModel;
import imagerec.corba.CORBA;
import Img.ImageHeader;

/**
 * {@link Alert} is a {@link Client} which sets an alert syscond
 * for every input image.  This effectively tells the tracking
 * system: "I've found it - and here's how far away it is!".
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Alert extends Client {
    /** Construct an {@link Alert} node with the default {@link CommunicationsModel}
     *  and name of server for integration with existing BBN UAV software. 
     *
     *  @param args The argument list given at the command line.
     *              Should include -ORBInitRef NameService=corbaloc::[host]:[port]/NameService
     *              or -ORBInitRef NameService=file://dir/ns
     */
    public Alert(String args[]) {
	this(new CORBA(args), "ATR Alert");
    }

    /** Construct an {@link Alert} node with <code>name</code> as 
     *  the name of the alert server to connect to.
     *
     *  @param cm a {@link CommunicationsModel} which provides the transport mechanism.
     *  @param name the name of the server to connect to.
     */
    public Alert(CommunicationsModel cm, String name) {
	long timeoutLength = 2000;
	int triesTillFail = 500;
	int count = 1;
	boolean connectionMade = false;
	while ((count <= triesTillFail) && !connectionMade) { 
	    try {
		cs = cm.setupAlertClient(name);
		connectionMade = true;
	    } catch (org.omg.CosNaming.NamingContextPackage.NotFound e){
		System.out.println("Alert/CORBA Exception: No matching server by the name '"+name+"'"+
				   " was found.\n  Waiting and trying again. (Try #"+count+"/"+triesTillFail+")");
		try {
		    Thread.currentThread().sleep(timeoutLength);
		}
		catch (InterruptedException ie) {
		}
		count++;
		
	    } catch (Exception e2) {
		System.out.println("**** "+e2.getClass()+" ******");
		throw new Error(e2);
	    }
	}
	
	if (!connectionMade) {
	    int minutes = (int)timeoutLength*triesTillFail/1000/60;
	    throw new Error("Alert was unable to find a matching server after "+minutes+" minutes. Giving up.");
	}
    }
    

    /** The <code>process</code> call that results in setting the Alert syscond
     *  with information from the input image: <code>(c1, c2, c3)</code>
     *  set with the relative target location.
     *
     *  @param id The input image that contains data for the tracker.
     */
    public void process(ImageData id) {
	//System.out.println("Alert Client #"+getUniqueID()+" sending image #"+id.id);
	//System.out.println("Alert Client:     c1: "+id.c1);
	//System.out.println("Alert Client:     c2: "+id.c2);
	//System.out.println("Alert Client:     c3: "+id.c3);
	final float c1 = id.c1;
	final float c2 = id.c2;
	final float c3 = id.c3;
	final long time = id.time;
	final int idNo = id.id;
	final ImageHeader header = id.header;
	final CommunicationsAdapter finalCS = cs;
	(new Thread() {
	    public void run() {
		//System.out.println("Alert Client:     c1: "+c1);
		//System.out.println("Alert Client:     c2: "+c2);
		//System.out.println("Alert Client:     c3: "+c3);
		
		finalCS.alert(c1, c2, c3, time, header);
		//System.out.println("Alert client #"+getUniqueID()+" sent image #"+idNo);
	    }
	}).start();
	if (getLeft() != null) {
	    getLeft().process(id);
	}
	if (getRight() != null) {
	    getRight().process(id);
	}
    }
}
