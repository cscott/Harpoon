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
 * This controller creates a simple yet very effective negative feedback
 * that stabilizes the level of the container. It is based on a simple
 * idea but it proves to be very adaptive to any kind of load creating
 * the minimum level that, on average, the container needs to fully satisfy
 * its requests without consuming memory or being empty.
 * <p>
 * The control is based on the assumption that if the container is empty,
 * some new object will be created and will later on be recycled. Following
 * this assumption, when the container is empty, its optimum level is raised
 * to allow more objects to be stored when they will be recycled.
 * <p>
 * On the other end, when the container reaches its top level, it means
 * there are too many objects and the level is lowered.
 * <p>
 * There is room for objects to be recycled only when the level is lower
 * than the top one. Note that you don't need to define any parameter
 * to this controller since it will find the best top level based
 * on the history of the requests, optimizing memory use by itself.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:07:33 $
 */

public class AdaptiveController implements Controller {

    private int top = 1;
    private int level = 0;

	/**
	 * Signal an object recycle.
	 */
	public void up() {
		if (level == top) top--;
		level++;
	}

    /**
     * Signal an object request.
     */
	public void down() {
        if (level == 0) {
        	top++;
        } else {
        	level--;
        }
	}

    /**
     * Evaluates the room for the recyclable object
     */
	public boolean isThereRoomFor(Recyclable object) {
		return (level < top);
	}
}