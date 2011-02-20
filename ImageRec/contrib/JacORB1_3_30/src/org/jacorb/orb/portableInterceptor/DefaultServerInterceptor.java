package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;

/**
 * DefaultServerInterceptor.java
 *
 * A simple base class for user-defined server interceptors
 *
 * @author Gerald Brose.
 * @version $Id: DefaultServerInterceptor.java,v 1.1 2003-04-03 16:54:16 wbeebee Exp $
 */

public abstract class DefaultServerInterceptor
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements ServerRequestInterceptor
{

    // InterceptorOperations interface
    public abstract String name();


    public void receive_request_service_contexts( ServerRequestInfo ri ) 
        throws ForwardRequest
    {
    }

    public void receive_request( ServerRequestInfo ri )
        throws ForwardRequest
    {
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void send_other(ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

}






