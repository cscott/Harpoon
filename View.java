class View {
    RoleUniverse [] universes;
    RoleI rolei;
    RoleUniverse []display;
    RoleDisplay roled;

    public View(RoleI rolei,RoleUniverse[] universes, RoleUniverse[] display) {
	this.rolei=rolei;
	this.universes=new RoleUniverse[universes.length];
	for(int i=0;i<universes.length;i++)
	    this.universes[i]=universes[i];
	this.display=new RoleUniverse[display.length];
	for(int i=0;i<display.length;i++)
	    this.display[i]=display[i];
    }

    public RoleCombination project(Role alluniverses) {
	int [] roles=new int[universes.length];
	for(int i=0;i<universes.length;i++) {
	    roles[i]=alluniverses.project(universes[i]).rolenumber;
	}
	RoleCombination rc=new RoleCombination(rolei, roles);

	if (roled!=null) {
	    int [] droles=new int[display.length];
	    for (int i=0;i<display.length;i++) {
		/*log */
		droles[i]=alluniverses.project(display[i]).rolenumber;
	    }
	    roled.register(rc,droles);
	}
	return rc;
    }

    public void attach(RoleDisplay roled) {
	this.roled=roled;
    }

    public void detach() {
	this.roled=null;
    }
}

