/*
Software that is contained on this CD-Rom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

/* NewScheduler - is a subclass of Scheduler used to test the non-abstract
**                method, changeIfFeasible() of the Scheduler class in
**                SchedulerTest.
**                The PriorityScheduler (an RTSJ provided subclass of
**                Scheduler)
**                currently overrides this method.
*/
import javax.realtime.*;
import java.lang.reflect.*;
import java.util.*;


public class NewScheduler extends Scheduler {
    public final int MAX_PRIORITY = 265;
    public final int MIN_PRIORITY = 10;

    static HashSet feasibilitySet = null;
    public boolean isFeasible()
    {
        System.out.println("NewScheduler: isFeasible()");
        return true;
    }
    public String getPolicyName() {
        System.out.println("NewScheduler: getPolicyName()");
        return new String("Fixed Priority");
    }
    public boolean removeFromFeasibility(Schedulable schedulable) {
        System.out.println("NewScheduler: removeFromFeasibility(Schedulable)");
        if (feasibilitySet != null)
            feasibilitySet.remove(schedulable);
        return false; // ???
    }
    public boolean addToFeasibility(Schedulable schedulable) {
        System.out.println("NewScheduler: addToFeasibility(Schedulable)");
        RealtimeThread t = null;
        PriorityParameters p = null;
        synchronized (this)
            {
                if (feasibilitySet == null)
                    {
                        feasibilitySet = new HashSet();
                    }
            }

        if (!feasibilitySet.contains(schedulable))
            {
                try
                    {
                        t = (RealtimeThread) schedulable;
                        p = (PriorityParameters) t.getSchedulingParameters();
                    }
                catch(Exception e)
                    {
                    }
                if (p.getPriority() >=MIN_PRIORITY)
                    p.setPriority(p.getPriority() - MIN_PRIORITY );
                feasibilitySet.add(schedulable);
            }

        return false; // ???
    }

    public void fireSchedulable(Schedulable schedulable)
    {
        System.out.println("NewScheduler: fireSchedulable(Schedulable)");
        return;
    }

}

