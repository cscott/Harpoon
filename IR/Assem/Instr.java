// Instr.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Util.ArrayFactory;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;

/**
 * <code>Instr</code> is an supperclass representation for
 * all of the assembly-level instruction representations used in
 * the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.4 1999-02-17 03:23:19 andyb Exp $
 */
public class Instr implements HCodeElement, UseDef {
    protected String assem;
    protected Temp[] dst;
    protected Temp[] src;

    protected InstrFactory inf;
    protected String source_file;
    protected int source_line;
    protected int id;

    /** Creates an <code>Instr</code> consisting of the String 
     *  assem and the lists of destinations and sources in dst and src. */
    Instr(InstrFactory inf, HCodeElement source, 
          String assem, Temp[] dst, Temp[] src) {
        this.source_file = (source != null)?source.getSourceFile():"unknown";
        this.source_line = (source != null)?source.getLineNumber():0;
        this.id = inf.getUniqueID();
        this.inf = inf;
        this.assem = assem; this.dst = dst; this.src = src;
    }
   
    /** Returns the assembly-level instruction as a String, with
     *  <code>Temp</code>s represented either by their temp name or
     *  (if the register allocator has been run), by their register. */
    public String format(TempMap tm) {
        /* XXX - needs to be done. */
        return null;
    }
    
    /** Defines an array factory which can be used to generate
     *  arrays of <code>Instr</code>s. */
    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Instr[len]; }
        };

    /* Definitions for the HCodeElement and UseDef interfaces. */

    public Temp[] use() { return src; }

    public Temp[] def() { return dst; }

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }
}
