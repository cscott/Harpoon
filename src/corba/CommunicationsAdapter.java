// CommunicationsAdapter.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import imagerec.graph.ImageData;

/** This class, together with {@link CommunicationsModel} provide
 *  a server/client abstraction that allows pluggable transport
 *  mechanisms for transferring data between image recognition
 *  components.  Planned targets include CORBA (JacORB), RT-CORBA
 *  (TAO), Quoskets, and Infopipes.  Also, wrappers will hopefully
 *  eventually be available for incorporation into the BBN UAV
 *  (MPEG/PPM, Corba A/V service and setting Alert SysConds).
 *
 *  @see CommunicationsModel
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>> 
 */
public class CommunicationsAdapter {

    /** This provides a default implementation of the <code>process</code> method.
     *  Please override this method if your transport mechanism supports transferring
     *  {@link ImageData}s.
     *
     *  @param id The {@link ImageData} to transport.
     */
    public void process(ImageData id) {
	throw new Error("This should be overridden if callable.");
    }

    /** This provides a default implementation of the <code>alert</code> method.
     *  This is overriden by the {@link CommunicationsAdapter} returned by 
     *  {@link CommunicationsModel}<code>.setupAlertClient</code>.
     *
     *  An alert contains information of the relative location of the target in
     *  (x,y,z) coordinates.  The system is first calibrated with the object
     *  placed one meter away.
     *  
     *  @param c1 The x coordinate relative to the current location in meters.
     *  @param c2 The y coordinate relative to the current location in meters.
     *  @param c3 The z coordinate relative to the current location in meters.
     */
    public void alert(float c1, float c2, float c3, long time) {
	throw new Error("This should be overridden if callable.");
    }
}
