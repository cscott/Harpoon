// Schedulable.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;
import java.io.*;

public interface Schedulable extends java.lang.Runnable {
    public boolean addToFeasibility();

    public MemoryParameters getMemoryParameters();
    public ProcessingGroupParameters getProcessingGroupParameters();
    public ReleaseParameters getReleaseParameters();
    public SchedulingParameters getSchedulingParameters();

    public Scheduler getScheduler();

    public void removeFromFeasibility();

    public void setMemoryParameters(MemoryParameters memory);
    public boolean setMemoryParametersIfFeasible(MemoryParameters memParam);
    public void setProcessingGroupParameters(ProcessingGroupParameters pgp);
    public boolean setProcessingGroupParametersIfFeasible(ProcessingGroupParameters groupParameters);
    public void setReleaseParameters(ReleaseParameters release);
    public boolean setReleaseParametersIfFeasible(ReleaseParameters release);
    public void setSchedulingParameters(SchedulingParameters scheduling);
    public boolean setSchedulingParametersIfFeasible(SchedulingParameters scheduling);
    public void setScheduler(Scheduler scheduler)
	throws IllegalThreadStateException;
    public void setScheduler(Scheduler scheduler,
			     SchedulingParameters scheduling,
			     ReleaseParameters release,
			     MemoryParameters memoryParameters,
			     ProcessingGroupParameters processingGroup)
	throws IllegalThreadStateException;
}
