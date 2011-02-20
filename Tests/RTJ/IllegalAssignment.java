import javax.realtime.*;

public class IllegalAssignment {
    public static int[] i;

    public static void main(String args[]) {
	(new VTMemory()).enter(new Runnable() {
	    public void run() {
		i = new int[0];
	    }
	});
    }
}
