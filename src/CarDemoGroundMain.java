// CarDemoGroundMain.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;

import imagerec.graph.*;

import imagerec.util.RunLength;

import imagerec.corba.CORBA;

public class CarDemoGroundMain {
    /** Ground component of the two-component version of the ATR.
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {	
	if (args.length == 0){
	    System.out.println("Usage: java -jar carDemoGroundATR.jar <pipeline #>");
	    System.out.println("       <compress|nocompress>");
	    System.out.println("       <CORBA name for embedded ATR client>");
	    System.out.println("       <CORBA name for embedded ATR server>");
	    System.out.println("       (CORBA options)");
	    System.out.println("");
	    System.out.println("  The compress option must match that of carDemoEmbeddedATR.jar");
	    System.out.println("  If compression is turned on, the ground ATR will receive");
	    System.out.println("  outlines of objects instead of full color images.");
	    System.exit(-1);
	}
	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[0]);
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
		Node pipe;
		Server embedToGrServer = new Server(new CORBA(args), args[2], null);

		Node ifNotThen = new IfNotThen("ObjectIsTank");
		HumanRecognition human = new HumanRecognition();
		human.setCommonMemory("ObjectIsTank");
		Node goRtCmd = new Command(Command.GO_RIGHT, null);
		Node strip = new Strip();
		Node grToEmbedClient = new Client(new CORBA(args), args[3]);
		
		pipe = embedToGrServer.linkL(ifNotThen.link(human.linkL(strip.linkL(grToEmbedClient)),
							    goRtCmd.linkL(strip)));
		
		//if compression is selected,
		//
		boolean willCompress = args[1].equalsIgnoreCase("compress");
		if (willCompress) {
		    Node decompress = new Decompress(new RunLength(), null);
		    embedToGrServer.linkL(decompress.linkL(ifNotThen));
		}
		
		pipe.run();
		
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    
	    }
	}//else [if(pipelineNumber <= 0)]
    }//public static void main()
}//public class CarDemoGroundMain
