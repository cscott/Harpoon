// CommunicationsModel.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

/** A {@link CommunicationsModel}, together with a {@link CommunicationsAdapter}
 *  provide a server/client abstraction that allows pluggable transport mechanisms for
 *  transferring data between image recognition components.  See {@link CommunicationsAdapter}
 *  for details.
 *
 *  @see CommunicationsAdapter
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public interface CommunicationsModel {
    /** 
     *  @param name
     *  @param out
     */
    public void runIDServer(String name, CommunicationsAdapter out) throws Exception;

    /** 
     *  @param name
     *  @return a {@link CommunicationsAdapter} which wraps the client RMI.
     */
    public CommunicationsAdapter setupIDClient(String name);

}
