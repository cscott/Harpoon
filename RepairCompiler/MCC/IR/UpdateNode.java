package MCC.IR;
import java.util.*;

class UpdateNode {
    Vector updates;
    Vector bindings;

    public UpdateNode() {
	updates=new Vector();
	bindings=new Vector();
    }

    public void addBinding(Binding b) {
	bindings.add(b);
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
