package harpoon.Analysis.MemOpt;


import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.Linker;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;

import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;

import harpoon.Analysis.Quads.CallGraph;

import harpoon.Temp.Temp;

import net.cscott.jutil.AggregateSetFactory;
import net.cscott.jutil.GenericMultiMap;
import net.cscott.jutil.LinearSet;
import net.cscott.jutil.MultiMap;
import net.cscott.jutil.WorkSet;
import harpoon.Util.Timer;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describe class <code>IncompatibilityAnalysis</code> here.
 *
 * @author <a href="mailto:Ovidiu Gheorghioiu <ovy@mit.edu>">Ovidiu Gheorghioiu</a>
 * @version 1.0
 */
public class IncompatibilityAnalysis {

    /**
     * If true, the analysis will not descend into classes other than
     * the class of the entry method. Useful for debugging.
     * */
    public static final boolean STAY_IN_DECLARING_CLASS = false;
    
    /**
     * If true, the analysis will show progress dots and other
     * progress indicators.
     * */
    public static final boolean SHOW_PROGRESS = false;

    
    /**
     * If true, and <code>SHOW_STATISTICS</code> is also true,
     * printStatistics will show A LOT OF statistics when it finishes.
     * */
    public static final boolean VERBOSE_STATISTICS = true;
    
    /**
     * If true, the analysis will show timings for all of its stages.
     * */
    public static final boolean SHOW_TIMINGS = true;

    /** If true, compute the sizes of the classes in bytes, otherwise,
	compute them in fields (an approximation).  Default is
	true. */
    public static boolean SIZE_IN_BYTES = true;

    // add this many fields as estimated overhead.
    private static final int ADD_FIELDS = 2;


    private Map mdCache; // cache of method data objects

    private MultiMap callees;
    
    private HMethod entry;
    private HCodeFactory codeFactory;
    private CallGraph callGraph;

    private LinkedList allMethods;

    // globally maps temps to the allocations defining them
    private Map globalAllocMap;

    // fake variables
    private static Object RETVAL = new String("RETVAL");
    private static Object RETEX = new String("RETEX");
    private static Object ESCAPE = new String("ESCAPE");

    // a set of all the above
    private static Set RETURNS = new LinearSet();
    static {
        RETURNS.add(RETVAL);
        RETURNS.add(RETEX);
        RETURNS.add(ESCAPE);
    }

    // our targets
    private MultiMap I;
    private Collection classes;
    private Set selfIncompatible, selfCompatible;

    private boolean callGraphNeedsSSA;

    // interface to the outside world

    /**
     * Convenience form for the other constructor, calls it with
     * <code>callGraphNeedsSSA</code> set to true.
     */
    public IncompatibilityAnalysis(HMethod entry, HCodeFactory codeFactory,
				   CallGraph callGraph, Linker linker) {
        this(entry, codeFactory, callGraph, linker, true);
    }

    /**
     * Creates a new <code>IncompatibilityAnalysis</code> instance.
     *
     * @param entry the entry method
     * @param codeFactory a <code>HCodeFactory</code>. This needs to be caching
     *   and in SSI form.
     * @param callGraph the <code>CallGraph</code> the analysis should use.
     * @param callGraphNeedsSSA hack that enables us to use Alex's
     * <code>SmartCallGraph</code>, which only operates on NoSSA form. If
     * you set to true, make sure you have set
     * <code>QuadSSI.KEEP_QUAD_MAP_HACK</code> to true. This is sucky, I
     * know, but there is no fast remedy.
     * 
     */
    public IncompatibilityAnalysis(HMethod entry, HCodeFactory codeFactory,
				   CallGraph callGraph, Linker linker,
                                   boolean callGraphNeedsSSA) {
        // init
        this.entry = entry;
        this.codeFactory = codeFactory;
        this.callGraph = callGraph;
        this.callGraphNeedsSSA = callGraphNeedsSSA;

        globalAllocMap = new HashMap();

        Timer timer, big_timer;

        if (SHOW_TIMINGS) {
            big_timer = new Timer();
            big_timer.start();
        }
        
        // stage 0: static analysis of methods for internal liveness & rest
        if (SHOW_TIMINGS) {
            timer = new Timer();
            timer.start();
        }
        
        intraproceduralAnalysis();
        
        if (SHOW_TIMINGS) {
            timer.stop();
            System.out.println("IA intraproc: " + timer);
        }
        
        // stage 1: compute An, Ae, Rn, Re, Esc
        if (SHOW_TIMINGS) {
            timer = new Timer();
            timer.start();
        }
        
        computeInitialSets();
        
        if (SHOW_TIMINGS) {
            timer.stop();
            System.out.println("IA points-to: " + timer);
        }

        // stage 2: compute I
        if (SHOW_TIMINGS) {
            timer = new Timer();
            timer.start();
        }
        
        computeI();
        
        if (SHOW_TIMINGS) {
            timer.stop();
            System.out.println("IA incompat: " + timer);
        }

        // stage 3: do compatible classes
        if (SHOW_TIMINGS) {
            timer = new Timer();
            timer.start();
        }
        
        computeClasses();

        if (SHOW_TIMINGS) {
            timer.stop();
            System.out.println("IA classes: " + timer);
	}

        if (SHOW_TIMINGS) {
            big_timer.stop();
            System.out.println("IA total: "+ big_timer);
        }
    }


    /**
     * Returns all allocation sites encountered by this analysis.
     *
     * @return a <code>Collection</code> of all the <code>NEW</code> quads processed.
     */
    public Collection allAllocationSites() {
        return Collections.unmodifiableCollection(globalAllocMap.values());
    }

    /**
     * Returns all the methods encountered by this analysis
     *
     * @return a <code>List</code> of all methods processed (as
     * <code>HMethod</code>s.)
     */
    public List allMethods() {
        return Collections.unmodifiableList(allMethods);
    }

    /**
     * Returns true if the given site shound be dynamic 
     * to the best of our knowledge, i.e. if we haven't seen this site
     *   for some reason (e.g. it's not reachable from the entry method), or
     *   if we have been unable to prove it can be allocated statically.
     *   Requires that the site is a <code>NEW</code> quad in SSI form.
     *   This takes the more general 
     *
     * @param e the allocation site. Right now, it must be a
     * <code>NEW</code> quad in SSI form. This parameter has the more general 
     * <code>HCodeElement</code> because in the future we might want to support
     * other forms and/or <code>ANEW</code> quads.
     * @return true if the allocation site should not be made static.
     */
    public boolean isSelfIncompatible(HCodeElement e) {
        assert e instanceof NEW : "allocations are NEW quads";

        NEW qNew = (NEW) e;
        Temp alloc = qNew.dst();

        return I.contains(alloc, alloc) || !globalAllocMap.containsKey(alloc);
    }

    /**
     * Returns true if the given allocation sites cannot use the same
     * memory to the best of our knowledge, i.e. we have not encountered
     * one or both of them, or we have unable to prove thay can safely use
     * the same memory. You should also call
     * <code>isSelfIncompatible()</code> to check whether the sites can be
     * allocated statically at all.
     *
     * @param e1,e2 the allocation sites. Right now, they must be
     *    <code>NEW</code> quads in SSI-form.
     * @return true if the sites should not use the same memory.
     */
    public boolean isIncompatible(HCodeElement e1, HCodeElement e2) {
        assert e1 instanceof NEW && e2 instanceof NEW :
            "allocations are NEW quads";

        NEW qNew1 = (NEW) e1; NEW qNew2 = (NEW) e2;
        Temp alloc1 = qNew1.dst(); Temp alloc2 = qNew2.dst();

        return I.contains(alloc1, alloc2);
    }

    /**
     * Returns a <code>Collection</code> whose members are disjunct
     * <code>Collection</code> of mutually compatible allocation sites
     * (<code>NEW</code> quads in SSI form). Every allocation site we
     * have encountered that can be made static is in one of these
     * classes.
     *
     * @return a <code>Collection</code> of compatible static allocation
     * classes.  */
    public Collection getCompatibleClasses() {
        LinkedList allocClasses = new LinkedList();
        for (Object thisClassO : classes) {
            Collection thisClass = (Collection) thisClassO;
            Collection newClass = new LinkedList();
            
            for (Iterator it2 = thisClass.iterator(); it2.hasNext(); ) {
                newClass.add(globalAllocMap.get(it2.next()));
            }

            allocClasses.add(newClass);
        }

        return allocClasses;
    }

    /**
     * Similar to the above, except it operates on a specified set of
     * allocation sites.
     *
     * @param allocs a <code>Collection</code> of allocation sites to be
     * divided into compatible classes.
     * @return a <code>Collection</code> of compatible classes. Every
     * allocation site in <code>alloc</code> that can be safely made static
     * is in one of these classes.  */
    public Collection getCompatibleClasses(Collection allocs) {
        Set allocVars = new HashSet();

        for (Object qO : allocs) {
            Quad q = (Quad) qO;
            if (!(q instanceof NEW)) continue;

            Temp temp = ((NEW) q).dst();
            if (globalAllocMap.containsKey(temp)) {
                allocVars.add(temp);
            }
        }

        allocVars.removeAll(selfIncompatible);

        Collection classes = MyGraphColorer.colorGraph(allocVars, I);
        
        LinkedList allocClasses = new LinkedList();
        for (Object thisClassO : classes) {
            Collection thisClass = (Collection) thisClassO;
            Collection newClass = new LinkedList();
            
            for (Iterator it2 = thisClass.iterator(); it2.hasNext(); ) {
                newClass.add(globalAllocMap.get(it2.next()));
            }
            
            allocClasses.add(newClass);
        }
        
        return allocClasses;
       
    }

    /**
     * Takes a Quad in NoSSA form, and returns the corresponding quad
     * in SSI form, generated by the <code>HCodeFactory</code> used to
     * create this <code>IncompatibilityAnalysis</code> instance. For
     * this to work correctly, our SSI factory *must* be a caching SSI
     * view of a caching SSA view.  I wish I had any other way to do
     * this kind of "bridging", but there apparently is none.
     *
     * @param q a <code>Quad</code> in NoSSA form/ @return the
     * corresponding <code>Quad</code> in SSI form, or null if we
     * couldn't find one (check the caching-SSI-of-caching-SSA
     * requirement) */
    public Quad getSSIQuad(Quad q) {
        HMethod method  = q.getFactory().getMethod();
        QuadSSI quadssi = (QuadSSI) codeFactory.convert(method);

        Quad retval = (Quad) quadssi.getQuadMapNoSSA2SSI().get(q);

        return retval;
    }
    
    // implementation

    // **** Step 0: analyze each method and gather everything we need for
    //      the analysis. This is the only pass that analyzes each element.

    // iterate through all of the methods and analyze them.
    private void intraproceduralAnalysis() {
        WorkSet workset = new WorkSet();
        mdCache = new HashMap();

        allMethods = new LinkedList();

        callees = new GenericMultiMap(new AggregateSetFactory());

        workset.add(entry);

        while (!workset.isEmpty()) {
            HMethod method = (HMethod) workset.removeFirst();

            
            // do the work
            MethodData md = createInitialMethodData(method);
            
            mdCache.put(method, md);
            if (md.isNative) continue;
            
            allMethods.addFirst(method);

            for (Object qCallO : md.calls.keySet()) {
                CALL qCall = (CALL) qCallO;
                Collection possibleCalls = md.calls.getValues(qCall);

                for (Object calledO : possibleCalls) {
                    HMethod called = (HMethod) calledO;

                    if (STAY_IN_DECLARING_CLASS &&
                        !(called.getDeclaringClass().equals(entry.getDeclaringClass())))
                        continue;
                    
                    callees.add(called, method);
                    
                    if (!mdCache.containsKey(called)) {
                        workset.addLast(called);
                    }
                }
            }
        }
    }

    private MethodData createInitialMethodData(HMethod method) {
        if (SHOW_PROGRESS) System.out.println("Analyzing " + method);
        
        // what we need
        HCode hcode = codeFactory.convert(method);
        if (hcode == null) {
            if (SHOW_PROGRESS) System.out.println("    native... ignoring");
            MethodData md = new MethodData();
            // return fake md
            md.isNative = true;
            return md;
        }

        // get ssi2nossa quadmap if needed
        Map quadSSI2NoSSA = 
	    callGraphNeedsSSA ? ((QuadSSI) hcode).getQuadMapSSI2NoSSA() : null;
        
        // ExactTypeMap typeMap = new SCCAnalysis(hcode);
        // ReachingDefs rd= new SSxReachingDefsImpl(hcode);

        // We can probably do with less objects, but dev time is the constraint
        //   now. 
        Collection allocations = new ArrayList();
        Collection allocationSites = new ArrayList();
        Collection escapeSites = new ArrayList();
        
        Collection retNormal = new ArrayList();
        Collection retNormalSites = new ArrayList();
        Collection retEx = new ArrayList(1);
        Collection retExSites = new ArrayList(1);

        Collection callParams = new ArrayList();
        Collection callReturns = new ArrayList();

        Map conditions = new HashMap();
        
        Collection escapes = new ArrayList();

        MultiMap param2calls = new GenericMultiMap(new AggregateSetFactory());
        
        METHOD header = null;
                
        MultiMap calls = new GenericMultiMap(new AggregateSetFactory());
        MultiMap liveness = new GenericMultiMap(new AggregateSetFactory());

        // helpers for liveness
	// edges for which we are interested in liveness & reachability
        Set interestingEdges = new HashSet(); 

        // direct assignments: which temps get assigned to this temp
        MultiMap assignMap = new GenericMultiMap(new AggregateSetFactory());

        // traverse the graph. Reimplement this using a Visitor if it threatens
        //    to become unmaintainable.
        // The thing is, IMHO, code that uses visitors is pretty ugly too.
        for (Iterator it = hcode.getElementsI(); it.hasNext(); ) {
            Quad q = (Quad) it.next();

            // allocation?
            if (q.kind() == QuadKind.NEW) {
                NEW qNew = (NEW) q;
                Temp temp = qNew.dst();
                
                allocations.add(temp);
                allocationSites.add(qNew);
                globalAllocMap.put(temp, qNew);
                
                assert qNew.nextLength() == 1 : 
		    "NEW should only have one incoming edge";

                interestingEdges.add(qNew.prevEdge(0));
                interestingEdges.add(qNew.nextEdge(0));
	    }
            // call site?
            else if (q.kind() == QuadKind.CALL) {
                CALL qCall = (CALL) q;
                CALL qCallToPass = qCall;

                if (callGraphNeedsSSA) {
                    qCallToPass = (CALL) quadSSI2NoSSA.get(qCall);
                    assert qCallToPass != null : "SSI->SSA mapping failed";
                }
                    
                HMethod[] calledMethods = callGraph.calls(method, qCallToPass);

                // FIXME: figure out how important this is
                //  and upgrade to assertion / downgrade to DEBUG stmnt
                if (calledMethods.length == 0) {
                    System.out.println("*** Warning: no methods found for: "
                                       + Util.code2str(qCall));
                }

                for (int i = 0; i<calledMethods.length; i++ ) {
                    HMethod called = calledMethods[i];
                    calls.add(qCall, called);
                }

                callParams.addAll(Arrays.asList(qCall.params()));
                
                if (qCall.retval() != null) callReturns.add(qCall.retval());
                callReturns.add(qCall.retex());

                interestingEdges.add(qCall.prevEdge(0));
                interestingEdges.add(qCall.nextEdge(0));
                interestingEdges.add(qCall.nextEdge(1));

                for (int i = 0; i<qCall.paramsLength(); i++) {
                    param2calls.add(qCall.params(i), qCall);
                }
            }
            // direct assignment? (MOVE, PHI, SIGMA)
            //   MOVE?
            else if (q.kind() == QuadKind.MOVE) {
                MOVE qMove = (MOVE) q;
                assignMap.add(qMove.src(), qMove.dst());
            }
            // PHI?
            else if (q instanceof PHI) {
                PHI qPhi = (PHI) q;

                for (int i = 0; i<qPhi.numPhis(); i++) {
                    for (int j = 0; j<qPhi.arity(); j++) {
                        assignMap.add(qPhi.src(i, j), qPhi.dst(i));
                    }
                }
            }
            // RETURN?
            else if (q.kind() == QuadKind.RETURN) {
                RETURN qRet = (RETURN) q;
                if (qRet.retval() != null) retNormal.add(qRet.retval());
                retNormalSites.add(qRet);
            }
            // THROW?
            else if (q.kind() == QuadKind.THROW) {
                THROW qThrow = (THROW) q;
                retEx.add(qThrow.throwable());
                retExSites.add(qThrow);
            }
            // METHOD?
            else if (q.kind() == QuadKind.METHOD) {
                assert header == null : "Only one METHOD quad";
                header = (METHOD) q;
            }
            // SET?
            else if (q.kind() == QuadKind.SET) {
                SET qSet = (SET) q;
                escapes.add(qSet.src());
                escapeSites.add(qSet);
            }
            // ASET?
            else if (q.kind() == QuadKind.ASET) {
                ASET qASet = (ASET) q;
                escapes.add(qASet.src());
                escapeSites.add(qASet);
            }
            
            // SIGMA stuff. Note that this might overlap w/above
            // handle typeswitch sigmas differently
            if (q instanceof SIGMA) {
                SIGMA qSigma = (SIGMA) q;
                for (int i = 0; i<qSigma.numSigmas(); i++) {
                    for (int j = 0; j<qSigma.arity(); j++) {
                        assignMap.add(qSigma.src(i), qSigma.dst(i, j));
                        if (q instanceof TYPESWITCH &&
                            qSigma.src(i).equals(((TYPESWITCH) q).index())) {
                            conditions.put(qSigma.dst(i, j),
                                           new TypeSwitchCondition((TYPESWITCH) q, j));
                        }
                                           
                    }
                }
            }
        }

        // interesting temps
        Set externals = new HashSet(allocations);
        externals.addAll(Arrays.asList(header.params()));
        Set internals = new HashSet(callParams);
        internals.addAll(callReturns);
        internals.addAll(conditions.keySet());
        internals.addAll(escapes);

        // add fake temps
        MultiMapUtils.multiMapAddAll(assignMap, retNormal, RETVAL);
        MultiMapUtils.multiMapAddAll(assignMap, retEx, RETEX);
        MultiMapUtils.multiMapAddAll(assignMap, escapes, ESCAPE);
        internals.add(RETVAL);
        internals.add(RETEX);
        internals.add(ESCAPE);

        // form interestingTemps collection. We don't need a fast contains()
        //   here, so use ArrayList.
        Collection interestingTemps = new ArrayList(externals.size()
                                                    + internals.size());
        interestingTemps.addAll(internals);
        interestingTemps.addAll(externals);

        // we are only interested in variables that get assigned the above,
        //    minus call params and escapes (e.g. a call param is not
        //    inherently interesting, only if it can hold an object)
        Set interestingRoots = new HashSet(allocations);
        interestingRoots.addAll(Arrays.asList(header.params()));
        interestingRoots.addAll(callReturns);
        interestingRoots.addAll(conditions.keySet());
                               

        // we are only interested in assignments between interesting temps
        
        // close assignMap as per the interesting roots
        //   but: do *NOT* allow propagation over conditionals
        //   *THAT* propagation is assigned-object-dependent

        assignMap = MultiMapUtils.multiMapClosure(assignMap, interestingRoots,
                                                  conditions.keySet());
        
        // compute reverse mapping of vars to possible values
        MultiMap valuesMap = MultiMapUtils.multiMapInvert(assignMap,
                                                          interestingRoots);

        // we don't need assignMap anymore, free it
        assignMap = null;

        SSILiveness ssiLiveness = new SSILiveness(hcode);

        // retain the liveness info we need
        // i.e., for interestingTemps and interestingEdges, via valuesMap
        for (Object edgeO : interestingEdges) {
            Edge edge = (Edge) edgeO;

            Set lvOn = ssiLiveness.getLiveOn(edge);

            for (Object tempO : lvOn) {
                Temp temp = (Temp) tempO;

                liveness.addAll(edge, valuesMap.getValues(temp));
                if (interestingRoots.contains(temp)) {
                    liveness.add(edge, temp);
                }
            }
        }

        // that's it, construct and return methodData
        MethodData md = new MethodData();

        // static values
        md.liveness = liveness;
        md.calls = calls;
        md.header = header;
        md.param2calls = param2calls;
        
        md.conditions = conditions;
        md.externals = externals;
        md.internals = internals;
        
        md.allocationSites = allocationSites;
        md.escapeSites = escapeSites;
        
        Set reachNormal = canReach(retNormalSites, true);
        Set reachEx = canReach(retExSites, true);
        
        // Only keep reachability info for interesting nodes, ie, nodes
        //     after calls.
        md.reachNormal = new HashSet();
        md.reachEx = new HashSet();

        for (Object edgeO : interestingEdges) {
            Edge edge = (Edge) edgeO;
            Quad q = edge.to();

            if (reachNormal.contains(q)) md.reachNormal.add(q);
            if (reachEx.contains(q)) md.reachEx.add(q);
        }
        
        // initial values of dynamic sets

        // aliases: only keep between interesting temps
        MultiMap aliasedValues =
            MultiMapUtils.multiMapFilter(valuesMap,
                                         interestingTemps,
                                         interestingTemps);
    
        md.aliasedValues = aliasedValues;
        md.aliasedAssigns =
            MultiMapUtils.multiMapInvert(aliasedValues, interestingTemps);

        // sanity check. take this out when sure the code works right
        //   (and put it back when it doesn't :)
        // assert MultiMapUtils.intersect(md.aliasedValues.keySet(,
        //                                     externals) == null);

        // An, Ae: use reachability on allocs in this method
        md.An = new HashSet();
        md.Ae = new HashSet();

        for (Object qNewO : allocationSites) {
            NEW qNew = (NEW) qNewO;

            if (reachNormal.contains(qNew)) md.An.add(qNew.dst());
            if (reachEx.contains(qNew)) md.Ae.add(qNew.dst());
        }

        // simulate pointsTo info from what we have
        MultiMap pointsTo = new GenericMultiMap(new AggregateSetFactory());
        for (Object tempO : externals) {
            Temp temp = (Temp) tempO;
            pointsTo.add(temp, temp);
        }

        // propagate pointsTo by simulating deltas
        propagateExternalDeltas(md, pointsTo);

        // initial values of Rn, Re, E: use fake variables
        md.Rn = new HashSet(pointsTo.getValues(RETVAL));
        md.Re = new HashSet(pointsTo.getValues(RETEX));
        md.E = new HashSet(pointsTo.getValues(ESCAPE));

        // done, hopefully
        return md;
    }


    // computes all quads from which the given sites are reachable
    // takes O(Nacc), where Nacc is the number of edges that can reach "sites"
    private Set canReach(Collection sites, boolean addInitial) {
        Set canreach = addInitial? new HashSet(sites) : new HashSet();
        WorkSet workset = new WorkSet(sites);
        
        while (!workset.isEmpty()) {
            Quad q = (Quad) workset.removeFirst();

            for (int i = 0; i<q.prevLength(); i++) {
                Quad prev = q.prevEdge(i).from();

                if (canreach.add(prev)) {
                    workset.add(prev);
                }
            }
        }

        return canreach;
    }

    // **** Stage 1: Fixed-point set computation for the 5 sets we need
    //      (An, Ae, Rn, Re, E)

    // Fixed point lodic
    private void computeInitialSets() {
        if (SHOW_PROGRESS) System.out.print("Fixed point set (Ax, Rx, E)");
        
        WorkSet workset = new WorkSet();

        workset.addAll(allMethods);

        while (!workset.isEmpty()) {
            HMethod method = (HMethod) workset.removeFirst();

            if (recomputeInitialSets(method)) {
                for (Object calleeO : callees.getValues(method)) {
                    HMethod callee = (HMethod) calleeO;
                    workset.addLast(callee);
                }
            }
        }

        if (SHOW_PROGRESS) System.out.println();
    }

    // Do the actual work for each method
    private boolean recomputeInitialSets(HMethod method) {
        if (SHOW_PROGRESS) System.out.print(".");

        MethodData md = (MethodData) mdCache.get(method);
        assert !md.isNative;

        int anSize = md.An.size(); int aeSize = md.Ae.size();
        int rnSize = md.Rn.size(); int reSize = md.Re.size();
        int eSize = md.E.size();

        // This is terribly, terribly inefficient
        // Should really be using deltas...
        for (Object qCallO : md.calls.keySet()) {
            CALL qCall = (CALL) qCallO;
            boolean reachNN = md.reachNormal.contains(qCall.nextEdge(0).to());
            boolean reachNE = md.reachEx.contains(qCall.nextEdge(0).to());
            boolean reachEN = md.reachNormal.contains(qCall.nextEdge(1).to());
            boolean reachEE = md.reachEx.contains(qCall.nextEdge(1).to());

            // sanity check
            assert  (reachNN || reachNE) && (reachEN || reachEE) :
                "Must have a way of getting out of the method (at "
                + Util.code2str(qCall)+")";

            
            for (Object calledO : md.calls.getValues(qCall)) {
                HMethod called = (HMethod) calledO;
                MethodData mdCalled = (MethodData) mdCache.get(called);

                if (mdCalled == null || mdCalled.isNative) continue;

                // do An, Ae changes
                if (reachNN) md.An.addAll(mdCalled.An);
                if (reachNE) md.Ae.addAll(mdCalled.An);
                if (reachEN) md.An.addAll(mdCalled.Ae);
                if (reachEE) md.Ae.addAll(mdCalled.Ae);

            }
        }

        // Rn, Re, E computation
        
        // for now, we don't compute deltas, just full pointsTo information
        // this has to change if we wanna be efficient
        MultiMap externalPointsTo = new GenericMultiMap();
        MultiMap internalPointsTo = new GenericMultiMap();

        computePointsTo(md, externalPointsTo, internalPointsTo);

        // non-optimized since we don't expect this to happen a lot
        if (md.aliasedValues.addAll(internalPointsTo)) {
            // i don't think we need redo transitivity...
            // FIXME: maybe reconsider

            md.aliasedAssigns.addAll(MultiMapUtils.multiMapInvert(internalPointsTo, null));
            
            // if we implement deltas, we should re-computePointsTo here,
            // or use just deltas if aliases haven't changed; but we do
            // pointsTo anyway (for now; inefficient)
            
            // the "o" indicates an alias recomputation is needed
            if (SHOW_PROGRESS) System.out.print("o");
        }

        propagateExternalDeltas(md, externalPointsTo);


        
        // finally, add any deltas to final variables to the final sets
        md.Rn.addAll(externalPointsTo.getValues(RETVAL));
        md.Re.addAll(externalPointsTo.getValues(RETEX));
        md.E.addAll(externalPointsTo.getValues(ESCAPE));
       
        return anSize != md.An.size() || aeSize != md.Ae.size()
            || rnSize != md.Rn.size() || reSize != md.Re.size()
            || eSize != md.E.size();
        
    }

    

    // this recomputes pointsTo information from params, allocs & calls
    //    separating it into external and internal
    // Note: only assignments between call values and call params will be
    //    written to internalPointsTo. The rest of the info should come
    //    from aliasedValues & friends. The info in internalPointsTo is
    //    meant to be added to aliases. internalPointsTo can be null if we
    //    are not interested in them.

    // this does NOT do transitivity. it must be handled externally, for good
    //    reasons.
    private void computePointsTo(MethodData md,
                                 MultiMap externalPointsTo,
                                 MultiMap internalPointsTo) {

        // add allocs & params
        for (Object tempO : md.externals) {
            Temp temp = (Temp) tempO;
            externalPointsTo.add(temp, temp);
        }
        
        for (Object qCallO : md.calls.keySet()) {
            CALL qCall = (CALL) qCallO;
            Temp [] actParams = qCall.params();
            for (Object calledO : md.calls.getValues(qCall)) {
                HMethod called = (HMethod) calledO;
                MethodData mdCalled = (MethodData) mdCache.get(called);
                
                if (mdCalled == null || mdCalled.isNative) continue;
                Temp [] declParams = mdCalled.header.params();
                
                if (qCall.retval() != null)
                    addCallDeltas(md,
                                  qCall.retval(),
                                  mdCalled.Rn,
                                  actParams,
                                  declParams,
                                  externalPointsTo,
                                  internalPointsTo);
                
                addCallDeltas(md,
                              qCall.retex(),
                              mdCalled.Re,
                              actParams,
                              declParams,
                              externalPointsTo,
                              internalPointsTo);
                
                addCallDeltas(md,
                              ESCAPE,
                              mdCalled.E,
                              actParams,
                              declParams,
                              externalPointsTo,
                              internalPointsTo);
                
            }
            
        }

    }
            
    
    // what a mouthful
    private void addCallDeltas(MethodData md,
                               Object var,
                               Set delta,
                               Temp[] actParams,
                               Temp[] declParams,
                               MultiMap externalDeltas,
                               MultiMap internalDeltas ) {
        
        assert actParams.length == declParams.length;

        // assume params in rets is actually a rare case
        externalDeltas.addAll(var, delta);

        for (int i = 0; i<actParams.length; i++) {
            if (delta.contains(declParams[i])) {

                externalDeltas.remove(var, declParams[i]);

                if (md.externals.contains(actParams[i])) {
                    externalDeltas.add(var, actParams[i]);
                }
                    
                if (internalDeltas != null)
                    internalDeltas.add(var, actParams[i]);
                
            }
        } 
    }


    // propagates deltas through aliases
    private void propagateExternalDeltas(MethodData md, MultiMap deltas) {
        WorkSet workset = new WorkSet();

        workset.addAll(deltas.keySet());

        while (!workset.isEmpty()) {
            Object var = workset.removeFirst();
            Collection thisDelta = deltas.getValues(var);

            // var has a delta, ie new possible values
            
            // for all variables that get assigned var
            for (Iterator it = md.aliasedAssigns.getValues(var).iterator();
                 it.hasNext(); ) {
                Object assign = it.next();

                boolean changed = false;

                // if this variable has a condition associated with it...
                if (md.conditions.containsKey(assign)) {
                    Condition condition =
                        (Condition) md.conditions.get(assign);

                    // filter deltas through the condition
                    for (Object tempO : thisDelta) {
                        
                        Temp temp = (Temp) tempO; 
                        if (condition.isSatisfiedFor(temp)) 
                            changed = deltas.add(assign, temp) || changed;
                        else ; // print some debug stuff if you needto
                    }
                } else {
                    // otherwise, add the whole delta
                    changed = deltas.addAll(assign, thisDelta) || changed;
                }

                if (changed) workset.addLast(assign);
            }
        }
    }

    // **** Stage 2: Compute incompatibilities

    // Fixed-point logic
    private void computeI() {

        I = new GenericMultiMap();

	if(SHOW_PROGRESS)
	    System.out.print("Computing initial Is");
        
        for (Object methodO : allMethods) {
            HMethod method = (HMethod) methodO;

            computeInitialI(method);
        }
        
        // fixed point set analysis for I
        // this is strinkingly similar to the one in stage 1
        //    we should really have a common interface of sorts
        WorkSet workset = new WorkSet();

        workset.addAll(allMethods);

        if (SHOW_PROGRESS) System.out.print("\nFixed point set (I)");

        while (!workset.isEmpty()) {
            HMethod method = (HMethod) workset.removeFirst();

            if (recomputeI(method)) {
                for (Object calleeO : callees.getValues(method)) {
                    HMethod callee = (HMethod) calleeO;
                    workset.addLast(callee);
                }
            }
        }

        // finally, make sure I is symmetric
        MultiMapUtils.ensureSymmetric(I);
        if (SHOW_PROGRESS) System.out.println();
    }

    private void computeInitialI(HMethod method) {
	if(SHOW_PROGRESS) System.out.print(".");
        MethodData md = (MethodData) mdCache.get(method);
        if (md == null || md.isNative) return;

        Collection params = Arrays.asList(md.header.params());


        // System.out.println("> i enter: " + method);
        // init Ip
        md.Ip = new GenericMultiMap(new AggregateSetFactory());

        // compute pointsTo information
        MultiMap pointsTo = new GenericMultiMap();
        computePointsTo(md, pointsTo, null);
        // System.out.println("pointsto...");
        propagateExternalDeltas(md, pointsTo);
        // System.out.println("extdeltas...");
        

        // compute "meta allocation nodes", i.e. nodes that directly or
        // indirectly cause allocations
        Collection interestingNodes =
            new ArrayList(md.allocationSites.size() + md.calls.keySet().size());
        interestingNodes.addAll(md.allocationSites);
        interestingNodes.addAll(md.calls.keySet());
        // System.out.println("Interestingnodes: " + interestingNodes.size());
        
        // compute mappings from escape sites to things that escape there

        // first, simple escapes
        // System.out.println("Simple escapes...");
        MultiMap escapes = new GenericMultiMap();
        for (Object qO : md.escapeSites) {
            Quad q = (Quad) qO;
            if (q.kind() == QuadKind.SET) {
                SET qSet = (SET) q;
                escapes.addAll(qSet, pointsTo.getValues(qSet.src()));
            } else if (q.kind() == QuadKind.ASET) {
                ASET qASet = (ASET) q;
                escapes.addAll(qASet, pointsTo.getValues(qASet.src()));
            }
        }

        // System.out.println("Escapes from calls...");
        // escapes from calls
        for (Object qCallO : md.calls.keySet()) {
            CALL qCall = (CALL) qCallO;
            Temp[] actParams = qCall.params();

            for (Object calledO : md.calls.getValues(qCall)) {
                HMethod called = (HMethod) calledO;
                MethodData mdCalled = (MethodData) mdCache.get(called);

                if (mdCalled == null || mdCalled.isNative) continue;


                Temp[] declParams = mdCalled.header.params();
                
                escapes.addAll(qCall, mdCalled.E);

                // do param replacement; add pointsTo for params
                for (int i = 0; i<actParams.length; i++) {
                    if (escapes.contains(qCall, declParams[i])) {
                        escapes.remove(qCall, declParams[i]);
                        escapes.addAll(qCall, pointsTo.getValues(actParams[i]));
                    }
                }

            }
        }

        // System.out.println("Total escapes: " + escapes.size());

        Set escKeySet = new HashSet(escapes.keySet());

        // System.out.println("Per meta alloc node...");
        // for each meta alloc node
        for (Object qO : interestingNodes) {
            Quad q = (Quad) qO;

            Set allocs;

            // allocs for outgoing edges
            Set[] edgeAllocs = new Set[q.nextLength()];
            // outgoing dest vars
            Temp[] edgeDst = new Temp[q.nextLength()];

            // System.out.println("  Per node: " + Util.code2str(q));            
            if (q.kind() == QuadKind.NEW) {
                allocs = edgeAllocs[0] = Collections.singleton( ((NEW) q).dst());

                edgeDst[0] = ((NEW) q).dst();
            } else {
                assert q.kind() == QuadKind.CALL;
                
                edgeAllocs[0]= new HashSet();
                edgeAllocs[1] = new HashSet();
                
                for (Object calledO : md.calls.getValues(q)) {
                    HMethod called = (HMethod) calledO;
                    MethodData mdCalled = (MethodData) mdCache.get(called);

                    if (mdCalled == null || mdCalled.isNative) continue;

                    edgeAllocs[0].addAll(mdCalled.An);
                    edgeAllocs[1].addAll(mdCalled.Ae);
                }

                allocs = new HashSet(edgeAllocs[0].size()
                                     + edgeAllocs[1].size());
                allocs.addAll(edgeAllocs[0]);
                allocs.addAll(edgeAllocs[1]);

                edgeDst[0] = ((CALL) q).retval();
                edgeDst[1] = ((CALL) q).retex();
            }

            // add incompatibilities from escapes
            // compute reachability of this node (there are more efficient
            //   ways though...)

            // System.out.println("   # allocs: " + allocs.size());
            Set canReachQ = canReach(Collections.singleton(q), false);

            // System.out.println("  escapes x allocs... ");

            Set escCanReachQ = MultiMapUtils.multiMapUnion(escapes,
                                                           canReachQ);
            /// System.out.println("   # escCanReachQ: " + escCanReachQ.size());
            
            for (Object escapedO : escCanReachQ) {
                Temp escaped = (Temp) escapedO;
                if (params.contains(escaped)) {
                    md.Ip.addAll(escaped, allocs);
                } else {
                    assert globalAllocMap.containsKey(escaped);
                    I.addAll(escaped, allocs);
                }
            }
            
            // System.out.println("   # escCanReachQ: " + escCanReachQ);

            // System.out.println("   # live-in objects: "  + liveInObjects.size());
                                                            
            // add incompatibilities from liveness
            // for each outgoing edge
            for (int i = 0; i<q.nextLength(); i++) {
                // edgeAllocs empty? no point going on
                if (edgeAllocs[i].isEmpty()) continue;
                
                Edge edge = q.nextEdge(i);

                Collection liveInternal = md.liveness.getValues(edge);

                liveInternal.remove(edgeDst[i]);
                
                Set liveOverObjects = 
		    MultiMapUtils.multiMapUnion(pointsTo, liveInternal);

                // all live objects become incompatible with allocs here
                // again, a good ol' cartesian product
                for (Object liveO : liveOverObjects) {
                    Temp live = (Temp) liveO;

                    if (params.contains(live)) {
                        md.Ip.addAll(live, edgeAllocs[i]);
                    } else {
                        assert globalAllocMap.containsKey(live);

                        I.addAll(live, edgeAllocs[i]);
                    }
                }
            }

        }

        // System.out.println("< i exit");
        
    }


    private boolean recomputeI(HMethod method) {
        if (SHOW_PROGRESS) System.out.print(".");
        MethodData md = (MethodData) mdCache.get(method);
        assert !md.isNative;

        int ipSize = md.Ip.size();

        
        // This is terribly, terribly inefficient
        // Should really be using deltas...
        // (yes, here too :)

        MultiMap pointsTo = new GenericMultiMap();
        computePointsTo(md, pointsTo, null);
        propagateExternalDeltas(md, pointsTo);
        
        Collection params = Arrays.asList(md.header.params());
        
        for (Object qCallO : md.calls.keySet()) {
            CALL qCall = (CALL) qCallO;
            Temp[] actParams = qCall.params();

            
            for (Object calledO : md.calls.getValues(qCall)) {
                HMethod called = (HMethod) calledO;
                MethodData mdCalled = (MethodData) mdCache.get(called);

                if (mdCalled == null || mdCalled.isNative) continue;
                Temp [] declParams = mdCalled.header.params();

                // replace params
                for (int i = 0; i<actParams.length; i++) {
                    if (mdCalled.Ip.containsKey(declParams[i])) {
                        for (Object tempO : pointsTo.getValues(actParams[i])) {
                            Temp temp = (Temp) tempO;

                            if (params.contains(temp)) {
                                md.Ip.addAll(temp,
                                             mdCalled.Ip.getValues(declParams[i]));
                            }
                            else {
                                I.addAll(temp,
                                         mdCalled.Ip.getValues(declParams[i]));
                            }
                            
                        }
                    }
                }
            }

        }

        return ipSize != md.Ip.size();
    }
        
    // **** Stage 3: classes computation

    // FIXME: try alternate heuristics that take sizes into account
    private void computeClasses() {
        selfCompatible = new HashSet();
        selfIncompatible = new HashSet();

        // compute self-compatibles and self-incompatibles
        for (Object allocO : globalAllocMap.keySet()) {
            Temp alloc = (Temp) allocO;

            if (I.contains(alloc, alloc)) {
                selfIncompatible.add(alloc);
            } else {
                selfCompatible.add(alloc);
            }

        }

        classes = MyGraphColorer.colorGraph(selfCompatible, I);

     }



    // *** Lots and lots of debugging code

    // This prints the statistics. Comment in/out what you want
    public void printStatistics(Frame frame, Linker linker) {

	if(VERBOSE_STATISTICS) {
	    // show off some end-results
	    MethodData md = (MethodData) mdCache.get(entry);
	    
	    // System.out.println("An for entry: " + md.An);        
	    // System.out.println("Ae for entry: " + md.Ae);
	    
	    HashSet allocs = new HashSet(md.An);
	    allocs.add(md.Ae);

	    // System.out.println("Rn for entry: " + md.Rn);        
	    // System.out.println("Re for entry: " + md.Re);
	    
	    // System.out.println("E for entry: " + md.E);
	    
	    // System.out.println("Liveness for entry: " + md.liveness);
	    // System.out.println("aliases for entry: " + md.aliasedValues);
	    
	    // System.out.println("Incompatibility: " + I);
	    
	    
	    // System.out.println("Classes: ");
	    // System.out.println(classes);
	    
	    // System.out.println("selfIncompatible: " + selfIncompatible);
	    
	    // System.out.println("Legend: ");
	    // printTempCollection(I.keySet());
	    
	    System.out.println("escape detail: ");
	    printTempCollection(md.E);
	    
	    System.out.println("Self-incompatible detail: ");
	    printTempCollection(selfIncompatible);
	    
	    
	    System.out.println("Global type statistics: ");
	    printTypeStatistics(globalAllocMap.keySet());
	    
	    System.out.println("Totally compatible type statistics: ");
	    Collection totally = new HashSet(globalAllocMap.keySet());
	    totally.removeAll(I.keySet());
	    printTypeStatistics(totally);
	    
	    System.out.println("Class type statistics: ");
	    int nclass = 0;
	    for (Object thisClassO : classes) {
		Collection thisClass = (Collection) thisClassO;
		nclass++;
		
		System.out.println(" *Class " + nclass);
		
		assert MultiMapUtils.intersect(thisClass,
					       selfIncompatible).isEmpty();

		printTypeStatistics(thisClass);
	    }

        
	    System.out.println("Self-incompatible type statistics: ");
	    printTypeStatistics(selfIncompatible, globalAllocMap.keySet());
	    
	    System.out.println("Statics type statics: ");
	    printTypeStatistics(selfCompatible, globalAllocMap.keySet());
	}

	    
	System.out.println("Statistics: ");
        System.out.println
	    ("   " + allMethods.size() + " methods analyzed;\t" +
	     sizeStatistics(allMethods, codeFactory) + " SSI;\t" +
	     sizeStatistics(allMethods,
			    harpoon.IR.Bytecode.Code.codeFactory()) +
	     " bytecodes");
        System.out.println("   " + globalAllocMap.keySet().size() + " allocations");
        System.out.println("   " + I.size() + " incompatible pairs");
        System.out.println("   " + classes.size() + " classes ("
                           + (100 - (classes.size()*100/(globalAllocMap.keySet().size() - selfIncompatible.size()))) +"% reduction)");
        System.out.println("   " + selfIncompatible.size() + " self-incompatible vars ("+(selfIncompatible.size()*100/globalAllocMap.keySet().size())+"%)");


	// compute the pre-allocated memory size
        long psize = 0L; // size of preallocated memory (with packing)
        long usize = 0L; // size of preallocated memory (no packing)

        Collection allocClasses = getCompatibleClasses();

        for (Object thisClassO : allocClasses) {
            Collection thisClass = (Collection) thisClassO;
	    long max = 0L;
	    for(Iterator itc = thisClass.iterator(); itc.hasNext(); ) {
		HClass allocatedClass = ((NEW) itc.next()).hclass();
		int size = SIZE_IN_BYTES ?
		    PreallocOpt.sizeForClass(frame.getRuntime(),
					     allocatedClass) :
		    fieldsForClass(allocatedClass);
		usize += size;
		max = Math.max(max, size);
	    }
	    psize += max; 
        }

	System.out.println
	    ("Preallocated memory size " + 
	     (SIZE_IN_BYTES ? "(bytes): " : "(fields): ") +
	     usize + " (normal) / " +
	     psize + " (sharing);\treduction = " +
	     Util.doubleRep
	     ( (100 * ((double) usize - psize)) / ((double) usize), 5, 2) +
	     "%");

	printSubTypeStatistics("Exceptions",
			       linker.forName("java.lang.Throwable"));
	printSubTypeStatistics("StringBuffers",
			       linker.forName("java.lang.StringBuffer"));
	printSubTypeStatistics("Iterators",
			       linker.forName("java.util.Iterator"));
    }

    private void printSubTypeStatistics(String label, HClass hclass) {
        int total = countTempIsA(globalAllocMap.keySet(), hclass);
        int stat  = countTempIsA(selfCompatible, hclass);

        System.out.println(label + "\t" + stat + "\tof " + total + 
			   "\t" + (stat*100/Math.max(total, 1)) + "%");
    }

    private  int countTempIsA(Collection temps, HClass hclass) {
        int count = 0;

        for (Object tempO : temps) {
            Temp temp = (Temp) tempO;

            if (tempIsA(temp, hclass)) count ++;
        }

        return count;
    }


    private static int fieldsForClass(HClass hclass) {
	return hclass.getFields().length + ADD_FIELDS;
    }


    private void printmap(Map map) {
	for (Object entryO : map.entrySet()) {
	    Map.Entry entry = (Map.Entry) entryO;
	    Quad from = (Quad) entry.getKey();
	    Quad to = (Quad) entry.getValue();

	    if (from instanceof NEW) { 
		System.out.println("  " +Util.code2str(from) + " | " +
				   from.hashCode() + " -> ");
		System.out.println("  " +Util.code2str(to) + " | " +
				   to.hashCode());
	    }
	}
    }
          

    private Map getTypeCountMap(Collection temps) {
        Map count = new HashMap();

        for (Object tempO : temps) {
            Temp temp = (Temp) tempO;
            HClass type = ((NEW) globalAllocMap.get(temp)).hclass();
            if (count.containsKey(type)) {
                Integer inMap = (Integer) count.get(type);
                count.put(type, new Integer(inMap.intValue() + 1));
            }
            else {
                count.put(type, new Integer(1));
            }
        }

        return count;
    }

    private void printTypeStatistics(Collection temps) {
        printTypeStatistics(temps, null);
    }

    private void printTypeStatistics(Collection temps, Collection correl) {
        final Map count = getTypeCountMap(temps);
        Map count_correl = correl != null ?
            getTypeCountMap(correl) : null;
        
        // sort
        Object[] types = count.keySet().toArray();

        Arrays.sort(types, new Comparator() {
                public int compare(Object a, Object b) {
                    Integer ac = (Integer) count.get(a);
                    Integer bc = (Integer) count.get(b);
                    return ac.compareTo(bc);
                };
            });

        int total = 0;
        for (int i = 0; i<types.length; i++) {
            int c_this = ((Integer) count.get(types[i])).intValue();
            System.out.print("   " + types[i] + ": " + c_this);
            
            if (correl != null) {
                int c_correl =
                    ((Integer) count_correl.get(types[i])).intValue();
                System.out.println("  (of " + c_correl + ", " +
                                   c_this*100/c_correl + "%)");
            } else System.out.println();
            
            total+= c_this;
        }
        System.out.println("   total: " + total);

    }

    public static long sizeStatistics(Collection methods, HCodeFactory factory) {
        long nElements = 0;
        for (Object methodO : methods) {
            HMethod method = (HMethod) methodO;
            HCode hc = factory.convert(method);

            if (hc!=null)
                nElements+= hc.getElementsL().size();
        }

        return nElements;
    }

    // some helper methods for debugging
    
    private boolean tempIsA(Object temp, HClass hclass) {
        if (globalAllocMap.containsKey(temp)) {
            return isA(((NEW) globalAllocMap.get(temp)).hclass(), hclass);
        } else return false;
    }

    // i would think hclass would have something like this...
    private static boolean isA(HClass hclass, HClass what) {
        if (what == null) return false;
        
        if (what.isInterface())
            return what.isSuperinterfaceOf(hclass);
        else 
            return what.isSuperclassOf(hclass);
    }



    private void printTempCollection(Collection temps) {
        for (Object allocO : temps) {
            Temp alloc = (Temp) allocO;

            if (I.getValues(alloc).isEmpty()) continue;

            System.out.println("   " + alloc + ": "
                               + Util.code2str((NEW) globalAllocMap.get(alloc)));
        }
    }
    
    private NEW allocQuad(Temp temp) {
        if (globalAllocMap.containsKey(temp)) {
            return (NEW) globalAllocMap.get(temp);
        } else return null;
    }
    

    private class TypeSwitchCondition implements Condition {
        TYPESWITCH typeswitch;
        int branch;
        
        TypeSwitchCondition(TYPESWITCH typeswitch, int branch) {
            this.typeswitch = typeswitch;
            this.branch = branch;
        }

        public boolean isSatisfiedFor(Object object) {
            if (!(object instanceof Temp)) return false;

            Object alloc = globalAllocMap.get(object);

            // if not allocated, satisfies
            if (alloc == null) return true;

            HClass hclass = ((NEW) alloc).hclass();

            // default?
            // asume default branch is the last, as is apparently the case now
            if (typeswitch.hasDefault() &&
                branch == typeswitch.keysLength()) {
                // yes...
                // then hclass must not extend any of the keys
                
                for (int i = 0; i<typeswitch.keysLength(); i++)
                    if (isA(hclass, typeswitch.keys(i)))
                        return false;

                return true;
            } else {
                // no...
                // then hclass must extend the corresponding key
                return isA(hclass, typeswitch.keys(branch));
            }

        }
        
        public String toString() {
            return "{branch " + branch + " of " + typeswitch + "}";
        }
    }

}

class MethodData {
    // This is the data we keep for each method

    // **** initial (static) data

    // if this is set, everything else should be ignored
    boolean isNative = false;
    
    // maps edges to sets of live allocations
    MultiMap liveness;

    // Interesting nodes that can reach normal / exceptional returns
    Set reachNormal;
    Set reachEx;

    // maps call sites to possibly called methods, for ease of use
    MultiMap calls;

    // these are vars that are internal and should not appear in returns
    Set internals;
    // guess what these are
    Set externals;

    // maps internals to the condition, if any, under which an object
    //    is assigned to an internal (think TYPESWITCH)
    Map conditions;

    MultiMap param2calls;

    Collection allocationSites;
    Collection escapeSites;

    // the header of this method
    METHOD header;

    // **** data that will be constructed through fixed-point

    // helper information: pointsTo information for internals
    // maps internal variables to possible values
    MultiMap aliasedValues;
    // inverse of the above. has to be kept in sync
    MultiMap aliasedAssigns;
    
    Set An, Ae, Rn, Re, E;

    // parameter incompatibilities
    MultiMap Ip;
}

// FIXME: move this outside
//   ... or maybe not. Does anybody else need it?
interface Condition {
    public boolean isSatisfiedFor(Object object);
}



        




