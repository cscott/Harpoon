// ReceiverStubMain.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

import imagerec.graph.*;

/**
 * This is the main program for the receiver stub that can send a series of images
 * to the ATR in place of the UAV OEP receiver.
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */

public class ReceiverStubMain {

    /** The entry point to the stub of the Receiver main program
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length<6) {
	    System.out.println("Usage: java -jar receiverStub.jar <jarFile|'none'> <filePrefix> <# of images>");
	    System.out.println("            <pipeline #> <CORBA name for ATR> <CORBA name for feedback server> ");
	    System.out.println("            [CORBA options]");
	    System.exit(-1);
	}

	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[3]);
	}
	catch (NumberFormatException nfe) {
	    System.out.println("Error: Pipeline argument was not a valid integer.");
	    System.exit(-1);
	}
	
	if (pipelineNumber <= 0) {
	    System.out.println("Error: Pipeline # must be > 0 (value was '"+pipelineNumber+")");
	    System.exit(-1);
	}
	else {
	    if (pipelineNumber == 1) {
		Node atrClient = new ATRClient(new CORBA(args), args[4]);
		Node load;
		if (args[0].equalsIgnoreCase("none")) {
		    load = new Load(args[1], Integer.parseInt(args[2]), atrClient);
		} else {
		    load = new Load(args[0], args[1], Integer.parseInt(args[2]), atrClient);
		}
		while (true) {
		    load.run();
		}
	    }
	    else if (pipelineNumber == 2) {
		AlertServer feedbackServer = new AlertServer(new CORBA(args), args[5], null);

		Node set = new SetCommonValue("heartbeat", new Boolean(true));

		feedbackServer.linkL(set);
		Thread t = new Thread(feedbackServer);
		t.start(); //calls feedbackServer.run();
		

		Node atrClient = new ATRClient(new CORBA(args), args[4]);
		Node regulate = new Regulator("heartbeat");
		Node load;
		if (args[0].equalsIgnoreCase("none")) {
		    load = new Load(args[1], Integer.parseInt(args[2]), null);
		} else {
		    load = new Load(args[0], args[1], Integer.parseInt(args[2]), null);
		}
		load.linkL(regulate.linkL(atrClient));
		while (true) {
		    load.run();
		}
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    }
	}
    }
}
