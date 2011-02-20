package org.jacorb.orb.factory;

/* 
 * 
 * @author Nicolas Noffke
 * $Id: SSLServerSocketFactory.java,v 1.1 2003-04-03 16:53:59 wbeebee Exp $
 */

import java.net.*;

public interface SSLServerSocketFactory
    extends ServerSocketFactory
{
    public void switchToClientMode( Socket socket );
    
    public boolean isSSL( ServerSocket socket );
}













