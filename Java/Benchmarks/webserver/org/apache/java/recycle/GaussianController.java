/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

package org.apache.java.recycle;

/**
 * This is an adaptive controller based on a statistical analysis of
 * the level transitions. The optimal level is derived from
 * the variance of the transitions using the following equation:
 * <pre>   adaptiveLevel = bias + factor * standardDeviation   </pre>
 * where the standard deviation signifies the variability of level
 * around its average.
 * <p>
 * This class creates a convergence of the system Container-Controller
 * to a point in which the average level tends to its standard deviation.
 * This creates a negative feedback that stabilizes the system.
 * <p>
 * NOTE: This class is only experimental and it is meant to show only a more
 * complex implementation of a <code>Controller</code>. The routines
 * that calculate the statistical parameters this class uses to determine
 * the level use heavy calculations and should be used only when a
 * very precise adaptivity is required.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:45 $
 */

public class GaussianController implements Controller {

    /** This is the log2 value of the sampling window. */
	private static final int WINDOW = 8;
    /** This is the minimum value allowed to be reached. */
    private static final int BIAS = 1;
    /** This is multiplication factor for the standard deviation. */
    private static final int FACTOR = 3;

    /**
     * The current pool level.
     */
    private int level = 0;
	
	/**
	 * The array of pool levels.
	 */
	private int[] levels = new int[1 << WINDOW];

    /**
     * The cursor in the circular array.
     */
    private int cursor = 0;
	
	/**
	 * Writes on the memory of this controller incrementing the level.
	 */
	public void up() {
		this.level++;
		this.levels[++cursor & (WINDOW - 1)] = this.level;
	}

	/**
	 * Writes on the memory of this controller decrementing the level.
	 */
	public void down() {
        this.level--;
        this.levels[++cursor & (WINDOW - 1)] = this.level;
	}

    /**
     * Evaluates the room for the object to recycle basing this
     * decision to the optimum level estrapolated from the
     * level history.
     */
	public boolean isThereRoomFor(Recyclable object) {
		return (level < BIAS + (int) (FACTOR * getStandardDeviation(levels)));
	}

	/**
	 * Calculates the RMS value of a given array.
	 */
	private float getStandardDeviation(int[] array) {
		float sum = 0;
		float average = getAverage(array);

		for (int i = 0; i < array.length; i++) {
			sum += Math.pow(Math.abs(array[i] - average), 2);
		}

		return (float) (Math.sqrt(sum / array.length));
	}

	/**
	 * Calculates the average value of a given array.
	 */
	private float getAverage(int[] array) {
		int sum = 0;
		for (int i = 0; i < array.length; sum += array[i++]);
		return ((float) sum) / array.length;
	}
}