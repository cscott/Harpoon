// Switch.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;


/**
 * Allows some sort of run-time decision making in pipelines,
 * by allowing {@link Node}s to tag {@link ImageData}s with
 * {@link Command}s and doing a conditional branch.
 *
 * @see Command
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
*/
public class Switch extends Node {

    /**
     * Sends the given {@link ImageData} along either the 
     * left or right path, according to the {@link Command}
     * that the {@link ImageData} is tagged with.<br><br>
     *
     * An {@link ImageData} tagged with the Command.GO_RIGHT tag
     * will be sent along the right path. Any other tag will
     * cause the {@link ImageData} to be sent along the left path.
     *
     * @param id The {@link ImageData} which can be tagged.
     *
     * @see Command
     */
    public void process(ImageData id) {
	int cmd = Command.read(id);
	//System.out.println("Switch: x:"+id.x);
	//System.out.println("Switch: y:"+id.x);
	//System.out.println("Switch: width:"+id.width);
	//System.out.println("Switch: height:"+id.height);
	
	if (cmd == Command.GO_BOTH) {
	    super.process(id);
	}
	else if (cmd == Command.GO_RIGHT) {
	    //System.out.println("Switch: Going RIGHT");
	    if (getRight() != null)
		getRight().process(id);
	}
	else {
	    //System.out.println("Switch: Going LEFT");
	    if (getLeft() != null)
		getLeft().process(id);
	}
    }
}
