/*-------------------------------------------------------------------------*
 * $Id: EagerTypeFactory.java,v 1.1 2002-07-02 15:53:15 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.loading;

import edu.uci.ece.ac.time.HighResTimer;

public class EagerTypeFactory {

    private static final EagerTypeFactory instance = new EagerTypeFactory();

    private static final HighResTimer timer = new HighResTimer();
    
    static {
        try {
            Class clazz = Class.forName("edu.uci.ece.doc.rtjperf.loading.TypeX"); 
            Object obj = clazz.newInstance();
            System.out.println(clazz.getClassLoader());
            obj = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        timer.start();
        timer.stop();
    }

    public final static  EagerTypeFactory instance() {
        return instance;
    }
    
    public final void createType(int type) {
        
        switch (type) {
        case 1:
            timer.start();
            new TypeI();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 2:
            timer.start();
            new TypeII();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 3:
            timer.start();
            new TypeIII();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 4:
            timer.start();
            new TypeIV();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 5:
            timer.start();
             new TypeV();
             timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 6:
            timer.start();
            new TypeVI();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 7:
            timer.start();
            new TypeVII();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 8:
            timer.start();
            new TypeVIII();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 9:
            timer.start();
            new TypeIX();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        case 10:
            timer.start();
            new TypeX();
            timer.stop();
            System.out.println(timer.getElapsedTime());
            break;
        }
    }
}
    
