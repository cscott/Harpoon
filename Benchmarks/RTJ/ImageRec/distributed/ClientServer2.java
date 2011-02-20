package imagerec;

class ClientServer {
    public ClientServer(String args[]) {}
    
    public synchronized void process(ImageData id) {
	System.out.println("Default implementation called");
    }
}
