// multiset.java, created Sun Jan 23 17:22:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test3;

/**
 * <code>multiset</code> is a test for the PA algorithm taken
 from the third example of the first paper on compositional PA
 written by John Whaley and Martin Rinard (Section 2.3 Recursive Data
 Structures).

 <code>multisetElement.insert</code> is the interesting method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: multiset.java,v 1.2 2002-02-25 21:07:40 cananian Exp $
 */
public class multiset {

    multisetElement elements;
    
    /** Creates a <code>multiset</code>. */
    public multiset() {
        elements = null;
    }
    
    synchronized void addElement(Integer e){
	if(elements == null)
	    elements = new multisetElement(e, null);
	else
	    elements = elements.insert(e);
    }

    // We need some top level procedure to create at least one multiset
    // object, otherwise the call graph simply ignores its methods.
    // We hope no dead code elimination is done before the PA ...
    public static void main(String[] params){
	multiset ms = new multiset();
	ms.addElement(new Integer(1));
    }
}

class multisetElement{
    Integer element;
    int count;
    multisetElement next;
    
    multisetElement(Integer e, multisetElement n){
	count = 1;
	element = e;
	next = n;
    }
    
    synchronized boolean check(Integer e){
	if(element.equals(e)){
	    count++;
	    return true;
	}
	else return false;
    }

    synchronized multisetElement insert(Integer e){
	multisetElement m = this;
	while(m!=null){
	    if(m.check(e)) return this;
	    m = m.next;
	}
	return new multisetElement(e,this);
    }
}

