package imagerec;

import java.io.Serializable;

/** The image recognition program is composed of a directed graph of nodes and edges.
 *  Each node has at most one in edge and can have zero, one, or two out edges. 
 *  ImageDatas flow along the edges.
 *  Object references point in the direction of ImageData flow.
 * 
 */

public class Node implements Serializable, Runnable {
    private Node out1, out2;

    public Node() {
	this(null);
    }

    public Node(Node out1) {
	this(out1, null);
    }

    public Node(Node out1, Node out2) {
	this.out1 = out1;
	this.out2 = out2;
    }

    public void setLeft(Node out1) {
	this.out1 = out1;
    }

    public void setRight(Node out2) {
	this.out2 = out2;
    }

    public synchronized void process(ImageData id) {
	if (this.out1 != null) {
	    this.out1.process(id);
	    if (this.out2 != null) {
		this.out2.process(id);
	    }
	}
    }

    public void run() {
	process(null);
    }
}
