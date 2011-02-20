// GroundATRMain.java, created by Amerson Lin
//                     modified by Reuben Sterling
// Copyright (C) 2003 Amerson H. Lin <amerson@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;
import imagerec.corba.CORBA;
import imagerec.graph.*;

/**
 * This runs the ground component of the two-component ATR.
 *
 * The ground computer runs the CPU intensive portion of the two-part image recognition pipeline.
 *
 * @author Amerson H. Lin <<a href="mailto:amerson@mit.edu">amerson@mit.edu</a>>
 */

public class GroundATRMain {
    /** Ground component of the two-component version of the ATR.
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length < 5){
	    System.out.println("Usage: java -jar groundATR.jar <'timer'|'notimer'> <'head'|'headless'> <pipeline #>");
	    System.out.println("             <CORBA name for embedded ATR client>");
	    System.out.println("             <CORBA name for embedded ATR server>");
	    System.out.println("             [CORBA options]");
	    System.exit(-1);
	}
	
	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[2]);
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
	    boolean timer = args[0].equalsIgnoreCase("timer");
	    Node timer1;
	    Node timer2;
	    //if timer is requested, then timer1/timer2 are made to be Timer nodes,
	    //otherwise, they become dummy Nodes, and ImageDatas will just be passed through
	    //with minimal performance impact
	    if (timer) {
		System.out.println("Warning: You included the 'timer' command argument,");
		System.out.println("         but timers are not yet implemented for the ground ATR.");
		timer1 = new Timer(true, false, null);
		timer2 = new Timer(false, true, null);
	    }
	    else {
		timer1 = new Node();
		timer2 = new Node();
	    }

	    if (pipelineNumber == 1) {

		boolean headless = args[1].equalsIgnoreCase("headless");
		Node blueDisp;
		if (!headless) {
		    blueDisp = new Display("labelBlue");
		}
		else {
		    blueDisp = new Node();
		}

		Node pipe;
		Server embedToGrServer = new Server(new CORBA(args), args[3], null);

		Node ifNotThen = new IfNotThen("ObjectIsBlue");
		LabelBlue labelblue = new LabelBlue(null, null);
		labelblue.setCommonMemory("ObjectIsBlue");
		Node goRtCmd = new Command(Command.GO_RIGHT, null);
		Node strip = new Strip();
		Node grToEmbedClient = new Client(new CORBA(args), args[4]);
		
		pipe = embedToGrServer.linkL(ifNotThen.link(labelblue.linkL(blueDisp.linkL(strip.linkL(grToEmbedClient))),
							    goRtCmd.linkL(strip)));
		pipe.run();
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    }
	}//ends else [matches if(pipelineNumber <= 0)]
	
    }// ends public static void main
}

