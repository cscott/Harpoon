package MCC.IR;

class Binding {
    VarDescriptor var;
    int position;

    public Binding(VarDescriptor vd,int pos) {
	var=vd;
	position=pos;
    }

    int getPosition() {
	return position;
    }

    VarDescriptor getVar() {
	return var;
    }

}
