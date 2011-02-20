package org.jacorb.util.threadpool;
/**
 * Consumer.java
 *
 *
 * Created: Thu Dec 21 10:52:28 2000
 *
 * @author Nicolas Noffke
 * $Id: Consumer.java,v 1.1 2003-04-03 17:01:58 wbeebee Exp $
 */

public interface Consumer  
{
    public void doWork( Object job );
} // Consumer






