class Transition {
    int rolefrom, roleto;
    int type;
    String[] names;
    public Transition(int rolefrom, int roleto,int type, String[] names) {
	this.rolefrom=rolefrom;
	this.roleto=roleto;
	this.type=type;
	this.names=names;
    }
    static public String htmlreadable(String input) {
	int start=0;
	String retval="";
	while(true) {
	    int i1=input.indexOf("<",start);
	    int i2=input.indexOf(">",start);
	    int i=(i1!=-1&&(i1<i2||i2==-1))?i1:i2;
	    if (i==-1)
		return retval+input.substring(start);
	    retval=retval+input.substring(start,i);
	    if (i==i1)
		retval+="&lt ";
	    else
		retval+="&gt";
	    start=i+1;
	}
    }

    static public String filtername(String input) {
	int start=0;
	String retval="";
	while(true) {
	    int i=input.indexOf("\\n",start);
	    if (i==-1) 
		return retval+input.substring(start);
	    retval=retval+input.substring(start,i);
	    start=i+2;
	}
    }
    static public String classname(String input) {
	int dot=input.indexOf('.');
	return input.substring(0,dot);
    }
    static public String methodname(String input) {
	int dot=input.indexOf('.');
	int para=input.indexOf('(',dot);
	return filtername(input.substring(dot+1,para));
    }
    static public String signature(String input) {
	int dot=input.indexOf('.');
	int firstpara=input.indexOf('(');
	int secondpara=input.indexOf('(',firstpara+1);
	if (secondpara==-1)
	    return input.substring(firstpara);
	else
	    return input.substring(firstpara, secondpara);
    }
}
