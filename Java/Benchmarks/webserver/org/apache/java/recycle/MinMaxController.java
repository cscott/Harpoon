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
 * This level controller is based on a simpler variance method: the 
 * variance of the request frequency is obtained from the difference
 * between the minimum and maximum value of the level.
 * The stability of the system is not much, but it consumes a lot
 * lesser time resources that GaussianController.
 * 
 * @author <a href="mailto:scoobie@systemy.it">Federico Barbieri</a>
 * @version $Revision: 1.1 $ $Date: 2000-06-29 01:41:45 $
 */

public class MinMaxController implements Controller {

    /** This is the log2 value of the sampling window. */
    private static final int WINDOW = 6;
    /** This is the minimum value allowed to be reached. */
    private static final int BIAS = 1;

    /**
     * The current level.
     */
    private int level = 0;
    
    /**
     * The array of levels.
     */
    private int[] levels = new int[1 << WINDOW];

    /**
     * The pointer in the circular array.
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
        return (level < BIAS + getLevel(levels));
    }

	/**
	 * Returns the predicted correct level
	 * calculating the max variable amplitude in levels statistics.
	 */
	private int getLevel(int[] array) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) min = array[i];
			if (array[i] > max) max = array[i];
		}
		
		return (max - min);
	}
}