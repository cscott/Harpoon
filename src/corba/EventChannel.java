// EventChannel.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import com.lmco.eagan.pces_demo.messaging.mif.MIF_Sender;
import com.lmco.eagan.pces_demo.tracker.ContactMessage;
import com.lmco.eagan.pces_demo.tracker.XYZT;

/** {@link EventChannel} is a {@link CORBA} that can connect to the 
 *  Lockheed-Martin tracker.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>
 */
public class EventChannel extends CORBA {
    private MIF_Sender mifSender;
    
    public EventChannel(String[] args, String name) {
	super(args);
	mifSender = new MIF_Sender(name);
    }
    
    public CommunicationsAdapter setupAlertClient(String name) {
	return new CommunicationsAdapter() {
		public void alert(float c1, float c2, float c3, long time) {
		    XYZT xyzt = new XYZT(c1, c2, c3, time, 1);
		    ContactMessage cm = new ContactMessage(xyzt);
		    mifSender.send(cm);
		}
	    };
    }
}
