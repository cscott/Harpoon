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
