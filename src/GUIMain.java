// GUIMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is the main program for displaying the results of the 
 * image recognition pipeline on a test suite.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class GUIMain {
    /** The entry point to the GUI display of the image recognition pipeline.
     *
     *  @param args foo
     */
    public static void main(String args[]) {
	if (args.length<1) {
	    System.out.println("Usage: java -jar GUI.jar <'pause'|'nopause'> <'corba'|'nocorba'> <pipeline #> [CORBA options]");
	    System.exit(-1);
	}

	if (Integer.parseInt(args[2])!=1) {
	    System.out.println("Pipeline #"+args[2]+" not implemented yet.");
	    System.exit(-1);
	}

	boolean pause = args[0].equalsIgnoreCase("pause");
	boolean corba = args[1].equalsIgnoreCase("corba");
	
	Node command = new Command(Command.GET_IMAGE, null);
	Node circle = new Circle(new Display("identified"), command);
	Node command2 = new Command(Command.RETRIEVED_IMAGE, circle);
	if (corba) circle = new Node(new RangeFind(new Alert(args)), circle);	
	Node cache = new Cache(2, new Copy(new RobertsCross(new Thresholding(
		     new Label(null, circle)))), command2);
	command.setLeft(cache);
	Node n1 = new Node(new Display("original"), cache);
	if (pause) n1 = new Pause(n1);
	n1 = corba?(Node)(new ATR(args, n1)):(Node)(new Load("GUI.jar", "tank.gz", 533, n1));

	while (true) {
	    n1.run();
	}
	
    }

}
