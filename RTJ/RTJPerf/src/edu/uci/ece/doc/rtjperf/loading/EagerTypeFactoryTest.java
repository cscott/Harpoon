/*-------------------------------------------------------------------------*
 * $Id: EagerTypeFactoryTest.java,v 1.1 2002-07-02 15:53:15 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.loading;

import edu.uci.ece.ac.time.HighResTimer;

public class EagerTypeFactoryTest {

    public static void main(String[] args) {
        
        EagerTypeFactory factory = EagerTypeFactory.instance();
        for (int round = 0; round < 3; ++round) {
            for (int i = 1; i <= 10; i++) 
                factory.createType(i);
            System.out.println("Round: "+ round);
        }
        
    }
}
