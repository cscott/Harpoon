/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* BAEventHandler - This class is a subclass of BoundAsyncEventHandler used in
**                  in subtests of BoundAsyncEventHandlerTest.
**                  This does NOT contain any subtests.
*/

import javax.realtime.*;
import com.timesys.*;

public class BAEventHandler extends BoundAsyncEventHandler
{
    public BAEventHandler()
    {
        super();
        System.out.println("BAEventHandler: Create BoundAsyncEventHandler(1)");
    }

    public BAEventHandler(SchedulingParameters scheduling,
                          ReleaseParameters release,
                          MemoryParameters memory,
                          MemoryArea area,
                          ProcessingGroupParameters group,
                          boolean nonheap,
                          Runnable logic)
    {
        super(scheduling, release, memory, area, group, nonheap, logic);
        System.out.println("BAEventHandler: Create BoundAsyncEventHandler(2)");
    }

    public void handleAsyncEvent()
    {
        System.out.println("BAEventHandler: Inside the BAEventHandler");
    }
}
