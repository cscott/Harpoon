// VariableLatency.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec.graph;

/**
 * The {@link VariableLatency} interface defines a way
 * to interact with classes promising to have
 * adjustable latencies.
 */
public interface VariableLatency {
    /**
     * Sets the latency of the process() method
     * of this class.
     *
     * @param time The new latency.
     */
    public void setLatency(int time);
}
