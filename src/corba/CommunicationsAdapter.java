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
    public synchronized void process(ImageData id) {
	throw new Error("This should be overridden if callable.");
    }
}
