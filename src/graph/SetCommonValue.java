// SetCommonValue.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.CommonMemory;

/**
 * {@link SetCommonValue} nodes allow variables in {@link CommonMemory} to be repeatedly
 * set to a constant value.
 * 
 * @see CommonMemory
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class SetCommonValue extends Node {
    /**
     * The name of the {@link CommonMemory} variable to set each
     * time <code>process()</code> is called.
    */
    private String name;
    /**
     * The value to set the {@link CommonMemory} variable to each
     * time <code>process()</code> is called.
    */
    private Object value;

    /**
     * Constructs a {@link SetCommonValue} node which sets a {@link CommonMemory}
     * variable to a constant value each time the <code>process()</code> method is
     * called.
     *
     * @param name The name of the {@link CommonMemory} variable that will be set.
     * @param value The value which will be stored in the {@link CommonMemory} variable.
     *
     * @see CommonMemory
     */
    public SetCommonValue(String name, Object value) {
	super();
	this.name = name;
	this.value = value;
    }

    /**
     * Simply sets the {@link CommonMemory} variable to the value specified in the
     * constructor, then calls <code>super.process()</code>.
     *
     * @param id The {@link ImageData} that will be passed on to the next {@link Node}s.
     *
     * @see CommonMemory
     */
    public void process(ImageData id) {
	//System.out.println("Setting common value: "+name+"("+value+")");
	CommonMemory.setValue(name, value);
	super.process(id);
    }
}
