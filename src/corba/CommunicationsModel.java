// CommunicationsModel.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import imagerec.graph.ImageData;

/** A {@link CommunicationsModel}, together with a {@link
 *  CommunicationsAdapter} provide a server/client abstraction that
 *  allows pluggable transport mechanisms for transferring data
 *  between image recognition components.  See {@link CommunicationsAdapter} 
 *  for details.
 *
 *  @see CommunicationsAdapter
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>> */
public interface CommunicationsModel {
    /** 
     *  Set up a server to receive {@link ImageData}s 
     *  (used primarily between image recognition components).
     *
     *  @param name The name of the server to create - use this name on the client side.
     *  @param out  An adapter to send <code>process</code> requests to.
     */
    public void runIDServer(String name, CommunicationsAdapter out) throws Exception;

    /**
     *  Set up a client for sending {@link ImageData}s to the server.
     *  @param name The name of the server to connect to.
     *  @return a {@link CommunicationsAdapter} which wraps the client RMI.
     */
    public CommunicationsAdapter setupIDClient(String name);

    /**
     *  Run a client that can connect to the BBN UAV OEP object tracker.
     * 
     *  @param name The name bound by the Alert server - in the current UAV distribution,
     *              this is "ATR Alert".
     *  @return a {@link CommunicationsAdapter} which wraps the UAV Alert RMI call.
     */
    public CommunicationsAdapter setupAlertClient(String name);

    /**
     *  Run a server that the BBN UAV receiver can connect to.
     *
     *  @param name The name bound by the ATR client (from the receiver node)
     *              in the current UAV distribution, this is "LMCO ATR"
     *              (because Lockheed-Martin wrote the first ATR)
     *  @param out a {@link CommunicationsAdapter} to send the process requests to.
     */
    public void runATRServer(String name, CommunicationsAdapter out) throws Exception;
}
