// Str2StrMap.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test20;

import java.util.Hashtable;

/**
 * <code>Str2StrMap</code> place-holder
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: Str2StrMap.java,v 1.1 2000-04-02 22:37:43 govereau Exp $
 */
public class Str2StrMap {
	Hashtable h = null;
	public Str2StrMap() {
		h = new Hashtable();
	}

	public synchronized String get(String key) {
		return (String)h.get(key);
	}

	public synchronized String put(String key, String value) {
		return (String)h.put(key, value);
	}		
}
