package org.jacorb.util.tracing;

import java.util.Vector;
/**
 * TraceTreeNode.java
 *
 *
 * Created: Tue Jul 25 13:06:22 2000
 *
 * @author Nicolas Noffke
 * $Id: TraceTreeNode.java,v 1.1 2003-04-03 17:02:12 wbeebee Exp $
 */

public class TraceTreeNode
{
    protected Vector subtraces = null;

    protected int tracer_id = 0;
    protected String operation = null;
    protected long client_time = 0;
    protected long server_time = 0;


    public TraceTreeNode(int tracer_id)
    {
        this.tracer_id = tracer_id;
        subtraces = new Vector();
    }

//      public TraceTreeNode(int tracer_id,
//                           long client_time,
//                           long server_time)
//      {
//          this(tracer_id);

//          client_time = client_time;
//          server_time = server_time;
//      }    
} // TraceTreeNode






