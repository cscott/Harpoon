package harpoon.Nate;

import harpoon.ClassFile.*;
import java.util.*;

public class MClass {
  HClass hclass;
  boolean instantiated;
  private Vector declaredFields;
  private Vector implementedFields;
  private Vector declaredMethods;
  private Vector implementedMethods;
  private Vector abstractMethods;
  private Vector accessedClasses;
  private Vector implementingClasses;
  private Hashtable children;


  Hashtable getChildren (){
    return children;
  }
  
  void addChild (MClass newClass){
    if (!children.containsKey (newClass)){
      children.put (newClass, newClass);
    }
  }

  void removeChild (MClass oldClass){
    children.remove (oldClass);
  }

  Vector getAccessedClasses (){
    return accessedClasses;
  }

  void addAccessedClass (HClass newClass){
    boolean flag = true;
    for (int i = 0; i < accessedClasses.size(); i++){
      if (newClass.getDescriptor().equals (((HClass)accessedClasses.elementAt(i)).getDescriptor()) &&
	  newClass.getName().equals (((HClass)accessedClasses.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      accessedClasses.addElement (newClass);
    }
  }


  Vector getImplementingClasses (){
    return implementingClasses;
  }

  void addImplementingClass (MClass newClass){
    boolean flag = true;
    for (int i = 0; i < implementingClasses.size(); i++){
      if (newClass.hclass.getDescriptor().equals (((MClass)implementingClasses.elementAt(i)).hclass.getDescriptor()) 
	  && newClass.hclass.getName().equals (((MClass)implementingClasses.elementAt(i)).hclass.getName())){
	flag = false;
      }
    }
    if (flag){
      implementingClasses.addElement (newClass);
    }
  }

  Vector getDeclaredFields (){
    return declaredFields;
  }

  void addDeclaredField (HField newField){
    boolean flag = true;
    for (int i = 0; i < declaredFields.size(); i++){
      if (newField.getDescriptor().equals (((HField)declaredFields.elementAt(i)).getDescriptor()) &&
	  newField.getName().equals (((HField)declaredFields.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      declaredFields.addElement (newField);
    }
  }

  Vector getImplementedFields (){
    return implementedFields;
  }


  void addImplementedField (HField newField){
    boolean flag = true;
    for (int i = 0; i < implementedFields.size(); i++){
      if (newField.getDescriptor().equals (((HField)implementedFields.elementAt(i)).getDescriptor()) &&
	  newField.getName().equals (((HField)implementedFields.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      implementedFields.addElement (newField);
    }
  }

  Vector getDeclaredMethods (){
    return declaredMethods;
  }

  void addDeclaredMethod (HMethod newMethod){
    boolean flag = true;
    for (int i = 0; i < declaredMethods.size(); i++){
      if (newMethod.getDescriptor().equals (((HMethod)declaredMethods.elementAt(i)).getDescriptor()) &&
	  newMethod.getName().equals (((HMethod)declaredMethods.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      declaredMethods.addElement (newMethod);
    }
  }

  Vector getImplementedMethods (){
    return implementedMethods;
  }


  void addImplementedMethod (HMethod newMethod){
    boolean flag = true;
    for (int i = 0; i < implementedMethods.size(); i++){
      if (newMethod.getDescriptor().equals (((HMethod)implementedMethods.elementAt(i)).getDescriptor()) &&
	  newMethod.getName().equals (((HMethod)implementedMethods.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      implementedMethods.addElement (newMethod);
    }
  }

  Vector getAbstractMethods (){
    return abstractMethods;
  }


  void addAbstractMethod (HMethod newMethod){
    boolean flag = true;
    for (int i = 0; i < abstractMethods.size(); i++){
      if (newMethod.getDescriptor().equals (((HMethod)abstractMethods.elementAt(i)).getDescriptor()) &&
	  newMethod.getName().equals (((HMethod)abstractMethods.elementAt(i)).getName())){
	flag = false;
      }
    }
    if (flag){
      abstractMethods.addElement (newMethod);
    }
  }

  MClass (HClass newClass, boolean instantiated){
    hclass = newClass;

    this.instantiated = instantiated;
    declaredMethods = new Vector();
    implementedMethods = new Vector();
    abstractMethods = new Vector(); 
    children = new Hashtable();
    accessedClasses = new Vector();

    declaredFields = new Vector();
    implementedFields = new Vector();

  }
  public boolean equals (Object obj) {
    return (hclass.equals(((MClass)obj).hclass));
  }
  
  public int hashCode (){
    return (hclass.getName().hashCode());
  }

}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
