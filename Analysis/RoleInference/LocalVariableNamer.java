// LocalVariableNamer, created Wed Jun  6 15:14:26 2001 by root
// Copyright (C) 2000 root <root@windsurf.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.RoleInference;
import harpoon.IR.RawClass.AttributeCode;
import harpoon.IR.RawClass.AttributeLineNumberTable;
import harpoon.IR.RawClass.AttributeLocalVariableTable;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Loader;
import harpoon.Util.Util;

import java.io.InputStream;

/**
 * <code>LocalVariableNamer</code>
 * 
 * @author  root <root@windsurf.lcs.mit.edu>
 * @version $Id: LocalVariableNamer.java,v 1.1.2.2 2001-06-14 20:14:04 bdemsky Exp $
 */
public class LocalVariableNamer {

    AttributeCode ac = null;
    AttributeLineNumberTable alnt = null;
    AttributeLocalVariableTable alvt = null;    

    /** Creates a <code>LocalVariableNamer</code>. */
    public LocalVariableNamer(HMethod hm) {
        /* create a RawClass for the declaring class of HMethod hm */
	HClass hc=hm.getDeclaringClass();
	String methodname=hm.getName();
	if (methodname.endsWith("$$initcheck"))
	    methodname=methodname.substring(0,methodname.length()-11);
        String className = hc.getName();
	//System.out.println(className+" "+hm.getName()+" "+methodname);
        InputStream is =
            Loader.getResourceAsStream(Loader.classToResource(className));
        harpoon.IR.RawClass.ClassFile raw =
            new harpoon.IR.RawClass.ClassFile(is);
        /* now find the appropriate MethodInfo */
        harpoon.IR.RawClass.MethodInfo mi = null;
        for (int i=0; i<raw.methods.length; i++)
            if (raw.methods[i].name().equals(methodname) &&
                raw.methods[i].descriptor().equals(hm.getDescriptor()))
                mi = raw.methods[i];
        Util.assert(mi!=null);
        /* find the Code attribute of the MethodInfo */

        for (int i=0; i<mi.attributes.length; i++)
            if (mi.attributes[i] instanceof AttributeCode)
                ac = (AttributeCode) mi.attributes[i];
        Util.assert(ac!=null); /* or else this is an abstract method */	

        for (int i=0; i<ac.attributes.length; i++)
            if (ac.attributes[i] instanceof AttributeLineNumberTable)
                alnt = (AttributeLineNumberTable) ac.attributes[i];
            else if (ac.attributes[i] instanceof AttributeLocalVariableTable)
                alvt = (AttributeLocalVariableTable) ac.attributes[i];

	//	if (alnt!=null)
	//alnt.print(new java.io.PrintWriter(System.out,true),3);
	//if (alvt!=null)
	//alvt.print(new java.io.PrintWriter(System.out,true),3);

    }
    
    public String lv_name(int lv_index, int line_number) {
        /* if we don't have both alnt and avnt we don't know the name */
        if (alnt==null || alvt==null) return null;
        Util.assert(lv_index < ac.max_locals); /* or invalid lv_index */
        /* now find the local variable and line number table attributes */
        /* otherwise, return the first appropriate name */
        for (int i=0; i<alnt.line_number_table.length; i++) {
            if (alnt.line_number_table[i].line_number==line_number) {
                int start_pc = alnt.line_number_table[i].start_pc;
                int end_pc = ac.code.length;
                if (i+1<alnt.line_number_table.length)
                    end_pc = alnt.line_number_table[i+1].start_pc;
                // xxx: the javadoc for AttributeLocalVariableTable
                // indicates that we should probably not depend on the
                // table being sorted, but every instance i've seen has
                // been sorted.
                for (int pc=start_pc; pc<end_pc; pc++) {
                    // check for lv entry at this pc
                    String ln = alvt.localName(pc, lv_index);
                    if (ln!=null) return ln;
                }
            }
        }

        for (int i=0; i<alnt.line_number_table.length; i++) {
            if (alnt.line_number_table[i].line_number==line_number) {
                int start_pc = alnt.line_number_table[i].start_pc;
                int end_pc = ac.code.length;
                if (i+1<alnt.line_number_table.length)
                    end_pc = alnt.line_number_table[i+1].start_pc;
                // xxx: the javadoc for AttributeLocalVariableTable
                // indicates that we should probably not depend on the
                // table being sorted, but every instance i've seen has
                // been sorted.
		int pc=end_pc;
		// check for lv entry at this pc
		String ln = alvt.localName(pc, lv_index);
		if (ln!=null) return ln;
            }
        }
        /* no relevant entries found. */
        return null;
    }

    
}
