// WatermarkMain.java, created by wbeebee, harveyj
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>, Harvey Jones <harveyj@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is a node watermarking pipeline.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 * @author Harvey Jones <<a href="mailto:harveyj@mit.edu">harveyj@mit.edu</a>>
 */

public class WatermarkMain {

    /** The entry point to the ATR main program.
     *
     *  @param args Should include parameters for contacting 
     *              the CORBA nameservice.
     */
    public static void main(String args[]) {
	if(args.length < 3){
	    System.out.println("Usage: java -jar Watermark.jar fileprefix filesuffix numOfImages");
	    System.exit(-1);
	}
	(new Load("Watermark.jar", args[0], java.lang.Integer.parseInt(args[2]), new Watermark(new Save(args[1], true, false)))).run();

    }//public static void main()
}//public class WatermarkMain
