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

    public StructureTypeDescriptor(String name) {
        super(name);
    }

    public TypeDescriptor getGenerateType() {
        return ReservedTypeDescriptor.INT;
    }

    public Enumeration getFieldKeys() {
        return fields.keys();
    }
   
    private Vector getFieldSizes() {
        Vector fieldsizes = new Vector();
                
        for (int i = 0; i < fieldlist.size(); i++) {
            FieldDescriptor fd = (FieldDescriptor) fieldlist.elementAt(i);
            TypeDescriptor td = fd.getType();
            boolean ptr = fd.getPtr();

            Expr basesize; 
            if (ptr) { /* ptrs are 32bits */
                basesize = new IntegerLiteralExpr(32);
            } else {
                basesize = td.getSizeExpr();
            }

            if (fd instanceof ArrayDescriptor) {
                Expr totalsize = new OpExpr(Opcode.MULT, basesize, ((ArrayDescriptor) fd).getIndexBound());
                fieldsizes.addElement(totalsize);
            } else {
                fieldsizes.addElement(basesize);
            }
        }

        return fieldsizes;
    }
    
    public Expr getSizeExpr() {        
        Vector fieldsizes = getFieldSizes();

        /* we've got the field sizes! now return the addition! */
        Expr size = new IntegerLiteralExpr(0);
        
        for (int i = 0; i < fieldsizes.size(); i++) {
            Expr fieldsize = (Expr) fieldsizes.elementAt(i);
            size = new OpExpr(Opcode.ADD, fieldsize, size);
        }
        
        return size;
    }

    public Expr getOffsetExpr(FieldDescriptor field) {
        Vector fieldsizes = getFieldSizes();

        // #ATTN#: getOffsetExpr needs to be called with the fielddescriptor obect that is in teh vector list
        // this means that if the field is an arraydescriptor you have to call getOffsetExpr with the array 

        /* we've got the field sizes! now return the addition! */
        Expr size = new IntegerLiteralExpr(0);
        
        for (int i = 0; i < fieldsizes.size(); i++) {
            FieldDescriptor fd = (FieldDescriptor)fieldlist.elementAt(i);

            if (fd == field) { /* stop, reached target field */
                break; 
            }

            Expr fieldsize = (Expr) fieldsizes.elementAt(i);
            size = new OpExpr(Opcode.ADD, fieldsize, size);
        }
        
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

    public TypeDescriptor getSubType() {
        return subtype;
    }

    public void setSubType(TypeDescriptor td) {
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
