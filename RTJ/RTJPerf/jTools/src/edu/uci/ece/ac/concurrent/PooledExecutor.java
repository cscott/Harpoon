// ************************************************************************
//    $Id: PooledExecutor.java,v 1.1 2002-07-02 15:35:18 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.concurrent;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class represent an abstraction for an <code>Executor</code>
 * that is actually a pool of <code>Executor</code>s. The QoS
 * parameters used to create this class are used as default parameters
 * for the pool's <code>Executor</code>s.<br>
 *
 * <b>NOTE:</b> The basic idea behind executors pool, is that the each
 * individual executor changes its QoS in order to adjust to the
 * schedulable entity for which it is running. No schedulability check
 * is done while changing the QoS, because the change should keep the
 * system feasible as far as the entity that is using the executor was
 * considered in the feasibility analysis.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class PooledExecutor implements Executor {

    private int executorNum;
    private LinkedList executorPool;
    private CountableEventVariable executorAvailableEvent;
    private boolean active;

    private int executionEligibility;
    
    ///////////////////////////////////////////////////////////////////////////
    //                         Inner Classes
    //
    class PooledExecutorLogic implements Runnable {

        private Runnable task;
        private Executor executor;
        
        PooledExecutorLogic(Executor executor) {
            this.executor = executor;
        }

        void shutdown() {
            this.executor.shutdown();
        }
        
        void execute(Runnable task) throws ShutdownExecutorException {
            //            System.out.println(">> PooledExecutorLogic.execute()");
            this.task = task;
            //            System.out.println(">> PooledExecutorLogic: Executing " + task + " with " + this.executor);
            this.executor.execute(this);
        }
        
        public void run() {
            this.task.run();
            PooledExecutor.this.notifyTaskCompletion(this);
        }
    }
    //
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Creates a new <code>PooledExecutor</code> instance with the
     * specified parameters.
     */
    public PooledExecutor(int executorNum, int priority) {

        this.executorNum = executorNum;
        this.executorPool = new LinkedList();
        this.executorAvailableEvent = new CountableEventVariable(executorNum);
        
        ThreadBoundExecutor executor;
        for (int i = 0; i < executorNum; i++) {
            executor = new ThreadBoundExecutor(priority);
            this.executorPool.add(new PooledExecutorLogic(executor));
        }
        executor = null;
        this.active(true);
    }

    synchronized void notifyTaskCompletion(PooledExecutorLogic executorLogic) {
        this.executorPool.addLast(executorLogic);
        executorAvailableEvent.signal();
    }

    
    public synchronized void execute(Runnable task)
        throws ShutdownExecutorException
    {
        //        System.out.println(">> PooledExecutor.execute()");
        if (!this.active())
            throw new ShutdownExecutorException(">> Unable to execute logic, ThreadBoundExecutor" +
                                                "has been already shut down");
        try {
            //            System.out.println(">> PooledExecutor: Waiting for a task");
            executorAvailableEvent.await();
            //            System.out.println(">> PooledExecutor: Grabbing and executor");
            PooledExecutorLogic executorLogic =
                (PooledExecutorLogic)this.executorPool.getLast();
            //            System.out.println(">> PooledExecutor: Got " + executorLogic);
            executorLogic.execute(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void shutdown() {
        this.active(false);
        PooledExecutorLogic pel;

        ListIterator iterator = this.executorPool.listIterator();
        while (iterator.hasNext()) {
            pel = (PooledExecutorLogic)iterator.next();
            pel.shutdown();
        }
    }

    private boolean active() {
        return this.active;
    }

    private void active(boolean bool) {
        this.active = bool;
    }

}
