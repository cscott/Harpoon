/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* AEventHandler - This class is a subclass of AsyncEventHandler used in many
**                 of the subtests that require an AsyncEventHandler
**                 This does NOT contain any subtests.
*/

import javax.realtime.*;
import com.timesys.*;

public class AEventHandler extends AsyncEventHandler
{
    /*
    ** The following constructors just call AsyncEventHandler
    ** constructors
    */

    public AEventHandler() {
        super();
        System.out.println("AEventHandler: Created an AEventHandler (1)");
    }

    public AEventHandler(Runnable logic) {
        super(logic);
        System.out.println("AEventHandler: Created an AEventHandler (2)");
    }

    public AEventHandler(SchedulingParameters scheduling,
                         ReleaseParameters release,
                         MemoryParameters memory,
                         MemoryArea area,
                         ProcessingGroupParameters group,
                         Runnable logic) {
        super(scheduling, release, memory, area, group, false, logic);
        System.out.println("AEventHandler: Created an AEventHandler (3)");
    }

    public AEventHandler(boolean nonheap) {
        super(nonheap);
        System.out.println("AEventHandler: Created an AEventHandler (4)");
    }

    public AEventHandler(boolean nonheap, Runnable logic) {
        super(nonheap, logic);
        System.out.println("AEventHandler: Created an AEventHandler (5)");
    }

    public AEventHandler(SchedulingParameters scheduling,
                         ReleaseParameters release,
                         MemoryParameters memory,
                         MemoryArea area,
                         ProcessingGroupParameters group,
                         boolean nonheap,
                         Runnable logic) {
        super(scheduling, release, memory, area, group, nonheap, logic);
        System.out.println("AEventHandler: Created an AEventHandler (6)");
    }



    public void handleAsyncEvent() {
        System.out.println("AEventHandler: handleAsyncEvent called");
    }

    public int getPendingFireCount_A()
    {
        return getPendingFireCount();
    }
}
