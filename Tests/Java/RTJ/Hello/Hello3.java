import javax.realtime.CTMemory;

public class Hello3 implements Runnable {
    public static void main(String argv[]) {
	CTMemory ct = new CTMemory(65536);
	ct.enter(new Hello3());
    }

    public void run() {
	Integer i = new Integer(5);
	Integer j = new Integer(10);
    }
}
