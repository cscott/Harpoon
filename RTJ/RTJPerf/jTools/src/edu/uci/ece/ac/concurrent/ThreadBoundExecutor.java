// ************************************************************************
//    $Id: ThreadBoundExecutor.java,v 1.1 2002-07-02 15:35:18 wbeebee Exp $
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

/**
 * This class implements an {@link Executor} that has permanently a
 * thread bound.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class ThreadBoundExecutor implements Executor {

    protected Thread thread;
    protected boolean active = false;
    protected ExecutorLogic executorLogic = new ExecutorLogic();
    protected int executionEligibility = -1;
    
    ///////////////////////////////////////////////////////////////////////////
    //                         Inner Classes
    //
    static private class ExecutorLogic implements Runnable {

        private Runnable task;
        private boolean active;
        protected EventVariable taskAvailableEvent;
        protected EventVariable executorIdleEvent;
        protected boolean isIdle;
        
        ExecutorLogic() {
            active = true;
            this.taskAvailableEvent = new EventVariable();
            this. executorIdleEvent =
                new EventVariable(true); // Create a "signaled" event
            this.isIdle = true;
        }
        
        void shutdown() {
            if (this.active()) {
                final ExecutorLogic logic = this;
                Runnable shutDownLogic = new Runnable() {
                        public void run() {
                            logic.active(false);
                        }
                    };
                try {
                    this.execute(shutDownLogic);
                } catch (ShutdownExecutorException e) {
                    e.printStackTrace();
                }
                shutDownLogic = null;
            }
        }
        
        void  execute(Runnable task) throws ShutdownExecutorException {
            //            System.out.println(">> ExecutorLogic.execute()");
            if (!this.active())
                throw new ShutdownExecutorException(">> Unable to execute logic, ThreadBoundExecutor" +
                                                    "has been already shut down");
            try {
                //                System.out.println(">> ExecutorLogic: Waiting for Idle Event");
                executorIdleEvent.await();
                this.isIdle(false);
                if (!this.active())
                    throw new ShutdownExecutorException(">> Unable to execute logic, ThreadBoundExecutor" +
                                                        "has been already shut down");
                this.task = task;
                //                System.out.println(">> ExecutorLogic: Signaling Available Task Event");
                taskAvailableEvent.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (this.active) {
                    //                    System.out.println(">> ExecutorLogic: Waiting for some Task");
                    taskAvailableEvent.await();
                    //                    System.out.println(">> ExecutorLogic: Running Task " + this.task);
                    this.task.run();
                    executorIdleEvent.signal();
                    this.isIdle(true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized boolean active() {
            return this.active;
        }

        synchronized void active(boolean bool) {
            this.active = bool;
        }

        synchronized boolean isIdle() {
            return this.isIdle;
        }

        private synchronized void isIdle(boolean bool) {
            this.isIdle = bool;
        }
        

    }
    //
    ///////////////////////////////////////////////////////////////////////////
    

    /**
     * Creates a new <code>ThreadBoundExecutor</code> instance.
     *
     * @param priority an <code>int</code> value representing the
     * priority associated with the executor.
     */
    public ThreadBoundExecutor(int priority) {
        this.thread = new Thread(this.executorLogic);
        this.thread.setDaemon(true);
        this.thread.setPriority(priority);
        this.thread.start();
    }
    
    public ThreadBoundExecutor() {
        this(Thread.NORM_PRIORITY);
    }

    /**
     * Executes the given logic. If the logic is executed on a newly
     * created thread, or of a thread is borrowed from a pool is
     * implementation dependent.
     *
     * @param logic a <code>Runnable</code> value
     * @exception ShutdownExecutorException if the
     * <code>Executor</code> has already been shut down.
     */
    public void execute(Runnable logic) throws ShutdownExecutorException {
        //        System.out.println(">> ThreadBoundExecutor.execute()");
        this.executorLogic.execute(logic);
    }
    
    /**
     * Releases all the resources assoiated with the executor. No
     * subsequent invocation of the <code>execute()</code> method
     * should be performed after the executor has been shutdown.
     *
     */
    public void shutdown() {
        this.executorLogic.shutdown();
    }
}
