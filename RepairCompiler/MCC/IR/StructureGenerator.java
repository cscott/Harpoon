package MCC.IR;

import java.io.*;
import java.util.*;
import MCC.State;

public class StructureGenerator {
    State state;
    CodeWriter cr;
    CodeWriter crhead;
    TypeDescriptor[] tdarray;
    RepairGenerator rg;
    StructureGenerator(State state, RepairGenerator rg) {
	this.state=state;
	this.rg=rg;
	try {
	    cr=new StandardCodeWriter(new java.io.PrintWriter(new FileOutputStream("size.cc"),true));
	    crhead=new StandardCodeWriter(new java.io.PrintWriter(new FileOutputStream("size.h"),true));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    void buildall() {
	int max=TypeDescriptor.counter;
	tdarray=new TypeDescriptor[max];
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    tdarray[ttd.getId()]=ttd;
	}
	cr.outputline("#include \"size.h\"");
	generategetfield();
	generategetnumfields();
	generateisArray();
	generateisPtr();
	generateissubtype();
	generatecalls();
	generatecomputesize();
	generateheader();
    }

    private void generatecalls() {
	int max=TypeDescriptor.counter;
	cr.outputline("int arsize["+max+"];");

	for(int i=0;i<max;i++) {
	    TypeDescriptor ttd=tdarray[i];
	    if (ttd instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) ttd;
		String str="int arnumelement"+std.getId()+"["+std.fieldlist.size()+"]={";
		for(int j=0;j<std.fieldlist.size();j++) {
		    str+=0;
		    if (((j+1)!=std.fieldlist.size()))
			str+=",";
		}
		str+="};";
		cr.outputline(str);
	    } else
		cr.outputline("int arnumelement"+ttd.getId()+"[0];");
	}
	String str="int* arnumelements["+String.valueOf(max)+"]={";
	for(int i=0;i<max;i++) {
	    str+="arnumelement"+i;
	    if (((i+1)!=max))
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	

	cr.outputline("int typeobject::size(int type) {");
	cr.outputline("return arsize[type];");
	cr.outputline("}");

	cr.outputline("int typeobject::numElements(int type, int fieldindex) {");
	cr.outputline("return arnumelements[type][fieldindex];");
	cr.outputline("}");
	cr.outputline("typeobject::typeobject() {}");
    }

    private void generatecomputesize() {
	int max=TypeDescriptor.counter;
	cr.outputline("void typeobject::computesizes("+rg.name+"_state * obj) {");
	cr.outputline("obj->computesizes(arsize,arnumelements);");
	cr.outputline("}");
    }

    private void generateheader() {
	crhead.outputline("#include \""+rg.headername + "\"");
	crhead.outputline("class typeobject {");
	crhead.outputline("public:");
	crhead.outputline("typeobject();");
	crhead.outputline("int getfield(int type, int fieldindex);");
	crhead.outputline("int isArray(int type, int fieldindex);");
	crhead.outputline("int isPtr(int type, int fieldindex);");
	crhead.outputline("int numElements(int type, int fieldindex);");
	crhead.outputline("int size(int type);");
	crhead.outputline("int getnumfields(int type);");
	crhead.outputline("bool issubtype(int subtype, int type);");
	crhead.outputline("void computesizes("+rg.name+"_state *);");
	crhead.outputline("};");
    }

    
    private void generategetfield() {
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    String str="";

	    if (ttd instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) ttd;
		str="int argetfield"+std.getId()+"["+std.fieldlist.size()+"]={";
		for(int i=0;i<std.fieldlist.size();i++) {
		    FieldDescriptor fd = (FieldDescriptor)std.fieldlist.elementAt(i);
		    TypeDescriptor td = fd.getType();
		    str+=String.valueOf(td.getId());
		    if ((i+1)!=std.fieldlist.size())
			str+=",";
		}
		str+="};";
		cr.outputline(str);
	    } else
		cr.outputline("int argetfield"+ttd.getId()+"[0];");
	}
	int max=TypeDescriptor.counter;
	String str="int* argetfield["+String.valueOf(max)+"]={";
	for(int i=0;i<max;i++) {
	    str+="argetfield"+i;
	    if (((i+1)!=max))
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	
	cr.outputline("int typeobject::getfield(int type, int fieldindex) {");
	cr.outputline("return argetfield[type][fieldindex];");
	cr.outputline("}");
    }

   private void generategetnumfields() {
	int max=TypeDescriptor.counter;
	String str="int argetnumfield["+String.valueOf(max)+"]={";
	for(int i=0;i<max;i++) {
	    TypeDescriptor ttd=tdarray[i];
	    if (ttd instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) ttd;
		str+=String.valueOf(std.fieldlist.size());
	    } else
		str+="0";
	    if (((i+1)!=max))
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	
	cr.outputline("int typeobject::getnumfields(int type) {");
	cr.outputline("return argetnumfield[type];");
	cr.outputline("}");
    }
    private void generateisArray() {
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    String str="";

	    if (ttd instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) ttd;
		str="int arisArray"+std.getId()+"["+std.fieldlist.size()+"]={";
		for(int i=0;i<std.fieldlist.size();i++) {
		    FieldDescriptor fd = (FieldDescriptor)std.fieldlist.elementAt(i);
		    TypeDescriptor td = fd.getType();
		    if (fd instanceof ArrayDescriptor)
			str+="1";
		    else
			str+="0";
		    if ((i+1)!=std.fieldlist.size())
			str+=",";
		}
		str+="};";
		cr.outputline(str);
	    } else
		cr.outputline("int arisArray"+ttd.getId()+"[0];");
	}
	int max=TypeDescriptor.counter;
	String str="int* arisArray["+String.valueOf(max)+"]={";
	for(int i=0;i<max;i++) {
	    str+="arisArray"+i;
	    if (((i+1)!=max))
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	
	cr.outputline("int typeobject::isArray(int type, int fieldindex) {");
	cr.outputline("return arisArray[type][fieldindex];");
	cr.outputline("}");
    }
    private void generateisPtr() {
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    String str="";

	    if (ttd instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) ttd;
		str="int arisPtr"+std.getId()+"["+std.fieldlist.size()+"]={";
		for(int i=0;i<std.fieldlist.size();i++) {
		    FieldDescriptor fd = (FieldDescriptor)std.fieldlist.elementAt(i);
		    if (fd.getPtr())
			str+="1";
		    else
			str+="0";
		    if ((i+1)!=std.fieldlist.size())
			str+=",";
		}
		str+="};";
		cr.outputline(str);
	    } else
		cr.outputline("int arisPtr"+ttd.getId()+"[0];");
	}
	int max=TypeDescriptor.counter;
	String str="int* arisPtr["+String.valueOf(max)+"]={";
	for(int i=0;i<max;i++) {
	    str+="arisPtr"+i;
	    if (((i+1)!=max))
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	
	cr.outputline("int typeobject::isPtr(int type, int fieldindex) {");
	cr.outputline("return arisPtr[type][fieldindex];");
	cr.outputline("}");
    }

    void generateissubtype() {
	int max=TypeDescriptor.counter;
	String str="bool arissubtype["+max+"]["+max+"]={";
	for(int i=0;i<max;i++) {
	    str+="{";
	    for(int j=0;j<max;j++) {
		TypeDescriptor tdi=tdarray[i];
		TypeDescriptor tdj=tdarray[j];
		if (tdi.isSubtypeOf(tdj))
		    str+="1";
		else
		    str+="0";
		if ((j+1)!=max)
		    str+=",";
	    }
	    str+="}";
	    if ((i+1)!=max)
		str+=",";
	}
	str+="};";
	cr.outputline(str);
	cr.outputline("bool typeobject::issubtype(int subtype, int type) {");
	cr.outputline("return arissubtype[subtype][type];");
	cr.outputline("}");
    }
}
