public class TestServer {
    public static void main(String args[]) {
	IPaqServoController ip = new IPaqServoController("18.24.6.219");
	while (true) {
	    for (int i=1; i<255; i++) ip.move(1, i);
	    for (int i=254; i>=1; i--) ip.move(1, i);
	}
    }

}
