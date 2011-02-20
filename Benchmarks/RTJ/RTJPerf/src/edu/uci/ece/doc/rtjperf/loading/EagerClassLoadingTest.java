/*-------------------------------------------------------------------------*
 * $Id: EagerClassLoadingTest.java,v 1.1 2002-07-02 15:53:15 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.loading;

import edu.uci.ece.ac.time.HighResTimer;


/**
 * This tests can be used to figure out wheather or not a JVM uses
 * <em> eager </em> class loading or not.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class EagerClassLoadingTest {

    public final static int TYPE_A = 0;
    public final static int TYPE_B = 1;
    public final static int TYPE_C = 2;
    
    public Type createType(int type) {
        switch (type) {
        case TYPE_A:
            return new TypeA();
        case TYPE_B:
            return new TypeB();
        case TYPE_C:
            return new TypeC();
        default:
            return null;
        }
    }

    public static void main(String[] args) {

        EagerClassLoadingTest test = new EagerClassLoadingTest();
        HighResTimer timer = new HighResTimer();
        Type t = null;

        timer.start();
        t = test.createType(0);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        t = test.createType(1);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        t = test.createType(2);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        t = test.createType(0);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        t = test.createType(1);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        t = test.createType(2);
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        new TypeX();
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());

        timer.start();
        new TenFace();
        timer.stop();
        System.out.println("Time Elapsed: " + timer.getElapsedTime());
    }
}
