package MCC.IR;

/**
 * Descriptor 
 *
 * represents a symbol in the language (var name, function name, etc).
 */

public abstract class Descriptor {

    protected String name;
    protected String safename;
    
    public Descriptor(String name) {
	this.name = name;
        this.safename = "__" + name + "__";
    }

    protected Descriptor(String name, String safename) {
	this.name = name;
        this.safename = safename;
    }
    
    public String toString() {
	return name;
    }
    
    public String getSymbol() {
	return name;
    }

    public String getSafeSymbol() {
        return safename;
    }

}
