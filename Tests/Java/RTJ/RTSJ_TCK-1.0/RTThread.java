/* RTThread - generic realtime thread used by subtest 1 of the SchedulerTest
*/

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import java.lang.*;
import javax.realtime.*;

public class RTThread extends RealtimeThread {

    public RTThread(SchedulingParameters scheduling,
                    ReleaseParameters release)
    {
        super(scheduling,release);
        System.out.println("RTThread: RTThread(SchedulingParameters,"+
                           "ReleaseParameters)");
    }

    public void run() {
        System.out.println("RTThread: run()");
    }
}
