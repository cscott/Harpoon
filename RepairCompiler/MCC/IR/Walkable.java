
package MCC.IR;

/**
 * The Walkable interface specifies a set of methods that defines a web. 
 */

public interface Walkable {

    /**
     * Returns the name of the node
     */
    public String getNodeName();


    /**
     * Returns the number of neighbors from this node
     */
    public int getNeighborCount();


    /**
     * Returns a specific neighbor
     */
    public Object getNeighbor(int index);

    /**
     * Returns a pretty print of the representation of the node. 
     *
     * @param indent    number of blank spaces to skip for a new line
     * @param recursive if true, recursively print children
     */
    public String PPrint(int indent, boolean recursive);
}

