package edu.uci.ece.doc.rtjperf.time.clock;

import javax.realtime.*;


public class ClockTest {

    public static void main(String[] args) {

        Clock clock = Clock.getRealtimeClock();
        RelativeTime resolution = clock.getResolution();

        System.out.println("Realtime Clock Resolution: " + resolution);

        AbsoluteTime currentTime = clock.getTime();
        System.out.println("Current Time: " + currentTime);
        
        RelativeTime newResolution = new RelativeTime(1, 1);
        clock.setResolution(newResolution);
        currentTime = clock.getTime();
        System.out.println("Current Time: " + currentTime);

        newResolution = new RelativeTime(1, 1000);
        clock.setResolution(newResolution);
        currentTime = clock.getTime();
        System.out.println("Current Time: " + currentTime);
        resolution = clock.getResolution();
        System.out.println("Realtime Clock Resolution: " + resolution);
    }
}
