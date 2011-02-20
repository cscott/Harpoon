package javax.realtime.memtests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

import com.ibm.ive.memoryModel.*;
import javax.realtime.RealtimeThread;

public class MemSpaceTest 
{
  	public static void main(String[] args) 
  	{
    	Thread myThread = null;
    	try 
    	{
      		myThread = new RealtimeThread() 
      		{
            	public void run() 
            	{
                     MemorySpace ms = null;
                     MemorySpace oldMs = MemorySpace.getCurrentMemorySpace();
                     MemorySpaceDescription msd;
                     msd = new MemorySpaceDescription("ThisSpace", 4096, 0);
                     try 
                     {
                       	ms = MemorySpace.createMemorySpace(msd);
                       	
                       	if (ms != null)
                       		System.out.println("MemorySpace successfully created.");
                       	
                     } catch (MemorySpaceException mse) {
                       	mse.printStackTrace();
                       	System.exit(1);
                     }
                     MemorySpace.setCurrentMemorySpace(ms);
                     MemorySpace.setCurrentMemorySpace(oldMs);                  
              	}
          	};
    	} catch (Exception e) 
    	{
      		e.printStackTrace();
    	};
    	
    	myThread.start();
    	
    	try 
    	{
      		myThread.join();
    	} catch (InterruptedException ie) 
    	{
      		ie.printStackTrace();
      		System.exit(1);
    	}
 	}
}
