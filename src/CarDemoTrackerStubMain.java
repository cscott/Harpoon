// CarDemoReceiverStubMain.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;

import imagerec.graph.*;

import imagerec.corba.CORBA;

public class CarDemoTrackerStubMain {
    public static void main(String args[]) {
	if (args.length==0) {
	    System.out.println("Usage: java -jar carDemoTrackerStub.jar");
	    System.out.println("            <pipeline #> <CORBA name> (CORBA options)");
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
		AlertServer alertServer = new AlertServer(new CORBA(args), args[1], null);
		Node alertDisp = new AlertDisplay();
		Node car = new CarController();
		Node servo = new Servo();
		
		alertServer.linkL(alertDisp.linkL(car.linkL(servo)));
		alertServer.run();
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
	    
	    }
	}//else [if(pipelineNumber <= 0)]
	
    }
}
