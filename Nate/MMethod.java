package harpoon.Nate;

import harpoon.ClassFile.*;

import java.util.Hashtable;

class MMethod {
  
  HMethod hmethod;
  HClass callingClass;
  
  MMethod (HMethod hmethod, HClass callingClass, MayCall mayCall){
    this.hmethod = hmethod;
    this.callingClass = callingClass;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
