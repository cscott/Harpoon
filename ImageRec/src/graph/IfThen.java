// IfThen.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.CommonMemory;


/**
 * An {@link IfThen} node allows for conditional branching based on the
 * boolean value of a variable in {@link CommonMemory}. When constructing
 * an {@link IfThen} node, you specify the name of the {@link CommonMemory}
 * variable containing the <code>true/false</code> value. When an
 * {@link ImageData} is passed to the <code>process()</code> method,
 * it is first sent along the left path.
 * When the left-hand path returns, then the {@link CommonMemory} variable
 * is checked. If the value is <code>true</code>, then the {@link ImageData}
 * is sent along the right-hand path. If the value is <code>false</code>
 * then the <code>process()</code> method returns.
 *
 * @see CommonMemory
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
*/
public class IfThen extends Node {
    /**
       The name of the {@link CommonMemory} variable that
       will determine if {@link ImageData}s are passed along
       the right-hand path.
     */
    private String commonMemName;

    /**
     * Constructs an {@link IfThen} node that
     * will send {@link ImageData}s along the right-hand
     * path conditional if the value of the
     * specified {@link CommonMemory} variable is <code>false</code> at
     * the time of execution.
     *
     * @see CommonMemory
     */
    public IfThen(String commonMemName) {
	this.commonMemName = commonMemName;
    }

    /**
     * First sends the given {@link ImageData} along the 
     * left-hand path. Then sends the {@link ImageData} along the
     * right-hand path if the value of the {@link CommonMemory} variable
     * specified in the constructor is <code>true</code>.
     *
     * @param id The {@link ImageData} which will be passed on.
     *
     * @see CommonMemory
     */
    public void process(ImageData id) {
	if (getLeft() != null)
	    getLeft().process(id);

	boolean goRight =
	    ((Boolean)(CommonMemory.getValue(commonMemName))).booleanValue();
	if (goRight && (getRight() != null))
	    getRight().process(id);
    }
}
