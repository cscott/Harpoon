// Jasmin.java, created Mon Aug  2 13:55:50 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode.Jasmin;

import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Analysis.UseDef;
import harpoon.Util.WorkSet;
import harpoon.Analysis.Maps.TypeMap;

import java.io.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

/**
 * <code>FinalRaw</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Jasmin.java,v 1.1.2.4 1999-08-04 19:40:42 bdemsky Exp $
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
	out.println(".super "+hclass.getSuperclass().getName().replace('.','/'));
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
	out.println(".method "+Modifier.toString(hm[i].getModifiers())+" "+hm[i].getName().replace('.','/')
		    +hm[i].getDescriptor().replace('.','/'));
	outputQuads(out, hm[i], hc[i]);
	out.println(".end method");
    }

    public void outputQuads(PrintStream out,HMethod hm, HCode hc) {
	Object[] tuple=buildmap(hc);
	Map map=(Map) tuple[0];
	out.println("    .limit locals "+((Integer)tuple[1]).toString());
	//Fix ME
	//This is wrong....
	out.println("    .limit stack "+((Integer)tuple[1]).toString());
	WorkSet done=new WorkSet();
	Visitor visitor=new Visitor(out, map,hc);
	Quad start=(Quad)hc.getRootElement();
	METHOD m=(METHOD)start.next(1);
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    visitor.labeler(h.next(0));
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements()) {
		Quad q=(Quad)e.nextElement();
		String tmp=visitor.labeler(q);
		tmp=visitor.labeler(q.next(0));
	    }
	}
	visitAll(out,visitor,start.next(1), done);
	visitAll(out,visitor,start.next(0), done);
    }
		 
    private static final void visitAll(PrintStream out, Visitor visitor, Quad start, Set qm) {
	start.visit(visitor);
	if (start.next().length!=0)
	    if (qm.contains(start.next(0)))
		out.println("    goto "+visitor.labeler(start.next(0)));
	qm.add(start);
        Quad[] ql = start.next();
        for (int i=0; i<ql.length; i++) {
            if (qm.contains(ql[i])) continue; // skip if already done.
            visitAll(out, visitor, ql[i],qm);
        }
    }

    class Visitor extends QuadVisitor {
	PrintStream out;
	int label;
	Map tempmap;
	HashMap labelmap;
	HCode hc;
	TypeInfo tm;

	Visitor(PrintStream out, Map tempmap, HCode hc) {
	    this.out=out;
	    this.label=0;
	    this.tempmap=tempmap;
	    this.hc=hc;
	    labelmap=new HashMap();
	    this.tm=new TypeInfo();
	}

	public void visit(Quad q) {
	    System.out.println("**********Unhandled quad"+q.toString());
	}

	public void visit(METHOD q) {
	}

	public void visit(HEADER q) {
	}

	public void visit(PHI q) {
	    out.println(labeler(q)+":");
	}

	public void visit(FOOTER q) {
	    out.println(labeler(q)+":");
	    out.println("    return");
	}

	public void visit(HANDLER q) {
	    Enumeration e=q.protectedQuads();
	    String h=labeler(q);
	    out.println(h+":");
	    store(q.exceptionTemp());
	    out.println("    goto "+labeler(q.next(0)));
	    while(e.hasMoreElements()) {
		Quad qd=(Quad)e.nextElement();
		String start=labeler(qd);
		String stop=labeler(qd.next(0));
		String handler=labeler(q);
		if (q.caughtException()!=null)
		    out.println(".catch "+q.caughtException().getName().replace('.','/')+" from "+start+" to "+stop+" using "+handler);
		else
		    out.println(".catch all from "+start+" to "+stop+" using "+handler);
	    }
	}

	public void visit(CALL q) {
	    if (q.isVirtual()&&!q.isInterfaceMethod()) {
		//virtual method
		out.println(iflabel(q));
		for(int i=0;i<q.params().length;i++) {
		    load(q.params(i)); 
		}
		out.println("    invokevirtual "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
		if (q.retval()!=null)
		    store(q.retval());
	    } 
	    else {
		if (q.isInterfaceMethod()) {
		    System.out.println("Error in call"+q.toString());
		}
		else
		    if(q.isStatic()) {
			//static method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q.params(i)); 
			}
			out.println("    invokestatic "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
			if (q.retval()!=null)
			    store(q.retval());
		    }
		    else {
					//non-virtual method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q.params(i)); 
			}
			out.println("    invokespecial "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
			if (q.retval()!=null)
			    store(q.retval());
		    }
	    }
	}

	public void visit(GET q) {
	    if (q.objectref()==null) {
		//Static
		out.println(iflabel(q));
		out.println("    getstatic "
			    +q.field().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.field().getName().replace('.','/')
			    +" "+q.field().getDescriptor().replace('.','/'));
	    }
	    else {
		load(q.objectref());
		out.println(iflabel(q));
		out.println("    getfield "+q.field());
	    }
	    store(q.dst());
	}


	public void visit(CJMP q) {
	    String target=labeler(q);
	    TempInfo tempinfo=(TempInfo)tempmap.get(q.test());
	    out.println(iflabel(q));
	    iload(tempinfo);
	    out.println("    ifne "+labeler(q.next(1)));
	}

	public void visit(THROW q) {
	    out.println(iflabel(q));
	    load(q.throwable());
	    out.println("    athrow");
	}

	public void visit(MOVE q) {
	    out.println(iflabel(q));
	    load(q.src());
	    store(q.dst());
	}

	public void visit(NEW q) {
	    out.println(iflabel(q));
	    out.println("    new "+q.hclass().getName().replace('.','/'));
	    store(q.dst());
	}

	public void visit(CONST q) {
	    out.println(iflabel(q));
	    if (q.value()!=null) {
		HClass hclass=q.type();
		if (hclass==HClass.forName("java.lang.String"))
		    out.println("    ldc "+'"'+q.value().toString()+'"');
		else
		    out.println("    ldc "+q.value().toString());
	    }
	    else out.println("    aconst_null");
	    store(q.dst());

	    //HClass.Void
	}
	
	public void visit(RETURN q) {
	    out.println(iflabel(q));
	    if (q.retval()!=null) {
		String operand="Error";
		HClass tp=tm.typeMap(hc,q.retval());
		if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="    ireturn";
	    else
		if (tp==HClass.Double)
		    operand="    dreturn";
		else
		    if (tp==HClass.Float)
			operand="    freturn";
		    else
			if (tp==HClass.Long)
			    operand="    lreturn";
			else
			    operand="    areturn";
		load(q.retval());
		out.println(operand);
	    }
	    else
		out.println("    return");
	}

	public void visit(OPER q) {
	    switch (q.opcode()) {
	    case Qop.ACMPEQ:
		String l1=label(), l2=label();
		for (int i=0;i<q.operandsLength();i++) {
		    TempInfo tempinfo=(TempInfo)tempmap.get(q.operands(i));
		    aload(tempinfo);
		}
		out.println(iflabel(q));
		out.println("    if_acmpeq "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		//Need code to do jump/etc...
		//To store 0/1
		store(q.dst());
		break;
	    default:
		out.println(q.toString()+" unimplemented");
	    }
	}

	private void store(Temp t) {
	    HClass tp=tm.typeMap(hc,t);
	    String operand="***store error";
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="istore ";
	    else
		if (tp==HClass.Double)
		    operand="dstore ";
		else
		    if (tp==HClass.Float)
			operand="fstore ";
		    else
			if (tp==HClass.Long)
			    operand="lstore ";
			else
			    operand="astore ";
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		out.println("    "+operand+(new Integer(dest.localvar)).toString());
	}

	private void load(Temp t) {
	    String operand="***load error";
	    HClass tp=tm.typeMap(hc,t);
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="iload ";
	    else
		if (tp==HClass.Double)
		    operand="dload ";
		else
		    if (tp==HClass.Float)
			operand="fload ";
		    else
			if (tp==HClass.Long)
			    operand="lload ";
			else
			    operand="aload ";
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		out.println("    "+operand+(new Integer(dest.localvar)).toString());
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

	private String iflabel(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q)+(new String(":"));
	    else return new String("");
	}

	private String label() {
	    return "L"+(new Integer(label++)).toString();
	}
    }

    public final Object[] buildmap(final HCode code) {
	Quad q=(Quad)code.getRootElement();
	METHOD m=(METHOD) q.next(1);
	UseDef ud=new UseDef();
	Temp[] alltemp=ud.allTemps(code);
	HashMap stacktemps=new HashMap();
	int j=0;
	for(int i=0;i<m.params().length;i++) {
	    stacktemps.put(m.params(i),new TempInfo(j++));
	}
	for (int i=0;i<alltemp.length;i++) {
	    Temp next=alltemp[i];
	    HCodeElement[] defs=ud.defMap(code,next);
	    if (!stacktemps.containsKey(next)) {
		if (defs.length==1) {
		    HCodeElement[] uses=ud.useMap(code,next);
		    if (uses.length==1) {
			checkPair(stacktemps, (Quad)  uses[0], (Quad) defs[0], next);
		    }
		}
	    }
	}
	for (int i=0;i<alltemp.length;i++) {
	    if (!stacktemps.containsKey(alltemp[i])) {
		stacktemps.put(alltemp[i],new TempInfo(j++));
	    }
	}
	return new Object[] {stacktemps, new Integer(j)};
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
	    if ((flag)&&(track.isEmpty())) {
		stacktemps.put(t, new TempInfo(true));
	    } 
	}
    }
}
