// LoopMap.java, created Thu Jun 17 16:11:41 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.WorkSet;
import harpoon.IR.Quads.Quad;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;

import java.util.Set;
/**
 * <code>LoopMap</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopMap.java,v 1.1.2.6 2001-11-08 00:22:21 cananian Exp $
 */
public class LoopMap implements TempMap {
    
    /** Creates a <code>LoopMap</code>. */
    public LoopMap(HCode hc,Loops lp, TempMap ssitossa) {
	//We need to build to map in the constructor
	//Set HCode pointer
        this.lp=lp;
	this.hc=hc;
	this.ud=new UseDef();
	this.elements=lp.loopIncElements();
	this.ssitossa=ssitossa;
    }

    public Temp tempMap(Temp t) {
	//Breadth first search
	WorkSet todo=new WorkSet();	    
	WorkSet newpile=new WorkSet();
	todo.push(t);
	Temp finalt=ssitossa.tempMap(t);
	Temp n=null;
	while (true) {
	    if (todo.isEmpty()) {
		//go down to next layer
		todo=newpile;
		newpile=new WorkSet();
	    }
	    n=(Temp)todo.pop();
	    if (n==finalt) break;
	    HCodeElement []sources=ud.defMap(hc,n);
	    Quad q=(Quad)sources[0];
	    if (elements.contains(q)) {
		Temp []uses=q.use();
		for (int i=0;i<uses.length;i++)
		    if (finalt==ssitossa.tempMap(uses[i]))
			newpile.push(uses[i]);	       
	    } else break;
   	}
	return n;
    }
    
    TempMap ssitossa;
    HCode hc;
    Set elements;
    UseDef ud;
    Loops lp;
}



