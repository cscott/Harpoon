package org.jacorb.orb.dns;

import java.net.InetAddress;

/**
 * DNSLookupDelegate.java
 *
 *
 * Created: Thu Apr  5 10:54:29 2001
 *
 * @author Nicolas Noffke
 * @version $Id: DNSLookupDelegate.java,v 1.1 2003-04-03 16:52:59 wbeebee Exp $
 */

public interface DNSLookupDelegate  
{                    
    public String inverseLookup( String ip );

    public String inverseLookup( InetAddress addr );

} // DNSLookupDelegate
