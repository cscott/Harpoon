
/**
 * ObjectContinuationOpt
 *
 *
 * Created: Fri Mar 17 15:24:21 2000
 *
 * @author bdemsky
 * @version
 */

package harpoon.Analysis.ContBuilder;


public class ObjectContinuationOpt extends ObjectContinuation {

    public ObjectContinuationOpt(Object r) {
	result=r;
	done=true;
    }
    public void exception(java.lang.Throwable t) {}
} // IntContinuationOpt
