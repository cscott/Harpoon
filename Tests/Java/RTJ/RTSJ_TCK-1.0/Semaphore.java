/* Sempahore - A generic semaphore class used by various tests as a way to
**             provide wait/notify between threads.
*/

/*
Software that is contained on this CDRom is the property of TimeSys
Corporation and TimeSys Corporation owns all intellectual property rights,
title and interests therein.  Some of the code contained herein is the
property of IBM and other third parties and incorporated and distributed by
the permission of IBM and those third parties.
*/

import javax.realtime.*;

public class Semaphore
{
    private int count;

    public void Semaphore() {
        System.out.println("Semaphore: Semaphore()");
        count = 0;
    }


    public synchronized void sWait() {
        if (count > 0)
            System.out.println("Semaphore: count is > 0 - unable to wait");
        while ( count <= 0 ) {
            try {
                System.out.println("Semaphore: Master waiting!");
                this.wait();
            } catch (Exception e) {
                System.out.println("Semaphore: SEMAPHORE UNABLE TO WAIT");
                e.printStackTrace();
            }
        }
        System.out.println("Semaphore: Master is awake");
        count -= 1;
    }


    public synchronized void sNotify() {
        count += 1;
        try {
            System.out.println("Semaphore: Going to wake Master");
            this.notify();
        } catch (Exception e) {
            System.out.println("Semaphore: SEMAPHORE UNABLE TO NOTIFY");
            e.printStackTrace();
        }
    }
}
