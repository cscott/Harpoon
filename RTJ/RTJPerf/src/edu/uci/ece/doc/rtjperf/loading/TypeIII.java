/*-------------------------------------------------------------------------*
 * $Id: TypeIII.java,v 1.1 2002-07-02 15:53:15 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.loading;

import edu.uci.ece.ac.time.HighResTimer;

public class TypeIII extends TypeII {

    public void doSomething() {  }

    public static void main(String[] args) {
        HighResTimer timer = new HighResTimer();
        timer.start();
        timer.stop();

        timer.start();
        TypeIII t = new TypeIII();
        timer.stop();
    }

}
