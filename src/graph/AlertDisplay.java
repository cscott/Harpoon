// AlertDisplay.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * This is a {@link Node} which displays alerts in text to the screen.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class AlertDisplay extends Node {
    /** Construct an {@link AlertDisplay} node. 
     */
    public AlertDisplay() {
	super();
    }

    /** The <code>process</code> call that displays alert data.
     *
     *  @param id The {@link ImageData} that contains the alert data.
     */   
    public void process(ImageData id) {
	System.out.println("ALERT: ("+id.c1+","+id.c2+","+id.c3+")");
    }
}
