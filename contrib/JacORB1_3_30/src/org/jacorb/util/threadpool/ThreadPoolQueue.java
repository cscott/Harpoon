package org.jacorb.util.threadpool;
/**
 * ThreadPoolQueue.java
 *
 *
 * Created: Fri Jun  9 15:18:43 2000
 *
 * @author Nicolas Noffke
 * $Id: ThreadPoolQueue.java,v 1.1 2003-04-03 17:01:58 wbeebee Exp $
 */

public interface ThreadPoolQueue
{
    public boolean add( Object job );
    public Object removeFirst();

    public boolean isEmpty();
} // ThreadPoolQueue






