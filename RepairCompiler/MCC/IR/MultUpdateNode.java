package MCC.IR;
import java.util.*;

class MultUpdateNode {
    Vector updates;
    AbstractRepair abstractrepair;
    public MultUpdateNode(AbstractRepair ar) {
	updates=new Vector();
	abstractrepair=ar;
    }
    void addUpdate(UpdateNode un) {
	updates.add(un);
    }
    int numUpdates() {
	return updates.size();
    }
    AbstractRepair getRepair() {
	return abstractrepair;
    }
    UpdateNode getUpdate(int i) {
	return (UpdateNode)updates.get(i);
    }
}
