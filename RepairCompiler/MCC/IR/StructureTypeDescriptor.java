package MCC.IR;

/**
 * StructureTypeDescriptor
 *
 * represents structure types
 */

import java.util.*;

public class StructureTypeDescriptor extends TypeDescriptor {

    TypeDescriptor subtype;

    Hashtable fields = new Hashtable(); /* fast lookups */
    Vector fieldlist = new Vector(); /* ordering information */
    Hashtable labels = new Hashtable();
    int idnum;
    static int counter=0;

    public int getId() {
	return idnum;
    }

    public StructureTypeDescriptor(String name) {
        super(name);
	idnum=counter++;
    }

    public TypeDescriptor getGenerateType() {
        return ReservedTypeDescriptor.INT;
    }

    public Enumeration getFieldKeys() {
        return fields.keys();
    }
   
    
    public Expr getSizeExpr() {        
        return getOffsetExpr(null);
    }

    public Expr getOffsetExpr(FieldDescriptor field) {
	/* Fix sizeof calculations */
	if ((field==null)&&(subtype!=null))
	    return subtype.getSizeExpr();

	boolean aligned=true;
        Expr size = new IntegerLiteralExpr(0);
        
        for (int i = 0; i < fieldlist.size(); i++) {
            FieldDescriptor fd = (FieldDescriptor)fieldlist.elementAt(i);

            TypeDescriptor td = fd.getType();
            boolean ptr = fd.getPtr();
            Expr basesize; 
            if (ptr) { /* ptrs are 32bits */
		
		basesize = new IntegerLiteralExpr(32);
            } else {
		basesize = td.getSizeExpr();
            }
	    Expr fieldsize;
            if (fd instanceof ArrayDescriptor) {
                Expr totalsize = new OpExpr(Opcode.MULT, basesize, ((ArrayDescriptor) fd).getIndexBound());
		fieldsize=totalsize;
            } else {
                fieldsize=basesize;
            }
	    if (td instanceof ReservedTypeDescriptor) {
		ReservedTypeDescriptor rtd=(ReservedTypeDescriptor) td;
		if (rtd==ReservedTypeDescriptor.BIT) {
		    aligned=false;
		} else {
		    if (!aligned) {
			size=new OpExpr(Opcode.RND, size,null);
			aligned=true;
		    }
		}
	    } else {
		if (!aligned) {
		    size=new OpExpr(Opcode.RND, size,null);
		    aligned=true;
		}
	    }

            if (fd == field) { /* stop, reached target field */
                break; 
            }

            size = new OpExpr(Opcode.ADD, fieldsize, size);
        }

        if ((field==null)&&(!aligned))
	    return new OpExpr(Opcode.RND, size, null);
        return size;
    }

    public Iterator getFields() {
        return fields.values().iterator();
    }

    public Iterator getLabels() {
        return labels.values().iterator();
    }

    public FieldDescriptor getField(String name) {
        return (FieldDescriptor) fields.get(name);       
    }

    public LabelDescriptor getLabel(String name) {
        return (LabelDescriptor) labels.get(name);
    }

    public void addField(FieldDescriptor fd) {
        if (getField(fd.getSymbol()) != null) {
            throw new IRException("Can not overwrite a field once it has been added.");
        }        
        fields.put(fd.getSymbol(), fd);
        fieldlist.addElement(fd);
    }

    public void addLabel(LabelDescriptor ld) {
        if (getLabel(ld.getSymbol()) != null) {
            throw new IRException("Can not overwrite a label once it has been added.");
        }
        labels.put(ld.getSymbol(), ld);
    }

    public TypeDescriptor getSuperType() {
        return subtype;
    }

    public void setSuperType(TypeDescriptor td) {
        subtype = td;
    }

    public boolean isSubtypeOf(TypeDescriptor td) {
        if (td == this) {
            return true;
        } else {
            return subtype.isSubtypeOf(td);
        }
    }

}
