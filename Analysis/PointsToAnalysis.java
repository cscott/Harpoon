// PointsToAnalysis.java, created Fri Mar  1 01:22:26 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import java.util.Set;

/**
 * The <code>PointsToAnalysis</code> interface encapsulates a points-to
 * pointer analysis algorithm, allowing transformation and analysis
 * code to use the results of a <code>PointsToAnalysis</code> w/o
 * knowing the details of its implementation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PointsToAnalysis.java,v 1.1.2.1 2002-03-04 20:53:03 cananian Exp $
 */
public abstract class PointsToAnalysis {

    /** Provides points-to information valid for all contexts in which
     *  <code>Temp</code> <code>t</code> defined at <code>defsite</code>
     *  is used or defined.  If <code>t1</code> defined at
     *  <code>d1</code> can point to the same object as
     *  <code>t2</code> defined at <code>d2</code>, then
     *  <code>pointsTo(d1,t1)</code> and <code>pointsTo(d2,t2)</code>
     *  will have a non-empty intersection.
     */
    public abstract Set<Node> pointsTo(HCodeElement defsite, Temp t);
    /** Provies points-to information valid only for a use of <code>t</code>
     *  (defined at <code>defsite</code> in the specific <code>Context</code>
     *  <code>c</code>.  You may be required to use the same
     *  <code>Context</code> implementation as the analysis.
     *  If <code>t1</code> defined at <code>d1</code> can, in context
     *  <code>c1</code>, point to the same object as <code>t2</code>
     *  defined at <code>d2</code> does in context <code>c2</code>, then
     *  <code>pointsTo(d1,t1,c1)</code> and <code>pointsTo(d2,t2,c2)</code>
     *  will have a non-empty intersection.
     */
    public abstract Set<Node> pointsTo(HCodeElement defsite, Temp t,Context c);

    /** <code>PointsToAnalysis.Node</code> is just a marker interface for
     *  the nodes in the points-to graph returned by the
     *  <code>PointsToAnalysis</code> interface. */
    public interface Node {
    }
}
