package MCC.IR;

class Binding {
    VarDescriptor var;
    SetDescriptor sd;
    int position;
    int type;
    public static final int POSITION=1;
    public static final int CREATE=2;
    public static final int SEARCH=3;

    public Binding(VarDescriptor vd,int pos) {
	var=vd;
	position=pos;
	type=POSITION;
    }

    public Binding(VarDescriptor vd, SetDescriptor sd,boolean search) {
	this.var=vd;
	this.sd=sd;
	if (search)
	    type=SEARCH;
	else
	    type=CREATE;
    }

    public int getType() {
	return type;
    }

    int getPosition() {
	assert type==POSITION;
	return position;
    }

    SetDescriptor getSet() {
	assert type==CREATE||type==SEARCH;
	return sd;
    }

    VarDescriptor getVar() {
	return var;
    }

    public String toString() {
	switch(type) {
	case POSITION:
	    return var.toString()+"="+String.valueOf(position);
	case CREATE:
	    return var.toString()+"=CREATE("+sd.toString()+")";
	case SEARCH:
	    return var.toString()+"=SEARCH("+sd.toString()+")";
	default:
	    return "UNRECOGNIZED Binding type";
	}
    }
}
