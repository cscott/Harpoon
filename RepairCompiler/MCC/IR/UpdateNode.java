package MCC.IR;
import java.util.*;

class UpdateNode {
    Vector updates;
    Vector bindings;
    Hashtable binding;

    public UpdateNode() {
	updates=new Vector();
	bindings=new Vector();
	binding=new Hashtable();
    }

    public void addBindings(Vector v) {
	for (int i=0;i<v.size();i++) {
	    bindings.add((Binding)v.get(i));
	}
    }

    public boolean checkconflicts() {
	Set toremove=new HashSet();
	for(int i=0;i<updates.size();i++) {
	    Updates u1=(Updates)updates.get(i);
	    for(int j=0;j<updates.size();j++) {
		Updates u2=(Updates)updates.get(j);
		if (u1.isAbstract()||u2.isAbstract())
		    continue;  /* Abstract updates are already accounted for by graph */
		if (u1.getDescriptor()!=u2.getDescriptor())
		    continue; /* No interference - different descriptors */
		
		if ((u1.getOpcode()==Opcode.GT||u1.getOpcode()==Opcode.GE)&&
		    (u2.getOpcode()==Opcode.GT||u2.getOpcode()==Opcode.GE))
		    continue; /* Can be satisfied simultaneously */

		if ((u1.getOpcode()==Opcode.LT||u1.getOpcode()==Opcode.LE)&&
		    (u2.getOpcode()==Opcode.LT||u2.getOpcode()==Opcode.LE))
		    continue;
		
		
		return false; /* They interfere */
	    }
	}
	updates.remove(toremove);
	return false;
    }

    public void addBinding(Binding b) {
	bindings.add(b);
	binding.put(b.getVar(),b);
    }

    public Binding getBinding(VarDescriptor vd) {
	if (binding.containsKey(vd))
	    return (Binding)binding.get(vd);
	else
	    return null;
    }

    public void addUpdate(Updates u) {
	updates.add(u);
    }

    public int numUpdates() {
	return updates.size();
    }
    public Updates getUpdate(int i) {
	return (Updates)updates.get(i);
    }
}
