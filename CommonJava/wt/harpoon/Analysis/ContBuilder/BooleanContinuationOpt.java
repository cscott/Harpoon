
/**
 * BooleanContinuationOpt
 *
 *
 * Created: Fri Mar 17 15:24:21 2000
 *
 * @author bdemsky
 * @version
 */

package harpoon.Analysis.ContBuilder;
public class BooleanContinuationOpt extends BooleanContinuation {

    public BooleanContinuationOpt(boolean r) {
	result=r;
	done=true;
    }
    public void exception(java.lang.Throwable t) {}

} // BooleanContinuationOpt
