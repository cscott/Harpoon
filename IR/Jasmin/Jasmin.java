// Jasmin.java, created Mon Aug  2 13:55:50 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Jasmin;

import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.LABEL;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.UseDef;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Util;
import harpoon.Analysis.Maps.TypeMap;

import java.io.PrintStream;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * <code>Jasmin</code> converts from QuadWithTry to bytecode
 * formatted for the Jasmin assembler.  
 * Note:  Requires patch on 1.06 to do sane things with
 * fields.
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Jasmin.java,v 1.3.2.1 2002-02-27 08:36:14 cananian Exp $
 */
public class Jasmin {
    HCode[] hc;
    HMethod[] hm;
    HClass hclass;
    String classname;
    int andmask,  ormask;

    /**Creates a <code>Jasmin</code> object.*/
    public Jasmin(HCode[] hc, HMethod[] hm, HClass hclass) {
	this.hc = hc;
	this.hm = hm;
	this.hclass=hclass;
	this.classname=hclass.getName();
	this.andmask=Modifier.ABSTRACT|Modifier.FINAL|Modifier.INTERFACE|
	    Modifier.NATIVE|Modifier.PRIVATE|Modifier.PROTECTED|Modifier.PUBLIC|
	    Modifier.STATIC|Modifier.SYNCHRONIZED|Modifier.TRANSIENT|Modifier.VOLATILE;
	this.ormask=0;
    }

    /**Creates a <code>Jasmin</code> object.*/
    public Jasmin(HCode[] hc, HMethod[] hm, HClass hclass, int andmask, int ormask) {
	this.hc = hc;
	this.hm = hm;
	this.hclass=hclass;
	this.classname=hclass.getName();
	this.andmask=andmask;
	this.ormask=ormask;
    }

    /**Takes in a <code>PrintStream</code> 'out' and dumps
     * Jasmin formatted assembly code to it.*/
    public void outputClass(PrintStream out) {
	if (hclass.isInterface()) 
	    out.println(".interface "+Modifier.toString(ormask|(andmask&hclass.getModifiers()))+" "+Util.jasminEscape(hclass.getName().replace('.','/')));
	    else
	out.println(".class "+Modifier.toString(ormask|(andmask&hclass.getModifiers()))+" "+Util.jasminEscape(hclass.getName().replace('.','/')));

	if (hclass.getSuperclass()!=null)
	    out.println(".super "+Util.jasminEscape(hclass.getSuperclass().getName().replace('.','/')));
	else
	    if (hclass.isInterface())
		out.println(".super java/lang/Object");

	//List the interfaces
	HClass[] interfaces=hclass.getInterfaces();
	for (int i=0;i<interfaces.length;i++)
	    out.println(".implements "+Util.jasminEscape(interfaces[i].getName().replace('.','/')));
	HField[] hfields=hclass.getDeclaredFields();

	//List the fields
	for (int i=0;i<hfields.length;i++) {
	    String value="";
	    if (hfields[i].isConstant())
		if (hfields[i].getType().getName().equals("java.lang.String"))
		    value=" = "+'"'+Util.jasminEscape(hfields[i].getConstant().toString())+'"';
		else if (hfields[i].getType()==HClass.Float)
		    value=" = "+escapeFloat((Float)hfields[i].getConstant());
		else if (hfields[i].getType()==HClass.Double)
		    value=" = "+escapeDouble((Double)hfields[i].getConstant());
		else
		    value=" = "+Util.jasminEscape(hfields[i].getConstant().toString());
	    out.println(".field "+Modifier.toString(ormask|(andmask&hfields[i].getModifiers()))+" "+Util.jasminEscape(hfields[i].getName()) +" "+Util.jasminEscape(hfields[i].getDescriptor())+value);
	}

	//List the methods
	for(int i=0;i<hc.length;i++) {
	    outputMethod(out, i);
	}
    }

   
    private void outputMethod(PrintStream out, int i) {
	out.println(".method "+Modifier.toString(ormask|(andmask&hm[i].getModifiers()))+" "+Util.jasminEscape(hm[i].getName().replace('.','/'))
		    +Util.jasminEscape(hm[i].getDescriptor().replace('.','/')));
	//Get modifiers
	int modifiers=hm[i].getModifiers();
	//Only print method quads if it isn't Abstract
	if ((!Modifier.isAbstract(modifiers))&&
	    (!Modifier.isNative(modifiers)))
	    outputQuads(out, hm[i], hc[i]);

	out.println(".end method");
    }

    private void outputQuads(PrintStream out,HMethod hm, HCode hc) {
	//Output assembly from the quads

	TypeMap tp=((QuadWithTry)hc).typeMap();
	assert tp!=null : "Need TypeMap support";
	//Build mapping from temps to localvars/stack
	Object[] tuple=buildmap(hc,tp);
	//Store the map
	Map map=(Map) tuple[0];
	//Print out the number of local variables
	out.println("    .limit locals "+((Integer)tuple[1]).toString());

	//FIX ME
	//This is wrong....
	int depth=finddepth(map, tp, hm, hc);

	out.println("    .limit stack "+depth);
	WorkSet done=new WorkSet();
	Visitor visitor=new Visitor(out, map,hc,tp);
	Quad start=(Quad)hc.getRootElement();
	METHOD m=(METHOD)start.next(1);
	//Loop through the handlers
	//We do this to label the quad ranges protected by the handler
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    visitor.labeler(h.next(0));
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements()) {
		Quad q=(Quad)e.nextElement();
		String tmp=visitor.labeler(q);
		//tmp=visitor.labeler(q.next(0));
	    }
	}
	//Visit all the quads
	visitAll(out,visitor,start.next(1), done);
    }
		 
    private static final void visitAll(PrintStream out, Visitor visitor, Quad start, Set qm) {
	//Visit the node passed to us
	start.accept(visitor);
	//If we have a node after us, but we've already printed it
	//we need to do a goto
	boolean sup=visitor.supGoto();
	if (sup==false)
	    if (start.next().length!=0)
		if (qm.contains(start.next(0)))
		    out.println("    goto "+visitor.labeler(start.next(0)));
	//add this node to the done list
	qm.add(start);
	//visit the kids
        Quad[] ql = start.next();
        for (int i=0; i<ql.length; i++) {
            if (qm.contains(ql[i])) continue; // skip if already done.
            visitAll(out, visitor, ql[i],qm);
        }
    }

    class CJMPVisitor extends QuadVisitor {
	private boolean cjmp;
	CJMPVisitor() {
	    cjmp=false;
	}
	public void visit(Quad q) {
	    cjmp=false;
	}
	public void visit(CJMP q) {
	    cjmp=true;
	}
	public boolean cjmp() {
	    return cjmp;
	}
    }

    /** This Visitor prints out opcodes for each quad visited.*/

    class Visitor extends QuadVisitor {
	PrintStream out;
	int label;
	Map tempmap;
	HashMap labelmap;
	HCode hc;
	TypeMap tm;
	UseDef ud;
	WorkSet skip;
	CJMPVisitor cjmp;
	boolean supGoto;

	Visitor(PrintStream out, Map tempmap, HCode hc, TypeMap tm) {
	    this.out=out;
	    this.label=0;
	    this.tempmap=tempmap;
	    this.hc=hc;
	    labelmap=new HashMap();
	    this.tm=tm;
	    this.skip=new WorkSet();
	    this.ud=new UseDef();
	    this.cjmp=new CJMPVisitor();
	    this.supGoto=false;
	}

	public boolean supGoto() {
	    if (supGoto) {
		supGoto=false;
		return true;}
	    return false;
	}
	    

	public void visit(Quad q) {
	    System.out.println("**********Unhandled quad"+q.toString());
	}

	public void visit(AGET q) {
	    String operand="***AGET error";

	    HClass ty = tm.typeMap(q, q.objectref());
	    HClass tp=ty.getComponentType();
	    if (tp==HClass.Boolean)
		operand="baload";
	    else if (tp==HClass.Byte)
		operand="baload";
	    else if(tp==HClass.Char)
		operand="caload";
	    else if (tp==HClass.Short)
		operand="saload";
	    else if (tp==HClass.Int)
		operand="iaload";
	    else if (tp==HClass.Double)
		operand="daload";
	    else if (tp==HClass.Float)
		operand="faload";
	    else if (tp==HClass.Long)
		operand="laload";
	    else
		operand="aaload";

	    out.println(iflabel(q));
	    load(q,q.objectref());
	    load(q,q.index());
	    out.println("    "+operand);
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(ALENGTH q) {
	    out.println(iflabel(q));
	    load(q,q.objectref());
	    out.println("    arraylength");
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(ANEW q) {
	    out.println(iflabel(q));
	    for (int i=0;i<q.dimsLength();i++)
		load(q,q.dims(i));
	    out.println("    multianewarray "+Util.jasminEscape(q.hclass().getName().replace('.','/')) +" "+q.dimsLength());
            store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(ARRAYINIT q) {
	    out.println(iflabel(q));
	    if (q.value().length>0)
		load(q,q.objectref());
	    for (int i=0;i<q.value().length;i++) {
		if (q.type()==HClass.Boolean) {
		    Boolean b=(Boolean)q.value()[i];
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    if (b.booleanValue())
			out.println("    iconst_1");
		    else
			out.println("    iconst_0");
		    out.println("    bastore");
		}
		else if (q.type()==HClass.Int) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc "+q.value()[i].toString());
		    out.println("    iastore");
		}
		else if (q.type()==HClass.Byte) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    bipush "+q.value()[i].toString());
		    out.println("    bastore");
		}
		else if (q.type()==HClass.Char) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    sipush "+((int) (((Character)q.value()[i]).charValue())));
		    out.println("    castore");
		}
		else if (q.type()==HClass.Short) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    sipush "+q.value()[i].toString());
		    out.println("    sastore");
		}
		else if (q.type()==HClass.Long) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc2_w "+q.value()[i].toString());
		    out.println("    lastore");
		}
		else if (q.type()==HClass.Double) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc2_w "+escapeDouble((Double)q.value()[i]));
		    out.println("    dastore");
		}
		else if (q.type()==HClass.Float) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc "+escapeFloat((Float)q.value()[i]));
		    out.println("    fastore");
		}
	    }
	    out.println(iflabel2(q));
	}
       
	private void dup(int i, int last) {
	    if ((i+1)!=last)
		out.println("    dup");
	}

	public void visit(ASET q) {
	    out.println(iflabel(q));
	    String operand="***ASET error";
	    HClass ty = tm.typeMap(q, q.objectref());
	    HClass tp=ty.getComponentType();
	    if (tp==HClass.Boolean)
		operand="bastore";
	    else if (tp==HClass.Byte)
		operand="bastore";
	    else if(tp==HClass.Char)
		operand="castore";
	    else if (tp==HClass.Short)
		operand="sastore";
	    else if (tp==HClass.Int)
		operand="iastore";
	    else if (tp==HClass.Double)
		operand="dastore";
	    else if (tp==HClass.Float)
		operand="fastore";
	    else if (tp==HClass.Long)
		operand="lastore";
	    else
		operand="aastore";

	    load(q,q.objectref());
	    load(q,q.index());
	    load(q,q.src());
	    out.println("    "+operand); 
	    out.println(iflabel2(q));
	}

	public void visit(COMPONENTOF q) {
	    //kludge
	    //fix me
	    System.out.println("ERROR: "+q.toString());
	    out.println(iflabel(q));
	    TempInfo dest=(TempInfo)tempmap.get(q.objectref());	    
	    if (dest.stack)
		out.println("    pop");
	    dest=(TempInfo)tempmap.get(q.arrayref());
	    if (dest.stack)
		out.println("    pop");
	    out.println("    bipush 1");
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(METHOD q) {
	}

	public void visit(HEADER q) {
	}

	public void visit(PHI q) {
	    out.println(labeler(q)+":");
	    out.println(iflabel2(q));
	}

	public void visit(FOOTER q) {
	    out.println(labeler(q)+":");
	    //not needed
	    //	    out.println("    return");
	    out.println(iflabel2(q));
	}

	public void visit(HANDLER q) {
	    Enumeration e=q.protectedQuads();
	    String h=labeler(q);
	    out.println(h+":");
	    store(q,q.exceptionTemp());
	    //out.println("    goto "+labeler(q.next(0)));
	    //covered by default...
	    while(e.hasMoreElements()) {
		Quad qd=(Quad)e.nextElement();
		String start=labeler(qd);
		String stop=labeler2(qd);
		String handler=labeler(q);
		if (q.caughtException()!=null)
		    out.println(".catch "+Util.jasminEscape(q.caughtException().getName().replace('.','/'))+" from "+start+" to "+stop+" using "+handler);
		else
		    out.println(".catch all from "+start+" to "+stop+" using "+handler);
	    }
	}

	public void visit(INSTANCEOF q) {
	    out.println(iflabel(q));
	    load(q,q.src());
	    out.println("    instanceof "+Util.jasminEscape(q.hclass().getName().replace('.','/')));
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}
	
	public void visit(CALL q) {
	    if (q.isVirtual()&&!q.isInterfaceMethod()) {
		//virtual method
		out.println(iflabel(q));
		for(int i=0;i<q.params().length;i++) {
		    load(q,q.params(i)); 
		}
		out.println("    invokevirtual "+
			    Util.jasminEscape(q.method().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.method().getName().replace('.','/'))
			    +Util.jasminEscape(q.method().getDescriptor().replace('.','/')));
		if (q.retval()!=null)
		    store(q,q.retval());
	    } 
	    else {
		if (q.isInterfaceMethod()) {
		    out.println(iflabel(q));
		    for(int i=0;i<q.params().length;i++) {
			load(q,q.params(i)); 
		    }
		    out.println("    invokeinterface "+
				Util.jasminEscape(q.method().getDeclaringClass().getName().replace('.','/'))
				+"/"+Util.jasminEscape(q.method().getName().replace('.','/'))
				+Util.jasminEscape(q.method().getDescriptor().replace('.','/'))
				+" "+q.params().length);
		    if (q.retval()!=null)
			store(q,q.retval());
		}
		else
		    if(q.isStatic()) {
			//static method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q,q.params(i)); 
			}
			out.println("    invokestatic "+
			    Util.jasminEscape(q.method().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.method().getName().replace('.','/'))
			    +Util.jasminEscape(q.method().getDescriptor().replace('.','/')));
			if (q.retval()!=null)
			    store(q,q.retval());
		    }
		    else {
					//non-virtual method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q,q.params(i)); 
			}
			out.println("    invokespecial "+
			    Util.jasminEscape(q.method().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.method().getName().replace('.','/'))
			    +Util.jasminEscape(q.method().getDescriptor().replace('.','/')));
			if (q.retval()!=null)
			    store(q,q.retval());
		    }
	    }
	    out.println(iflabel2(q));
	}

	public void visit(GET q) {
	    if (q.objectref()==null) {
		//Static
		out.println(iflabel(q));
		out.println("    getstatic "
			    +Util.jasminEscape(q.field().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.field().getName().replace('.','/'))
			    +" "+Util.jasminEscape(q.field().getDescriptor().replace('.','/')));
	    }
	    else {
		out.println(iflabel(q));
		load(q,q.objectref());
		out.println("    getfield "
			    +Util.jasminEscape(q.field().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.field().getName().replace('.','/'))
			    +" "+Util.jasminEscape(q.field().getDescriptor().replace('.','/')));
	    }
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(CJMP q) {
	    out.println(iflabel(q));
	    if (!skip.contains(q)) {
		load(q,q.test());
		out.println("    ifne "+labeler(q.next(1)));
	    }
	    out.println(iflabel2(q));
	}

	public void visit(THROW q) {
	    supGoto=true;
	    out.println(iflabel(q));
	    load(q,q.throwable());
	    out.println("    athrow");
	    out.println(iflabel2(q));
	}

	public void visit(MONITORENTER q) {
	    out.println(iflabel(q));
	    load(q,q.lock());
	    out.println("    monitorenter");
	    out.println(iflabel2(q));
	}

	public void visit(MONITOREXIT q) {
	    out.println(iflabel(q));
	    load(q,q.lock());
	    out.println("    monitorexit");
	    out.println(iflabel2(q));
	}

	public void visit(MOVE q) {
	    out.println(iflabel(q));
	    load(q,q.src());
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(NEW q) {
	    out.println(iflabel(q));
	    out.println("    new "+Util.jasminEscape(q.hclass().getName().replace('.','/')));
	    store(q,q.dst());
	    out.println(iflabel2(q));
	}

	public void visit(LABEL q) {
	    out.println(iflabel(q));
	    out.println(";"+q.toString());
	    out.println(iflabel2(q));
	}

	public void visit(NOP q) {
	    out.println(iflabel(q));
	    out.println("    nop");
	    out.println(iflabel2(q));
	}

	public void visit(CONST q) {
	    out.println(iflabel(q));
	    if (q.value()!=null) {
		HClass hclass=q.type();
		if (hclass.getName().equals("java.lang.String"))
		    out.println("    ldc "+'"'+Util.jasminEscape(q.value().toString())+'"');
		else
		    if (hclass==HClass.Double)
			out.println("    ldc2_w "+escapeDouble((Double)q.value()));
		    else if (hclass==HClass.Long)
			out.println("    ldc2_w "+q.value().toString());
		    else if (hclass==HClass.Float)
			out.println("    ldc "+escapeFloat((Float)q.value()));
		    else
			out.println("    ldc "+q.value().toString());
	    }
	    else out.println("    aconst_null");
	    store(q,q.dst());
	    out.println(iflabel2(q));
	    //HClass.Void
	}
	
	public void visit(RETURN q) {
	    supGoto=true;
	    out.println(iflabel(q));
	    if (q.retval()!=null) {
		String operand="Error";
		HClass tp=tm.typeMap(q,q.retval());
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
		load(q,q.retval());
		out.println(operand);
	    }
	    else
		out.println("    return");
	    out.println(iflabel2(q));
	}

	public void visit(SET q) {
	    if (q.objectref()==null) {
		//Static
		out.println(iflabel(q));
		load(q,q.src());
		out.println("    putstatic "
			    +Util.jasminEscape(q.field().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.field().getName().replace('.','/'))
			    +" "+Util.jasminEscape(q.field().getDescriptor().replace('.','/')));
	    }
	    else {
		out.println(iflabel(q));
		load(q,q.objectref());
		load(q,q.src());
		out.println("    putfield "
			    +Util.jasminEscape(q.field().getDeclaringClass().getName().replace('.','/'))
			    +"/"+Util.jasminEscape(q.field().getName().replace('.','/'))
			    +" "+Util.jasminEscape(q.field().getDescriptor().replace('.','/')));
	    }
	    out.println(iflabel2(q));
	}

	public void visit(SWITCH q) {
            out.println(iflabel(q));
	    load(q,q.index());
	    out.println("    lookupswitch");
	    for(int i=0;i<q.keysLength();i++)
		out.println("    "+q.keys(i)+" : "+labeler(q.next(i)));
	    out.println("    default : "+labeler(q.next(q.keysLength())));
	    out.println(iflabel2(q));
	}
	public void visit(TYPESWITCH q) {
	    throw new Error("TYPESWITCH quads should be removed before "+
			    "running this pass!  Use TypeSwitchRemover.");
	}

	public void visit(TYPECAST q) {
	    out.println(iflabel(q));
	    load(q,q.objectref());
	    out.println("    checkcast "+Util.jasminEscape(q.hclass().getName().replace('.','/')));
	    TempInfo dest=(TempInfo)tempmap.get(q.objectref());
	    store(q,q.objectref());
	    out.println(iflabel2(q));
	}

	//This method handles operands
	//really 3 cases:
	//normal, compares that are done with compares,
	//compares done with conditional branches

	public void visit(OPER q) {
	    out.println(iflabel(q));
	    switch (q.opcode()) {
	    case Qop.LCMPEQ:
		
		String l1=label(),l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q,q.operands(i));
		out.println("    lcmp");
		q.next(0).accept(cjmp);
		boolean testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			out.println("    ifeq "+labeler(qnext.next(1)));
		    } else
			testcjmp=false;
		} 
		if (!testcjmp) {
		    out.println("    ifeq "+l1);
		    out.println("    bipush 0");
		    out.println("    goto "+l2);
		    out.println(l1+":");
		    out.println("    bipush 1");
		    out.println(l2+":");
		    store(q,q.dst());
		}
		break;

	    case Qop.LCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q,q.operands(i));
		out.println("    lcmp");

	        q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			out.println("    ifgt "+labeler(qnext.next(1)));
		    } else
			testcjmp=false;
		} 
		if (!testcjmp) {
		    out.println("    ifgt "+l1);
		    out.println("    bipush 0");
		    out.println("    goto "+l2);
		    out.println(l1+":");
		    out.println("    bipush 1");
		    out.println(l2+":");
		    store(q,q.dst());
		}
		break;
	    case Qop.DCMPEQ:
	    case Qop.DCMPGE:
	    case Qop.DCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q,q.operands(i));
		out.println("    dcmpl");

		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
		    if (q.opcode()==Qop.DCMPEQ)
			out.println("    ifeq "+labeler(qnext.next(1)));
		    if (q.opcode()==Qop.DCMPGE)
			out.println("    ifge "+labeler(qnext.next(1)));
		    if (q.opcode()==Qop.DCMPGT)
			out.println("    ifgt "+labeler(qnext.next(1)));
		    } else
			testcjmp=false;
		}
		if (!testcjmp) {
		    if (q.opcode()==Qop.DCMPEQ)
			out.println("    ifeq "+l1);
		    if (q.opcode()==Qop.DCMPGE)
			out.println("    ifge "+l1);
		    if (q.opcode()==Qop.DCMPGT)
			out.println("    ifgt "+l1);
		    out.println("    bipush 0");
		    out.println("    goto "+l2);
		    out.println(l1+":");
		    out.println("    bipush 1");
		    out.println(l2+":");
		    store(q,q.dst());
		}
		break;

	    case Qop.FCMPEQ:
	    case Qop.FCMPGE:
	    case Qop.FCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q,q.operands(i));
		out.println("    fcmpl");

		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
		    if (q.opcode()==Qop.FCMPEQ)
			out.println("    ifeq "+labeler(qnext.next(1)));
		    if (q.opcode()==Qop.FCMPGE)
			out.println("    ifge "+labeler(qnext.next(1)));
		    if (q.opcode()==Qop.FCMPGT)
			out.println("    ifgt "+labeler(qnext.next(1)));
		    } else
			testcjmp=false;
		}

		if (!testcjmp) {
		    if (q.opcode()==Qop.FCMPEQ)
			out.println("    ifeq "+l1);
		    if (q.opcode()==Qop.FCMPGE)
			out.println("    ifge "+l1);
		    if (q.opcode()==Qop.FCMPGT)
			out.println("    ifgt "+l1);
		    out.println("    bipush 0");
		    out.println("    goto "+l2);
		    out.println(l1+":");
		    out.println("    bipush 1");
		    out.println(l2+":");
		    store(q,q.dst());
		}
		break;

	    case Qop.ACMPEQ:
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		String base=Qop.toString(q.opcode());
		l1=label();
		l2=label();
		for (int i=0;i<q.operandsLength();i++) {
		    load(q,q.operands(i));
		}
		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			out.println("    if_"+base+" "+labeler(qnext.next(1)));
		    } else
			testcjmp=false;
		}
		if (!testcjmp) {
		    out.println("    if_"+base+" "+l1);
		    out.println("    bipush 0");
		    out.println("    goto "+l2);
		    out.println(l1+":");
		    out.println("    bipush 1");
		    out.println(l2+":");
		    //Need code to do jump/etc...
		    //To store 0/1
		    store(q,q.dst());
		}
		break;
	    case Qop.D2F:
	    case Qop.D2I:
	    case Qop.D2L:
	    case Qop.DADD:
	    case Qop.DDIV:
	    case Qop.DMUL:
	    case Qop.DNEG:
	    case Qop.DREM:
	    case Qop.F2D:
	    case Qop.F2I:
	    case Qop.F2L:
	    case Qop.FADD:
	    case Qop.FDIV:
	    case Qop.FMUL:
	    case Qop.FNEG:
	    case Qop.FREM:
	    case Qop.I2B:
	    case Qop.I2C:
	    case Qop.I2D:
	    case Qop.I2F:
	    case Qop.I2L:
	    case Qop.I2S:
	    case Qop.IADD:
	    case Qop.IAND:
	    case Qop.IDIV:
	    case Qop.IMUL:
	    case Qop.INEG:
	    case Qop.IOR:
	    case Qop.IREM:
	    case Qop.ISHL:
	    case Qop.ISHR:
	    case Qop.IUSHR:
	    case Qop.IXOR:
	    case Qop.L2D:
	    case Qop.L2I:
	    case Qop.L2F:
	    case Qop.LADD:
	    case Qop.LAND:
	    case Qop.LDIV:
	    case Qop.LMUL:
	    case Qop.LNEG:
	    case Qop.LOR:
	    case Qop.LREM:
	    case Qop.LSHL:
	    case Qop.LSHR:
	    case Qop.LUSHR:
	    case Qop.LXOR:
		String cbase=Qop.toString(q.opcode());
		for (int i=0;i<q.operandsLength();i++) {
		    load(q,q.operands(i));
		}
		out.println("    "+cbase);
		store(q,q.dst());
		break;
	    default:
		out.println(q.toString()+" unimplemented");
	    }
	    out.println(iflabel2(q));
	}

	private void store(Quad q,Temp t) {
	    // Should be ok to pass null for HCodeElement param
	    HClass tp=tm.typeMap(q,t); 
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

	private void load(Quad q,Temp t) {
	    String operand="***load error";
	    // Should be ok to pass null for HCodeElement param
	    HClass tp=tm.typeMap(q,t);
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

	//Give unique labels to quads
	private String labeler(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q);
	    else {
		String label=label();
		labelmap.put(q,label);
		return label;
	    }
	}
	//End of quad label
	private String labeler2(Quad q) {
	    if (labelmap.containsKey(q))
		return (new String ("E"))+(String)labelmap.get(q);
	    else {
		String label=label();
		labelmap.put(q,label);
		return (new String("E"))+(String)label;
	    }
	}

	//Returns string with label in it
	private String iflabel(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q)+(new String(":"));
	    else return new String("");
	}

	//Returns string with label in it
	private String iflabel2(Quad q) {
	    if (labelmap.containsKey(q))
		return (new String("E"))+(String)labelmap.get(q)+(new String(":"));
	    else return new String("");
	}

	//Assigns label numbers
	private String label() {
	    return "L"+(new Integer(label++)).toString();
	}
    }

    /** <code>buildmap</code> takes in a <code>HCode</code> and a <code>TypeMap</code>
     *  and returns a Map and number of local variables used.
     *  Buildmap maps each temp either to the stack or to a localvariable.*/

    public final Object[] buildmap(final HCode code, TypeMap tp) {
	Quad q=(Quad)code.getRootElement();
	METHOD m=(METHOD) q.next(1);
	UseDef ud=new UseDef();
	Temp[] alltemp=ud.allTemps(code);
	HashMap stacktemps=new HashMap();
	WorkSet badquads=new WorkSet();
	int j=0;

	//Cycle through the formal parameters...Assign them to their
	//appropriate local variables
	for(int i=0;i<m.params().length;i++) {
	    stacktemps.put(m.params(i),new TempInfo(j++));
	    HCodeElement[] defs=ud.defMap(code,m.params(i));
	    //gross hack...cycle through all defs
	    for (int k=0; k<defs.length;k++) {
		HClass hclass=tp.typeMap(defs[k],m.params(i));
		if ((hclass==HClass.Double)||(hclass==HClass.Long)) {
		    j++;
		    break;
		}
	    }
	}


	//Make a set containing all of the quads protected
	//by handler quads...
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements())
		badquads.add((Quad)e.nextElement());
	}

	//Cycled through all temps defined and used once
	//Consider them for placement on stack

	for (int i=0;i<alltemp.length;i++) {
	    Temp next=alltemp[i];
	    HCodeElement[] defs=ud.defMap(code,next);
	    if (!stacktemps.containsKey(next)) {
		if (defs.length==1) {
		    HCodeElement[] uses=ud.useMap(code,next);
		    if (uses.length==1) {
			checkPair(stacktemps, (Quad)  uses[0], (Quad) defs[0], next, badquads);
		    }
		}
	    }
	}

	//Iterate through the temps we've placed so far

	Iterator iterate=stacktemps.keySet().iterator();
	while (iterate.hasNext()) {
	    Temp next=(Temp)iterate.next();

	    //Only worry about the ones on the stack
	    if (((TempInfo)stacktemps.get(next)).stack) {
		HCodeElement[] huses=ud.useMap(code,next);
		HCodeElement[] hdefs=ud.defMap(code,next);
		Quad use=(Quad)huses[0];
		Quad def=(Quad)hdefs[0];
		Temp[] uses=use.use();
		Temp[] defs=def.def();
		boolean flag=true;
		boolean broken=false;

		//go through the using quad
		for (int i=0;i<uses.length;i++) {
		    //If we have an temp before 'next'
		    //that isn't on the stack, we can't put it
		    //under 'next'

		    if (!stacktemps.containsKey(uses[i]))
			flag=false;
		    else
			if (!((TempInfo)stacktemps.get(uses[i])).stack)
			    flag=false;
		    if ((flag==false)&&(uses[i]==next)) {
			broken=true;
			break;
		    }
		}
		flag=true;

		//if we need a temp under 'next'
		//we can't put 'next' on the stack
		for (int i=0;i<defs.length;i++) {
		    if (!stacktemps.containsKey(defs[i]))
			flag=false;
		    else if (!((TempInfo)stacktemps.get(defs[i])).stack)
			flag=false;
		    if ((flag==false)&&(uses[i]==next)) {
			broken=true;
			break;
		    }
		}

		if (broken) {
		    // If this stack allocation failed,
		    // allocate it to a local variable.
		    stacktemps.put(next,new TempInfo(j++));
		    
		    //gross hack...cycle through all defs
		    for (int k=0; k<defs.length;k++) {
			HClass hclass=tp.typeMap(hdefs[k],next);
			if ((hclass==HClass.Double)||(hclass==HClass.Long)) {
			    j++;
			    break;
			}
		    }
		}
	    }
	}

	//loop through the rest of the temps
	//assign them to local variables...
	for (int i=0;i<alltemp.length;i++) {
	    if (!stacktemps.containsKey(alltemp[i])) {
		// Should be ok to pass null as HCodeElement param

		stacktemps.put(alltemp[i],new TempInfo(j++));

		HCodeElement[] defs=ud.defMap(code,alltemp[i]);
		//gross hack...cycle through all defs
		for (int k=0; k<defs.length;k++) {
		    HClass hclass=tp.typeMap(defs[k],alltemp[i]);
		    if ((hclass==HClass.Double)||(hclass==HClass.Long)) {
			j++;
			break;
		    }
		}
	    }
	}
	return new Object[] {stacktemps, new Integer(j)};
    }

    void checkPair(Map stacktemps, final Quad use, final Quad def, Temp t, Set badquads) {
	Quad ptr=def;
	boolean flag=true;
	WorkSet track=new WorkSet();
	//make sure that the definition quad
	//isn't covered by a handler and that it only defines one temp
	if ((ptr.def().length==1)&&(!badquads.contains(ptr))) {
	    //while we haven't found the use
	    while (ptr!=use) {
		//make sure we aren't at a sigma quad
		//don't want to stack allocate across them
		if (ptr.next().length!=1) {
		    flag=false;break;}
		
		//Look at the temp's defined by this quad
		Temp[] defs=ptr.def();
		//if any of them are on the stack
		//add them to our stack tracking list
		for (int i=0;i<defs.length;i++)
		    if (stacktemps.containsKey(defs[i]))
			track.push(defs[i]);

		//go to the next quad
       		ptr=ptr.next(0);

		//make sure that this isn't a phi...
		//don't want the possibility of the analysis
		//following a loop...
		//also make sure that this quad isn't covered by a handler
		if ((ptr.prev().length!=1)||(badquads.contains(ptr))) {
		    flag=false;break;}
		//check the uses of this quad....
		//if it is our temp, we are done...
		//if it is another temp, check to see
		//if it is stack allocated...
		//if it is in our track list remove it, if it is
		//not bail on this temp
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
	    //if we made it here, and our tracking list is empty
	    //we add it to our list of possibilities
	    if ((flag)&&(track.isEmpty())) {
		stacktemps.put(t, new TempInfo(true));
	    } 
	}
    }

    private int finddepth(Map map, TypeMap tp, HMethod hm, HCode hc) {
	//Output assembly from the quads
	//Build mapping from temps to localvars/stack

	WorkSet done=new WorkSet();
	DepthVisitor visitor=new DepthVisitor(map,hc,tp);
	Quad start=(Quad)hc.getRootElement();
	METHOD m=(METHOD)start.next(1);
	//Loop through the handlers
	//We do this to label the quad ranges protected by the handler
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements()) {
		Quad q=(Quad)e.nextElement();
	    }
	}
	//Visit all the quads
	visitAllDepth(visitor,start.next(1), done);
	return visitor.max();
    }

    private static final void visitAllDepth(DepthVisitor visitor, Quad start, Set qm) {
	//Visit the node passed to us
	start.accept(visitor);
	//add this node to the done list
	qm.add(start);
	//visit the kids
        Quad[] ql = start.next();
        for (int i=0; i<ql.length; i++) {
            if (qm.contains(ql[i])) continue; // skip if already done.
            visitAllDepth(visitor, ql[i],qm);
        }
    }

    class DepthVisitor extends QuadVisitor {
	Map tempmap;
	HCode hc;
	TypeMap tm;
	UseDef ud;
	WorkSet skip;
	CJMPVisitor cjmp;
	HashMap depth;
	int max;

	DepthVisitor(Map tempmap, HCode hc, TypeMap tm) {
	    this.max=0;
	    this.tempmap=tempmap;
	    this.hc=hc;
	    this.tm=tm;
	    this.skip=new WorkSet();
	    this.ud=new UseDef();
	    this.cjmp=new CJMPVisitor();
	    depth=new HashMap();
	}

	public void visit(Quad q) {
	}

	public void visit(AGET q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.objectref());
	    depth1+=load(q,q.index());
	    maxcheck(depth1);
	    depth1-=load2(q, q.objectref());
	    depth1+=load2(q,q.dst());
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}

	int max() {
	    return max;
	}

	void maxcheck(int t) {
	    if (t<0)
		System.out.println("ERROR: Negative Stack Depth Calculated in Jasmin"+t);
	    if (t>max) max=t;
	}

	public void visit(ALENGTH q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.objectref());
	    maxcheck(depth1);
	    depth1-=load2(q,q.objectref());
	    depth1+=1;
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer (depth1));
	}

	public void visit(ANEW q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    for (int i=0;i<q.dimsLength();i++)
		depth1+=load(q,q.dims(i));
	    maxcheck(depth1);
	    depth1+=1;
	    for (int i=0;i<q.dimsLength();i++)
		depth1-=load2(q,q.dims(i));
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer (depth1));
	}

	public void visit(ARRAYINIT q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    if (q.value().length>0)
		depth1+=load(q,q.objectref());
	    if ((q.type()==HClass.Boolean)||
		(q.type()==HClass.Int)||
		(q.type()==HClass.Byte)||
		(q.type()==HClass.Char)||
		(q.type()==HClass.Short)||		
		(q.type()==HClass.Float)||
		(q.type()==HClass.Boolean)) {
		    maxcheck(depth1+3);
		}
	    else if ((q.type()==HClass.Long)||
		     (q.type()==HClass.Double)) {
		maxcheck(depth1+1+1+2);
	    }
	    depth1-=load2(q,q.objectref());
	    depth.put(q, new Integer(depth1));
	}
       
	public void visit(ASET q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
   	    depth1+=load(q,q.objectref());
	    depth1+=load(q,q.index());
	    depth1+=load(q,q.src());
	    maxcheck(depth1);
	    depth1-=load2(q,q.objectref());
	    depth1-=load2(q,q.index());
	    depth1-=load2(q,q.src());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(COMPONENTOF q) {
	    //kludge
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    maxcheck(depth1);
	    depth1+=load(q,q.objectref());
	    depth1+=load(q,q.arrayref());
	    depth1-=load2(q,q.objectref());
	    depth1-=load2(q,q.arrayref());
	    depth1+=1;
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(METHOD q) {
	    depth.put(q, new Integer(0));
	}

	public void visit(HEADER q) {
	    depth.put(q, new Integer(0));
	}

	public void visit(PHI q) {
	    int depth1=0;
	    for (int i=0;i<q.prevLength();i++) {
		if (depth.containsKey(q.prev(i))) {
		    depth1=((Integer) (depth.get(q.prev(i)))).intValue();
		    break;
		}
	    }
	    maxcheck(depth1);
	    depth.put(q, new Integer(depth1));
	}

	public void visit(FOOTER q) {
	    int depth1=0;
	    for (int i=0;i<q.prevLength();i++) {
		if (depth.containsKey(q.prev(i))) {
		    depth1=((Integer) (depth.get(q.prev(i)))).intValue();
		    break;
		}
	    }
	    maxcheck(depth1);
	    depth.put(q, new Integer(depth1));
	}

	public void visit(HANDLER q) {
	    maxcheck(1);
	    depth.put(q, new Integer(1));
	}

	public void visit(INSTANCEOF q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.src());
	    maxcheck(depth1);
	    depth1-=load2(q, q.src());
	    depth1+=1;
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}
	
	public void visit(CALL q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();

	    if (q.isVirtual()&&!q.isInterfaceMethod()) {
		//virtual method
		for(int i=0;i<q.params().length;i++) {
		    depth1+=load(q,q.params(i)); 
		}
		maxcheck(depth1);
		for(int i=0;i<q.params().length;i++) {
		    depth1-=load2(q,q.params(i)); 
		}
		if (q.retval()!=null)
		    depth1+=load2(q,q.retval());
		maxcheck(depth1);
		if (q.retval()!=null)
		    depth1+=store(q,q.retval());
		depth.put(q, new Integer(depth1));
	    } 
	    else {
		if (q.isInterfaceMethod()) {
		    for(int i=0;i<q.params().length;i++) {
			depth1+=load(q,q.params(i)); 
		    }
		    maxcheck(depth1);
		    for(int i=0;i<q.params().length;i++) {
			depth1-=load2(q,q.params(i)); 
		    }
		    if (q.retval()!=null)
			depth1+=load2(q,q.retval());
		    maxcheck(depth1);
		    if (q.retval()!=null)
			depth1+=store(q,q.retval());
		    depth.put(q, new Integer(depth1));
		}
		else
		    if(q.isStatic()) {
			//static method
			for(int i=0;i<q.params().length;i++) {
			    depth1+=load(q,q.params(i)); 
			}
			maxcheck(depth1);
			for(int i=0;i<q.params().length;i++) {
			    depth1-=load2(q,q.params(i)); 
			}
			if (q.retval()!=null)
			    depth1+=load2(q,q.retval());
			maxcheck(depth1);
			if (q.retval()!=null)
			    depth1+=store(q,q.retval());
			depth.put(q, new Integer(depth1));
		    }
		    else {
					//non-virtual method
			for(int i=0;i<q.params().length;i++) {
			    depth1+=load(q,q.params(i)); 
			}
			maxcheck(depth1);
			for(int i=0;i<q.params().length;i++) {
			    depth1-=load2(q,q.params(i)); 
			}
			if (q.retval()!=null)
			    depth1+=load2(q,q.retval());
			maxcheck(depth1);
			if (q.retval()!=null)
			    depth1+=store(q,q.retval());
			depth.put(q, new Integer(depth1));
		    }
	    }
	}

	public void visit(GET q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    if (q.objectref()==null) {
		//Static
		depth1+=load2(q, q.dst());
		maxcheck(depth1);
	    }
	    else {
		depth1+=load(q,q.objectref());
		maxcheck(depth1);
		depth1-=load2(q,q.objectref());
		depth1+=load2(q, q.dst());
		maxcheck(depth1);
	    }
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(CJMP q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    if (!skip.contains(q)) {
		depth1+=load(q,q.test());
		maxcheck(depth1);
		depth1-=load2(q,q.test());
	    }
	    depth.put(q, new Integer(depth1));
	}

	public void visit(THROW q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.throwable());
	    maxcheck(depth1);
	    depth1-=load2(q, q.throwable());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(MONITORENTER q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.lock());
	    maxcheck(depth1);
	    depth1-=load2(q,q.lock());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(MONITOREXIT q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.lock());
	    maxcheck(depth1);
	    depth1-=load2(q,q.lock());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(MOVE q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.src());
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(NEW q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load2(q,q.dst());
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(LABEL q) {
	    int depth1=0;
	    for (int i=0;i<q.prevLength();i++) {
		if (depth.containsKey(q.prev(i))) {
		    depth1=((Integer) (depth.get(q.prev(i)))).intValue();
		    break;
		}
	    }
	    maxcheck(depth1);
	    depth.put(q, new Integer(depth1));
	}

	public void visit(NOP q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    maxcheck(depth1);
	    depth.put(q, new Integer(depth1));
	}

	public void visit(CONST q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load2(q,q.dst());
	    maxcheck(depth1);
	    depth1+=store(q,q.dst());
	    depth.put(q, new Integer(depth1));
	}
	
	public void visit(RETURN q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    if (q.retval()!=null)
		depth1+=load(q,q.retval());
	    maxcheck(depth1);
	    //doesn't matter...end of method
	    depth.put(q, new Integer(depth1));
	}

	public void visit(SET q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    if (q.objectref()==null) {
		//Static
		depth1+=load(q,q.src());
		maxcheck(depth1);
		depth1-=load2(q,q.src());
	    }
	    else {
		load(q,q.objectref());
		load(q,q.src());
		depth1+=load(q,q.objectref());
		depth1+=load(q,q.src());
		maxcheck(depth1);
		depth1-=load2(q,q.objectref());
		depth1-=load2(q,q.src());
	    }
	    depth.put(q, new Integer(depth1));
	}

	public void visit(SWITCH q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.index());
	    maxcheck(depth1);
	    depth1-=load2(q,q.index());
	    depth.put(q, new Integer(depth1));
	}

	public void visit(TYPECAST q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();
	    depth1+=load(q,q.objectref());
	    maxcheck(depth1);
	    depth1+=store(q,q.objectref());
	    depth.put(q, new Integer(depth1));
	}

	//This method handles operands
	//really 3 cases:
	//normal, compares that are done with compares,
	//compares done with conditional branches

	public void visit(OPER q) {
	    int depth1=((Integer) (depth.get(q.prev(0)))).intValue();

	    switch (q.opcode()) {
	    case Qop.LCMPEQ:
		
		for (int i=0;i<q.operandsLength();i++)
		    depth1+=load(q,q.operands(i));
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++)
		    depth1-=load2(q,q.operands(i));
		depth1+=1;
		maxcheck(depth1);

		q.next(0).accept(cjmp);
		boolean testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			depth1-=1;
		    } else
			testcjmp=false;
		} 
		if (!testcjmp) {
		    depth1+=store(q,q.dst());
		}
		depth.put(q, new Integer(depth1));
		break;

	    case Qop.LCMPGT:
		for (int i=0;i<q.operandsLength();i++)
		    depth1+=load(q,q.operands(i));
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++)
		    depth1-=load2(q,q.operands(i));
		depth1+=1;
		maxcheck(depth1);

	        q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			depth1-=1;
		    } else
			testcjmp=false;
		} 
		if (!testcjmp) {
		    depth1+=store(q,q.dst());
		}
		depth.put(q, new Integer(depth1));
		break;
	    case Qop.DCMPEQ:
	    case Qop.DCMPGE:
	    case Qop.DCMPGT:
		for (int i=0;i<q.operandsLength();i++)
		    depth1+=load(q,q.operands(i));
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++)
		    depth1-=load2(q,q.operands(i));
		depth1+=1;
		maxcheck(depth1);


		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			depth1-=1;
		    } else
			testcjmp=false;
		}
		if (!testcjmp) {
		    depth1+=store(q,q.dst());
		}
		depth.put(q, new Integer(depth1));
		break;

	    case Qop.FCMPEQ:
	    case Qop.FCMPGE:
	    case Qop.FCMPGT:
		for (int i=0;i<q.operandsLength();i++)
		    depth1+=load(q,q.operands(i));
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++)
		    depth1-=load2(q,q.operands(i));
		depth1+=1;
		maxcheck(depth1);
		
		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
			depth1-=1;
		    } else
			testcjmp=false;
		}
		if (!testcjmp) {
		    depth1+=store(q,q.dst());
		}
		depth.put(q, new Integer(depth1));
		break;

	    case Qop.ACMPEQ:
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		for (int i=0;i<q.operandsLength();i++) {
		    depth1+=load(q,q.operands(i));
		}
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++) {
		    depth1-=load2(q,q.operands(i));
		}
		q.next(0).accept(cjmp);
		testcjmp=cjmp.cjmp();
		if (testcjmp) {
		    CJMP qnext=(CJMP)q.next(0);
		    
		    if ((qnext.test()==q.dst())&&
			(ud.useMap(hc, q.dst()).length==1)) {
			skip.add(qnext);
		    } else
			testcjmp=false;
		}
		if (!testcjmp) {
		    depth1+=store(q,q.dst());
		}
		depth.put(q, new Integer(depth1));
		break;
	    case Qop.D2F:
	    case Qop.D2I:
	    case Qop.D2L:
	    case Qop.DADD:
	    case Qop.DDIV:
	    case Qop.DMUL:
	    case Qop.DNEG:
	    case Qop.DREM:
	    case Qop.F2D:
	    case Qop.F2I:
	    case Qop.F2L:
	    case Qop.FADD:
	    case Qop.FDIV:
	    case Qop.FMUL:
	    case Qop.FNEG:
	    case Qop.FREM:
	    case Qop.I2B:
	    case Qop.I2C:
	    case Qop.I2D:
	    case Qop.I2F:
	    case Qop.I2L:
	    case Qop.I2S:
	    case Qop.IADD:
	    case Qop.IAND:
	    case Qop.IDIV:
	    case Qop.IMUL:
	    case Qop.INEG:
	    case Qop.IOR:
	    case Qop.IREM:
	    case Qop.ISHL:
	    case Qop.ISHR:
	    case Qop.IUSHR:
	    case Qop.IXOR:
	    case Qop.L2D:
	    case Qop.L2I:
	    case Qop.L2F:
	    case Qop.LADD:
	    case Qop.LAND:
	    case Qop.LDIV:
	    case Qop.LMUL:
	    case Qop.LNEG:
	    case Qop.LOR:
	    case Qop.LREM:
	    case Qop.LSHL:
	    case Qop.LSHR:
	    case Qop.LUSHR:
	    case Qop.LXOR:
		for (int i=0;i<q.operandsLength();i++) {
		    depth1+=load(q,q.operands(i));
		}
		maxcheck(depth1);
		for (int i=0;i<q.operandsLength();i++) {
		    depth1-=load2(q,q.operands(i));
		}
		depth1+=load2(q,q.dst());
		depth1+=store(q,q.dst());
		depth.put(q, new Integer(depth1));
		break;
	    default:
	    }
	}

	private int store(Quad q,Temp t) {
	    // Should be ok to pass null for HCodeElement param
	    HClass tp=tm.typeMap(q,t); 
	    int size;
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		size=-1;
	    else
		if (tp==HClass.Double)
		    size=-2;
		else
		    if (tp==HClass.Float)
			size=-1;
		    else
			if (tp==HClass.Long)
			    size=-2;
			else
			    size=-1;
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		return size;
	    else
		return 0;
	}

	private int load2(Quad q,Temp t) {
	    // Should be ok to pass null for HCodeElement param
	    int size=0;
	    HClass tp=tm.typeMap(q,t);
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		size=1;
	    else
		if (tp==HClass.Double)
		    size=2;
		else
		    if (tp==HClass.Float)
			size=1;
		    else
			if (tp==HClass.Long)
			    size=2;
			else
			    size=1;
	    return size;
	}
	private int load(Quad q,Temp t) {
	    // Should be ok to pass null for HCodeElement param
	    int size=0;
	    HClass tp=tm.typeMap(q,t);
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		size=1;
	    else
		if (tp==HClass.Double)
		    size=2;
		else
		    if (tp==HClass.Float)
			size=1;
		    else
			if (tp==HClass.Long)
			    size=2;
			else
			    size=1;
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		return size;
	    else
		return 0;
	}
    }
    String escapeFloat(Float f) {
	//	if (f.isNaN()||f.isInfinite()) {
	    int i=Float.floatToIntBits(f.floatValue());
	    String s=Integer.toHexString(i);
	    while (s.length()!=8) 
		s=new String("0"+s);
	    return new String("0\\"+s);
	    //}
	    //else return f.toString();
    }

    String escapeDouble(Double f) {
	//if (f.isNaN()||f.isInfinite()) {
	    long i=Double.doubleToLongBits(f.doubleValue());
	    String s=Long.toHexString(i);
	    while (s.length()!=16) 
		s=new String("0"+s);
	    return new String("1\\"+s);
	    //}
	    //else return f.toString();
    }
}



