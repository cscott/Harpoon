
/**
 * CharContinuationOpt
 *
 *
 * Created: Fri Mar 17 15:24:21 2000
 *
 * @author bdemsky
 * @version
 */

package harpoon.Analysis.ContBuilder;
public class CharContinuationOpt extends CharContinuation {

    public CharContinuationOpt(char r) {
	result=r;
	done=true;
    }
    public void exception(java.lang.Throwable t) {}

} // CharContinuationOpt
