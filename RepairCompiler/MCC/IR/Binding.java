package MCC.IR;

class Binding {
    VarDescriptor var;
    int position;
    boolean search;
    public Binding(VarDescriptor vd,int pos) {
	var=vd;
	position=pos;
	search=false;
    }

    public Binding(VarDescriptor vd) {
	var=vd;
	search=true;
    }
    int getPosition() {
	return position;
    }
    VarDescriptor getVar() {
	return var;
    }
    public String toString() {
	if (search)
	    return "SEARCHFOR"+var.toString();
	else
	    return var.toString()+"="+String.valueOf(position);
    }
}
