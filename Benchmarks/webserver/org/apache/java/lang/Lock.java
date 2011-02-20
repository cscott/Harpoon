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

package org.apache.java.lang;

/**
 * This is a reader/writer lock object. The code is vaugely based on
 * an example from <i>_Concurrent Programming in Java_</i>, by Doug Lea.
 * <p>
 * As implemented, JServLock works as an actual "lock" object, which
 * another object will set up and lock and unlock. It blocks incoming
 * readers if there are waiting writers.
 * <p>
 * There are method that wait only at specified amount of time before
 * failing with TimeoutException.
 *
 * @author <a href="mailto:akosut@apache.org">Alexei Kosut</a>
 * @version $Revision: 1.1 $ $Date: 2000-07-18 22:07:17 $
 */
public class Lock {

    private int activeReadLocks = 0;
    private int waitingReadLocks = 0;

    private int activeWriteLocks = 0;
    private int waitingWriteLocks = 0;

    // We only allow a reader to lock if there are no write locks,
    // and no waiting writer locks.
    private boolean allowReadLock() {
        return activeWriteLocks == 0 && waitingWriteLocks == 0;
    }

    // We only allow a writer to lock if there are no active
    // locks
    private boolean allowWriteLock() {
        return activeReadLocks == 0 && activeWriteLocks == 0;
    }

    /**
     * Wait for a read lock. This will wait for all write lock to
     * be removed before returning.
     *
     * @exception InterruptedException if the wait is interrupted. Calling
     * thread must consider the operation to have failed and stop its
     * processing.
     */
    public synchronized void readLock() throws InterruptedException {
        // Register our intent
        waitingReadLocks++;

        try {
            // Wait for a chance to read
            while (allowReadLock() == false) {
                wait();
            }
            // Bingo! Switch from a waiting lock to an active lock
            activeReadLocks++;
        } finally {
            //Remove intent to read
            waitingReadLocks--;
        }
    }

    /**
     * Wait for a read lock. This will wait for all write lock to
     * be removed before returning.
     *
     * @param timeout the number of millisecond before giving up and failing
     * with a TimeoutException.
     * @exception TimeoutException if the lock isn't acquired after the
     * specified amount of time.
     * @exception InterruptedException if the wait is interrupted. Calling
     * thread must consider the operation to have failed and stops its
     * processing.
     */
    public synchronized void readLock(long timeout)
        throws InterruptedException, TimeoutException
    {
        long waitTill = System.currentTimeMillis() + timeout;

        // Register our intent
        waitingReadLocks++;

        try {
            // Wait for a chance to read
            while (allowReadLock() == false) {
                wait(timeout);
                // Check to see if was have the lock
                if (allowReadLock() == false &&
                    (timeout = waitTill - System.currentTimeMillis()) < 0 ) {
                    // Timeout without obtaining lock.
                    throw new TimeoutException();
                }
            }
            // Bingo! Switch from a waiting lock to an active lock
            activeReadLocks++;
        } finally {
            // Remove intent to read.
            waitingReadLocks--;
        }
    }

    /**
     * Unlocks a previously acquired read lock.
     */
    public synchronized void readUnlock() {
        // We're gone
        activeReadLocks--;
        // Wake other threads up
        notifyAll();
    }

    /**
     * Wait for a read lock. This will wait until all read lock have been
     * removed and no other write lock are active.
     *
     * @exception InterruptedException if the wait is interrupted. Calling
     * thread must consider the operation to have failed and stops its
     * processing.
     */
    public synchronized void writeLock() throws InterruptedException {
        // Register our intent
        waitingWriteLocks++;

        try {
            // Wait for a chance to write
            while (allowWriteLock() == false) {
                wait();
            }
            // Bingo! Switch from a waiting lock to an active lock
            activeWriteLocks++;
        } finally {
            // Remove intent lock
            waitingWriteLocks--;
        }
    }

    /**
     * Wait for a read lock. This will wait until all read lock have been
     * removed and no other write lock are active.
     *
     * @param timeout the number of millisecond before giving up and failing
     * with a TimeoutException.
     * @exception TimeoutException if the lock isn't acquired after the
     * specified amount of time.
     * @exception InterruptedException if the wait is interrupted. Calling
     * thread must consider the operation to have failed and stops its
     * processing.
     */
    public synchronized void writeLock(long timeout)
        throws InterruptedException, TimeoutException
    {
        long waitTill = System.currentTimeMillis() + timeout;

        // Register our intent
        waitingWriteLocks++;

        try {
            // Wait for a chance to write
            if (allowWriteLock() == false) {
                wait(timeout);
                if (allowWriteLock() == false &&
                    (timeout = waitTill - System.currentTimeMillis()) < 0 ) {
                    // Timeout, we failed to acquire the lock
                    throw new TimeoutException();
                }
            }
            // Bingo! Switch from a waiting lock to an active lock
            activeWriteLocks++;
        } finally {
            waitingWriteLocks--;
        }
    }

    /**
     * Unlock a previously acquired write lock.
     */
    public synchronized void writeUnlock() {
        // We're gone
        activeWriteLocks--;
        // Wake other threads up
        notifyAll();
    }
}
