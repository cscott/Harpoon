import java.util.*;

class RoleUniverse {
    boolean classmatters;
    HashSet singleroleclass;

    boolean restrictarrayreferenceto;
    HashSet allowedarrayto;

    boolean restrictfieldreferenceto;
    HashSet allowedfieldto;

    boolean restrictarrayreferencefrom;
    HashSet allowedarrayfrom;

    boolean restrictfieldreferencefrom;
    HashSet allowedfieldfrom;

    boolean restrictidentity;
    HashSet allowedidentityfields;

    boolean restrictinvokedmethods;
    HashSet allowedinvokedmethods;
    
    boolean restrictdominators;
    HashSet alloweddominators;

    HashMap cache;

    public RoleUniverse() {
	classmatters=true;
	singleroleclass=new HashSet();
	restrictarrayreferenceto=false;
	restrictfieldreferenceto=false;
	restrictarrayreferencefrom=false;
	restrictfieldreferencefrom=false;
	restrictidentity=false;
	restrictinvokedmethods=false;
	restrictdominators=false;
	cache=new HashMap();
    }

    public RoleUniverse(boolean classmatters,boolean restrictarrayreferenceto,
			Reference[] allowedarrayto, boolean restrictfieldreferenceto, 
			Reference[] allowedfieldto, boolean restrictarrayreferencefrom,
			Reference[] allowedarrayfrom, boolean restrictfieldreferencefrom,
			Reference[] allowedfieldfrom, boolean restrictidentity,
			String[] allowedidentityfields, boolean restrictinvokedmethods,
			String[] allowedinvokedmethods, boolean restrictdominators,
			Dominator[] alloweddominators
			) {
	this.classmatters=classmatters;
	this.singleroleclass=new HashSet();

	this.restrictarrayreferenceto=restrictarrayreferenceto;
	if (restrictarrayreferenceto) {
	    this.allowedarrayto=new HashSet();
	    for (int i=0;i<allowedarrayto.length;i++) {
		this.allowedarrayto.add(allowedarrayto[i]);
	    }
	}

	this.restrictfieldreferenceto=restrictfieldreferenceto;
	if (restrictfieldreferenceto) {
	    this.allowedfieldto=new HashSet();
	    for (int i=0;i<allowedfieldto.length;i++) {
		this.allowedfieldto.add(allowedfieldto[i]);
	    }
	}

	this.restrictarrayreferencefrom=restrictarrayreferencefrom;
	if (restrictarrayreferencefrom) {
	    this.allowedarrayfrom=new HashSet();
	    for (int i=0;i<allowedarrayfrom.length;i++) {
		this.allowedarrayfrom.add(allowedarrayfrom[i]);
	    }
	}

	this.restrictfieldreferencefrom=restrictfieldreferencefrom;
	if (restrictfieldreferencefrom) {
	    this.allowedfieldfrom=new HashSet();
	    for (int i=0;i<allowedfieldfrom.length;i++) {
		this.allowedfieldfrom.add(allowedfieldfrom[i]);
	    }
	}

	this.restrictidentity=restrictidentity;
	if (restrictidentity) {
	    this.allowedidentityfields=new HashSet();
	    for (int i=0;i<allowedidentityfields.length;i++) {
		this.allowedidentityfields.add(allowedidentityfields[i]);
	    }
	}

	this.restrictinvokedmethods=restrictinvokedmethods;
	if (restrictinvokedmethods) {
	    this.allowedinvokedmethods=new HashSet();
	    for (int i=0;i<allowedinvokedmethods.length;i++) {
		this.allowedinvokedmethods.add(allowedinvokedmethods[i]);
	    }
	}

	this.restrictdominators=restrictdominators;
	if (restrictdominators) {
	    this.alloweddominators=new HashSet();
	    for (int i=0;i<alloweddominators.length;i++) {
		this.alloweddominators.add(alloweddominators[i]);
	    }
	}
	cache=new HashMap();
    }

    public void setclass(boolean classmatters) {
	this.classmatters=classmatters;
    }
}
