// Instr.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Util.ArrayFactory;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.IR.Properties.Edges;
import harpoon.ClassFile.HCodeEdge;

/**
 * <code>Instr</code> is an supperclass representation for
 * all of the assembly-level instruction representations used in
 * the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.10 1999-05-17 20:08:00 andyb Exp $
 */
public class Instr implements HCodeElement, UseDef, Edges {
    public String assem;
    public Temp[] dst;
    public Temp[] src;
    public Label[] targets;

    public InstrFactory inf;
    public String source_file;
    public int source_line;
    public int id;

    // FSK: below variables are needed for proper implementation of the 
    // Edges interface
    public HCodeEdge[] edges;
    public HCodeEdge[] pred;
    public HCodeEdge[] succ;
    
    /** Creates an <code>Instr</code> consisting of the String 
     *  assem and the lists of destinations and sources in dst and src. */
    public Instr(InstrFactory inf, HCodeElement source, 
          String assem, Temp[] dst, Temp[] src, Label[] targets) {
        this.source_file = (source != null)?source.getSourceFile():"unknown";
        this.source_line = (source != null)?source.getLineNumber():0;
        this.id = inf.getUniqueID();
        this.inf = inf;
        this.assem = assem; this.dst = dst; this.src = src;
        this.targets = targets;
    }

    public Instr(InstrFactory inf, HCodeElement source,
          String assem, Temp[] dst, Temp[] src) {
        this(inf, source, assem, dst, src, null);
    } 

    public Instr(InstrFactory inf, HCodeElement source,
          String assem, Label[] targets) {
        this(inf, source, assem, null, null, targets);
    }
   
    public String toString() {
        StringBuffer s = new StringBuffer();
        int len = assem.length();
        for (int i = 0; i < len; i++) 
            if (assem.charAt(i) == '`')
                switch (assem.charAt(++i)) {
                    case 's': {
                        int n = Character.digit(assem.charAt(++i), 10);
                        s.append(dst[n]);
                        }
                        break;
                    case 'd': {
                        int n = Character.digit(assem.charAt(++i), 10);
                        s.append(src[n]);
                        }
                        break;
                    case 'j': {
                        int n = Character.digit(assem.charAt(++i), 10);
                        s.append(targets[n]);
                        }
                        break;
                    case '`': 
                        s.append('`');
                        break;
                }
            else s.append(assem.charAt(i));

        return s.toString();
    }

    public InstrFactory getFactory() { return inf; }

    /** Defines an array factory which can be used to generate
     *  arrays of <code>Instr</code>s. */
    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Instr[len]; }
        };

    /* Definitions for the HCodeElement and UseDef interfaces. */

    public Temp[] use() { return src; }

    public Temp[] def() { return dst; }

    public Label[] jumps() { return targets; }

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }

    // FSK: following methods are necessary for Edges interface 

    // XXX while these are correct methods, they are relying on an
    // incorrect underlying construction method for Instrs; update
    // Instr creation to properly update these three state variables.
    public HCodeEdge[] edges() { return edges; }
    public HCodeEdge[] pred() { return pred; }
    public HCodeEdge[] succ() { return succ; }

    /** Accept a visitor. 
	<BR> <B>NOTE:</B> for VISITOR pattern to work, all subclasses
	                  must override this method with the body:
			  { v.visit(this); }
     */
    public void visit(InstrVisitor v) { v.visit(this); }
}
