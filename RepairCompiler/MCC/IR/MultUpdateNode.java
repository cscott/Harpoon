package MCC.IR;
import java.util.*;

class MultUpdateNode {
    Vector updates;
    AbstractRepair abstractrepair;
    ScopeNode scopenode;
    int op;
    static public final int ADD=0;
    static public final int REMOVE=1;
    static public final int MODIFY=2;

    public MultUpdateNode(AbstractRepair ar, int op) {
	updates=new Vector();
	abstractrepair=ar;
	this.op=op;
    }

    public MultUpdateNode(ScopeNode sn) {
	updates=new Vector();
	scopenode=sn;
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
