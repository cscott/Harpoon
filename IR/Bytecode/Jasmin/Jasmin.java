// Jasmin.java, created Mon Aug  2 13:55:50 1999 by root
// Copyright (C) 1999 root <root@kikashi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode.Jasmin;

import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Analysis.UseDef;
import harpoon.Util.WorkSet;

import java.io.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;
/**
 * <code>FinalRaw</code>
 * 
 * @author  root <root@kikashi.lcs.mit.edu>
 * @version $Id: Jasmin.java,v 1.1.2.1 1999-08-03 06:56:34 bdemsky Exp $
 */
public class Jasmin {
    HCode[] hc;
    HMethod[] hm;
    HClass hclass;
    String classname;

    public Jasmin(HCode[] hc, HMethod[] hm, HClass hclass) {
	this.hc = hc;
	this.hm = hm;
	this.hclass=hclass;
	this.classname=hclass.getName();

	
    }

    public void outputClass(PrintStream out) {
	if (hclass.isInterface()) 
	    out.println(".interface "+Modifier.toString(hclass.getModifiers())+" "+hclass.getName().replace('.','/'));
	    else
	out.println(".class "+Modifier.toString(hclass.getModifiers())+" "+hclass.getName().replace('.','/'));
	out.println(".super "+Modifier.toString(hclass.getSuperclass().getModifiers())+" "+hclass.getSuperclass().getName().replace('.','/'));
	HClass[] interfaces=hclass.getInterfaces();
	for (int i=0;i<interfaces.length;i++)
	    out.println(".implements "+interfaces[i].getName().replace('.','/'));
	HField[] hfields=hclass.getDeclaredFields();
	for (int i=0;i<hfields.length;i++) {
	    String value="";
	    if (hfields[i].isConstant())
		value=" = "+hfields[i].getConstant().toString();
	    out.println(".field "+Modifier.toString(hfields[i].getModifiers())+" "+hfields[i].getName() +" "+hfields[i].getDescriptor()+value);
	}
	for(int i=0;i<hc.length;i++) {
	    outputMethod(out, i);
	}
    }

    public void outputMethod(PrintStream out, int i) {
	out.println(".method "+hm[i].toString());
	outputQuads(out, hm[i], hc[i]);
	out.println(".end method");
    }

    public void outputQuads(PrintStream out,HMethod hm, HCode hc) {
	Map map=buildmap(hc);
	WorkSet done=new WorkSet();
	Visitor visitor=new Visitor(out, map,done);
	Quad start=(Quad)hc.getRootElement();
	visitAll(visitor,start, done);
    }
		 
    private static final void visitAll(Visitor visitor, Quad start, Set qm) {
	start.visit(visitor);
	qm.add(start);
        Quad[] ql = start.next();
        for (int i=0; i<ql.length; i++) {
            if (qm.contains(ql[i])) continue; // skip if already done.
            visitAll(visitor, ql[i],qm);
        }
    }


    class Visitor extends QuadVisitor {
	PrintStream out;
	int label;
	Map tempmap;
	HashMap labelmap;
	Set done;

	Visitor(PrintStream out, Map tempmap, Set done) {
	    this.out=out;
	    this.label=0;
	    this.tempmap=tempmap;
	    labelmap=new HashMap();
	    this.done=done;
	}

	public void visit(Quad q) {
	    System.out.println("**********Unhandled quad"+q.toString());
	}

	public void visit(HEADER q) {
	}

	public void visit(PHI q) {
	    out.println(labeler(q)+": "+q.toString());
	}

	public void visit(CJMP q) {
	    String target=labeler(q);
	    TempInfo tempinfo=(TempInfo)tempmap.get(q.test());
	    iload(tempinfo);
	    out.println("    ifne "+labeler(q.next(1)));
	    if (done.contains(q.next(0)))
		out.println("    goto"+labeler(q.next(0)));
	}

	public void visit(THROW q) {
	    TempInfo tempinfo=(TempInfo)tempmap.get(q.throwable());
	    aload(tempinfo);
	    out.println("    athrow");
	}

	public void visit(MOVE q) {
	    TempInfo dst=(TempInfo)tempmap.get(q.dst());
	    TempInfo src=(TempInfo)tempmap.get(q.src());
	    iload(src);
	    istore(dst);
	}

	public void visit(CONST q) {
	    if (q.value()!=null)
		out.println("    ldc "+q.value().toString());
	    else out.println("    ldc null");
	    TempInfo ti=(TempInfo)tempmap.get(q.dst());
	    HClass tp=q.type();
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		istore(ti);
	    else
		if (tp==HClass.Double)
		    dstore(ti);
		else
		    if (tp==HClass.Float)
			fstore(ti);
		    else
			if (tp==HClass.Long)
			    lstore(ti);
			else
			    astore(ti);
	    //HClass.Void
	}
	

	public void visit(OPER q) {
	    switch (q.opcode()) {
	    case Qop.ACMPEQ:
		for (int i=q.operandsLength()-1;i>=0;i--) {
		    TempInfo tempinfo=(TempInfo)tempmap.get(q.operands(i));
		    aload(tempinfo);
		}
		out.println("    if_acmpeq ");
		//Need code to do jump/etc...
		//To store 0/1
		TempInfo dest=(TempInfo)tempmap.get(q.dst());
		astore(dest);
		break;
	    default:
		out.println(q.toString()+" unimplemented");
	    }
	}

	private void astore(TempInfo t) {
	    if (!t.stack)
		out.println("    astore "+(new Integer(t.localvar)).toString());
	}

	private void aload(TempInfo t) {
	    if (!t.stack)
		out.println("    aload "+(new Integer(t.localvar)).toString());
	}
	private void dstore(TempInfo t) {
	    if (!t.stack)
		out.println("    dstore "+(new Integer(t.localvar)).toString());
	}

	private void lstore(TempInfo t) {
	    if (!t.stack)
		out.println("    lstore "+(new Integer(t.localvar)).toString());
	}

	private void fstore(TempInfo t) {
	    if (!t.stack)
		out.println("    fstore "+(new Integer(t.localvar)).toString());
	}

	private void istore(TempInfo t) {
	    if (!t.stack)
		out.println("    istore "+(new Integer(t.localvar)).toString());
	}
	
	private void iload(TempInfo t) {
	    if (!t.stack)
		out.println("    iload "+(new Integer(t.localvar)).toString());
	}

	private String labeler(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q);
	    else {
		String label=label();
		labelmap.put(q,label);
		return label;
	    }
	}

	private String label() {
	    return "L"+(new Integer(label++)).toString();
	}
    }

    public final Map buildmap(final HCode code) {
	UseDef ud=new UseDef();
	Temp[] alltemp=ud.allTemps(code);
	HashMap stacktemps=new HashMap();
	for (int i=0;i<alltemp.length;i++) {
	    Temp next=alltemp[i];
	    HCodeElement[] defs=ud.defMap(code,next);
	    if (defs.length!=1) break;
	    HCodeElement[] uses=ud.useMap(code,next);
	    if (uses.length!=1) break;
	    checkPair(stacktemps, (Quad)  uses[0], (Quad) defs[0], next);
	}
	int j=0;
	for (int i=0;i<alltemp.length;i++) {
	    if (!stacktemps.containsKey(alltemp[i])) {
		stacktemps.put(alltemp[i],new TempInfo(j++));
	    }
	}
	return stacktemps;
    }

    void checkPair(Map stacktemps, final Quad use, final Quad def, Temp t) {
	Quad ptr=def;
	boolean flag=true;
	WorkSet track=new WorkSet();
	if (ptr.def().length==1) {
	    while (ptr!=use) {
		if (ptr.next().length!=1) {
		    flag=false;break;}
		Temp[] defs=ptr.def();
		for (int i=0;i<defs.length;i++)
		    if (stacktemps.containsKey(defs[i]))
			track.push(defs[i]);
       		ptr=ptr.next(0);
		if (ptr.prev().length!=1) {
		    flag=false;break;}
		Temp[] uses=ptr.use();
		for (int i=uses.length-1;i>=0;i--) {
		    if (uses[i]==t) break;
		    if (stacktemps.containsKey(uses[i])) {
			if (track.contains(uses[i]))
			    track.remove(uses[i]);
			else
			    {flag=false; break;}
		    }
		}
	    }
	    if ((flag)&&(track.size()==0)) {
		stacktemps.put(t, new TempInfo(true));
	    }
	}
    }
}





