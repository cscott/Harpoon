// ATRMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is the main program for the MIT ATR one-component system
 * for use in the BBN UAV OEP.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ATRMain {

    /** The entry point to the ATR main program.
     *
     *  @param args Should include parameters for contacting 
     *              the CORBA nameservice.
     */
    public static void main(String args[]) {
	/*
	 * New development of pipelines can be found in {@link Main} 
	 * (under imagerec.jar).  When pipelines are finished development,
	 * they will be placed in this file.
	 */

	if (args.length<2) {
	    System.out.println("Usage: java -jar ATR.jar <'timer'|'notimer'> <pipeline #> [CORBA options]");
	    System.exit(-1);
	}
	
	if (Integer.parseInt(args[1])!=1) {
	    System.out.println("Pipeline #"+args[1]+" not implemented yet.");
	    System.exit(-1);
	}

	boolean timer = args[0].equalsIgnoreCase("timer");

	Node pipe = new Alert(args);
	if (timer) pipe = new Timer(false, true, pipe);
	pipe = new RobertsCross(new Thresholding(new Label(null, new RangeFind(pipe))));
	if (timer) pipe = new Timer(true, false, pipe);
	(new ATR(args, pipe)).run();
    }
}
