// AllocationStrategyFactory.java, created Mon Feb 10 19:25:29 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;

/**
 * <code>AllocationStrategyFactory</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AllocationStrategyFactory.java,v 1.1 2003-02-11 21:38:06 salcianu Exp $
 */
public interface AllocationStrategyFactory {
    /** Creates an <code>AllocationStrategy</code> object.
	@param fram backend specfic details */
    AllocationStrategy getAllocationStrategy(Frame frame);
}
