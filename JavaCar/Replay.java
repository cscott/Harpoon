import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.io.InputStreamReader;

public class Replay {
    public static void main(String s[]) {
	FileInputStream b = null;
	IPaqServoController servo = new IPaqServoController(s[1]);
	try {
	    b = new FileInputStream(s[0]);
	    BufferedReader r = new BufferedReader(new InputStreamReader(b));
	    String delay;
	    while ((delay=r.readLine())!=null) {
		String command = r.readLine();
		try {
		    Thread.sleep(Long.parseLong(delay));
		} catch(InterruptedException e) {
		    System.out.println("Error -" +e);
		    System.exit(-1);
		}    
		if (command.equals("u")) {
		    servo.move(2,150);
		} else if (command.equals("u1")) {
		    servo.move(2,128);
		} else if (command.equals("d")){
		    servo.move(2,110);
		} else if (command.equals("d1")){
		    servo.move(2,128);
		} else if (command.equals("l")){
		    servo.move(1,1);
		} else if (command.equals("l1")){
		    servo.move(1,126);
		} else if (command.equals("r")){
		    servo.move(1,255);
		} else if (command.equals("r1")){
		    servo.move(1,126);
		}
	    }
	    b.close();
	} catch (IOException e) {
	    System.out.println("Error-" + e.toString());
	    System.exit(-1);   
	}
    }
}
