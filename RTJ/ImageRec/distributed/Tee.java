package imagerec;

public class Tee extends ClientServer {
    private ClientServerInt cs1 = null;
    private ClientServerInt cs2 = null;

    public Tee(String args[]) {
	super(args);
    }

    public static void main(String args[]) {
	Tee tee = new Tee(args);
	tee.server(args[0]);
	tee.client(args[1]);
	tee.cs1 = tee.cs;
	tee.client(args[2]);
	tee.cs2 = tee.cs;
    }

    public synchronized void process(ImageData id) {
	cs = cs1;
	remoteProcess(id);
	cs = cs2;
	remoteProcess(id);
    }


}
