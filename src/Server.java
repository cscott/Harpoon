package imagerec;

public class Server extends Node {
    private String args, corbaName;

    public Server(String args, String corbaName, Node out) {
	super(out);
	this.args = args;
	this.corbaName = corbaName;
    }

    public synchronized void process(ImageData id) {


	super.process(id);
    }

}
