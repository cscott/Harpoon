package harpoon.Analysis.MemOpt;


import harpoon.ClassFile.*;
import java.util.*;
import harpoon.IR.Quads.*;
import harpoon.IR.Properties.*;
import harpoon.Analysis.*;
import harpoon.Analysis.Quads.*;

import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;

// alex's metacallgraph
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.Quads.CallGraph;

import harpoon.Analysis.LowQuad.Loop.*;

import harpoon.Temp.*;

import harpoon.Util.Util;
import harpoon.Util.Collections.*;
import harpoon.Util.Grapher;

import harpoon.Analysis.PointerAnalysis.Debug;


public class IncompatibilityAnalysis {

    // hacks
    public static final boolean STAY_IN_DECLARING_CLASS = false;
    public static final boolean TO_STRING_ESCAPES_NOTHING = false;
    public static final boolean REMOVE_RE_HACK = false;
    public static final boolean TO_STRING_SUCKS = false;
    public static final boolean AN_EQUALS_AE_HACK = false;

    // static objects used for statistics only
    private static HClass exception;
    private static HClass iterator;

    // test case
    public static void main(String args[]) {
        String defaultClassName = "MemTest";
        String defaultMethodName = "main";

        String className, methodName;

        // check args;

        if (args.length > 2) {
            System.out.println("Usage: java IncompatibilityAnalysis [className] [methodName]");
            System.exit(1);
        }
        
        if (args.length == 0) {
            System.out.println("No class specified, using default: "
                               + defaultClassName);
            className = defaultClassName;
        } else className = args[0];

        if (args.length <= 1) {
            System.out.println("No method specified, using default: "
                               + defaultMethodName);
            methodName = defaultMethodName;
        } else methodName = args[1];
        
        Linker linker = Loader.systemLinker;
        exception = linker.forName("java.lang.Exception");
        iterator = linker.forName("java.util.Iterator");

 	HClass hclass = linker.forName(className);

        HMethod[] methods = hclass.getDeclaredMethods();
        HMethod entry = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName ().equals(methodName)){
                entry = methods[i];
                break;
            }
        }

        if (entry == null) {
            System.out.println("Give me a method called " + methodName +
			       " please");
            return;
        }
        


        // call graph building code
        System.out.println("Building call graph...");

        HCodeFactory hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory();
        // caching is IMPORTANT
        hcf = new CachingCodeFactory(hcf, true);

        Set mroots = createRoots(entry);

        ClassHierarchy ch = new QuadClassHierarchy(linker,
                                                  mroots,
                                                  hcf);

        // now add static initializers;
        for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
            HClass hcl = (HClass) it.next();
            HMethod hm = hcl.getClassInitializer();
            if (hm != null)
                mroots.add(hm);
        }

        

        // final call graph
        CallGraph cg = new SmartCallGraph((CachingCodeFactory) hcf, ch,
                                          mroots);

        System.out.println("Done building.");

        HCodeFactory hcf_ssi = new CachingCodeFactory(harpoon.IR.Quads.QuadSSI.codeFactory(hcf));
        // HCodeFactory hcf_ssi = hcf;

        QuadSSI.KEEP_QUAD_MAP_HACK = true;
        IncompatibilityAnalysis analysis = new IncompatibilityAnalysis(entry, hcf_ssi,
                                                           cg);

        analysis.printSomething();
        
   }

    private static Set createRoots(HMethod entry) {
        // for thorough example see SAMain.java
        Set roots = new HashSet();
        // ask the runtime which roots it requires.
        harpoon.Backend.Generic.Frame frame = 
	    new harpoon.Backend.StrongARM.Frame(entry);

        roots.addAll(frame.getRuntime().runtimeCallableMethods());

        roots.add(entry);

        // filter out things that are not hmethods
        for (Iterator it = roots.iterator(); it.hasNext(); ) {
            Object atom = it.next();
            if (!(atom instanceof HMethod)) it.remove();
        }
        
        // should perhaps add tests/includes for other infrastructure hacks
        
        return roots;
    }

    private Map mdCache; // cache of method data objects

    private MultiMap callees;
    
    private HMethod entry;
    private HCodeFactory codeFactory;
    private CallGraph callGraph;

    // FIXME: change to private
    public LinkedList allMethods;

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
    private Set selfIncompatible;

    // interface to the outside world

    public IncompatibilityAnalysis(HMethod entry, HCodeFactory codeFactory,
				   CallGraph callGraph) {

        // init

        this.entry = entry;
        this.codeFactory = codeFactory;
        this.callGraph = callGraph;

        globalAllocMap = new HashMap();

        // stage 0: static analysis of methods for internal liveness & rest
        intraproceduralAnalysis();
        
        // stage 1: compute An, Ae, Rn, Re, Esc
        computeInitialSets();

        // stage 2: compute I
        computeI();

        // stage 3: do compatible classes
        computeClasses();

        // FIXME: destroy unneeded data here (mainly mdCache)
    }

    // returns true if the given SSI NEW quad is self-incompatible
    //    i.e. it cannot be statically allocated
    public boolean isSelfIncompatible(HCodeElement e) {
        assert e instanceof NEW : "allocations are NEW quads";

        NEW qNew = (NEW) e;
        Temp alloc = qNew.dst();

        return I.contains(alloc, alloc) || !globalAllocMap.containsKey(alloc);
    }

    // returns true if the given SSI NEW quads are incompatible
    public boolean isIncompatible(HCodeElement e1, HCodeElement e2) {
        assert e1 instanceof NEW && e2 instanceof NEW :
            "allocations are NEW quads";

        NEW qNew1 = (NEW) e1; NEW qNew2 = (NEW) e2;
        Temp alloc1 = qNew1.dst(); Temp alloc2 = qNew2.dst();

        return I.contains(alloc1, alloc2);
    }

    // returns a collection whose members are collections of compatible
    //    NEW quads, which can then be statically allocated in the same slot
    // all NEW quads in the methods analyzed are in one of these collections,
    //    unless they are self-incompatible
    // these are all in SSI form
    public Collection getCompatibleClasses() {
        LinkedList allocClasses = new LinkedList();
        for (Iterator it = classes.iterator(); it.hasNext(); ) {
            Collection thisClass = (Collection) it.next();
            Collection newClass = new LinkedList();
            
            for (Iterator it2 = thisClass.iterator(); it2.hasNext(); ) {
                newClass.add(globalAllocMap.get(it2.next()));
            }

            allocClasses.add(newClass);
        }

        return allocClasses;
    }

    // takes a Quad in QuadNoSSA, returns the equivalent quad in QuadSSI
    //   or null if one doesn't exist
    // the result is compatible with the quads the method above
    //   operate on, provided that the CodeFactory we were passed is caching
    // this will only work if the SSI CodeFactory we were passed is based
    //   on the caching QuadNoSSA factory which generated this code, otherwise
    //   it will almost always return null
    public Quad getSSIQuad(Quad q) {
        HMethod method  = q.getFactory().getMethod();
        QuadSSI quadssi = (QuadSSI) codeFactory.convert(method);

        Quad retval = (Quad) quadssi.getQuadMap().get(q);

        // debug, remove me pls
        assert retval != null : "didn't find a quad ssi for " + q;

        return retval;
    }
    

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

            for (Iterator it = md.calls.keySet().iterator(); it.hasNext(); ) {
                CALL qCall = (CALL) it.next();
                Collection possibleCalls = md.calls.getValues(qCall);

                for (Iterator it2 = possibleCalls.iterator(); it2.hasNext();) {
                    HMethod called = (HMethod) it2.next();

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
        System.out.println("Analyzing " + method);
        
        // what we need
        HCode hcode = codeFactory.convert(method);
        if (hcode == null ||
            TO_STRING_SUCKS && method.getName().equals("toString")) {
            System.out.println("    native... ignoring");
            MethodData md = new MethodData();
            // return fake md
            md.isNative = true;
            return md;
        }

         // get ssi2nossa quadmap
         Map quadSSI2NoSSA = new LinearMap(hcode.getElementsL().size());


         Map quadMap = ((QuadSSI) hcode).getQuadMap();
         // invert quadmap from ssiRename
         for (Iterator it = quadMap.entrySet().iterator();
              it.hasNext(); ) {
             Map.Entry entry = (Map.Entry) it.next();
             quadSSI2NoSSA.put(entry.getValue(), entry.getKey());
         }
        
        // ExactTypeMap typeMap = new SCCAnalysis(hcode);
        // ReachingDefs rd= new SSxReachingDefsImpl(hcode);

        // will construct these
        // I can certainly do with less objects, but dev time is the constraint
        //   now. Will optimize if needed, I promise.
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

        // Map defMap = new HashMap();

        MultiMap param2calls = new GenericMultiMap(new AggregateSetFactory());
        
        METHOD header = null;
                
        MultiMap calls = new GenericMultiMap(new AggregateSetFactory());
        MultiMap liveness = new GenericMultiMap(new AggregateSetFactory());

        // helpers for liveness
	// edges for which we are interested in liveness & reachability
        Set interestingEdges = new HashSet(); 

        // direct assignments: which temps get assigned to this temp
        MultiMap assignMap = new GenericMultiMap(new AggregateSetFactory());

        // traverse the graph. I should really implement this thru a Visitor...
        for (Iterator it = hcode.getElementsI(); it.hasNext(); ) {
            Quad q = (Quad) it.next();

            // for (Iterator it2 = q.defC().iterator(); it2.hasNext(); ) {
            //    defMap.put(it2.next(), q);
            // }

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

		CALL qCallNoSSA = (CALL) quadSSI2NoSSA.get(qCall);
		assert qCallNoSSA != null;

                HMethod[] calledMethods = callGraph.calls(method, qCallNoSSA);

                if (calledMethods.length == 0) {
                    System.out.println("*** Warning: no methods found for: "
                                       + Debug.code2str(qCall));
                }

                calls.addAll(qCall, Arrays.asList(calledMethods));

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
            
            
           
            // SIGMA stuff
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
        //   here
        Collection interestingTemps = new ArrayList(externals.size()
                                                    + internals.size());
        interestingTemps.addAll(internals);
        interestingTemps.addAll(externals);

        // we are only interested in variables that get assigned the above,
        //    minus call params and escapes
        Set interestingRoots = new HashSet(allocations);
        interestingRoots.addAll(Arrays.asList(header.params()));
        interestingRoots.addAll(callReturns);
        interestingRoots.addAll(conditions.keySet());
                               

        // we are only interested in assignments between interesting temps
        
        // close assignMap as per the interesting roots
        //   but: do *NOT* allow propagation over conditionals
        //   *THAT* propagation is assigned-object-dependent

        // System.out.println("assignMap before closure: " + assignMap);
        assignMap = MultiMapUtils.multiMapClosure(assignMap, interestingRoots,
                                                  conditions.keySet());
        // System.out.println("assignMap after closure: " + assignMap);

        
        // compute reverse mapping of vars to possible values
        MultiMap valuesMap = MultiMapUtils.multiMapInvert(assignMap, interestingRoots);

        
        SSILiveness ssiLiveness = new SSILiveness(hcode);

        // retain the liveness info we need
        // i.e., for interestingTemps and interestingEdges, via valuesMap
        for (Iterator it = interestingEdges.iterator(); it.hasNext(); ) {
            Edge edge = (Edge) it.next();

            Set lvOn = ssiLiveness.getLiveOn(edge);

            for (Iterator it2 = lvOn.iterator(); it2.hasNext(); ) {
                Temp temp = (Temp) it2.next();

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
        
        // Keep interesting nodes, ie, nodes after calls
        md.reachNormal = new HashSet();
        md.reachEx = new HashSet();

        for (Iterator it = interestingEdges.iterator(); it.hasNext(); ) {
            Edge edge = (Edge) it.next();
            Quad q = (Quad) edge.toCFG();

            if (reachNormal.contains(q)) md.reachNormal.add(q);
            if (reachEx.contains(q)) md.reachEx.add(q);
        }
        
        // initial values of dynamic sets
        MultiMap aliasedValues = MultiMapUtils.multiMapFilter(valuesMap, interestingTemps, interestingTemps);
    
        md.aliasedValues = aliasedValues;
        md.aliasedAssigns = MultiMapUtils.multiMapInvert(aliasedValues, interestingTemps);

        // sanity check. take this out when sure the code works right
        // assert MultiMapUtils.intersect(md.aliasedValues.keySet(,
        //                                     externals) == null);

        
        md.An = new HashSet();
        md.Ae = AN_EQUALS_AE_HACK ? md.An : new HashSet();

        for (Iterator it = allocationSites.iterator(); it.hasNext(); ) {
            NEW qNew = (NEW) it.next();

            if (reachNormal.contains(qNew)) md.An.add(qNew.dst());
            if (reachEx.contains(qNew)) md.Ae.add(qNew.dst());
        }

        // simulate pointsTo info from what we have
        MultiMap pointsTo = new GenericMultiMap(new AggregateSetFactory());
        for (Iterator it = externals.iterator(); it.hasNext(); ) {
            Temp temp = (Temp) it.next();
            pointsTo.add(temp, temp);
        }

        // propagate pointsTo by simulating deltas
        propagateExternalDeltas(md, pointsTo);
        
        md.Rn = new HashSet(pointsTo.getValues(RETVAL));
        md.Re = new HashSet(pointsTo.getValues(RETEX));
        if (REMOVE_RE_HACK) {
            md.An.removeAll(md.Re);
        }

            
        md.E = new HashSet(pointsTo.getValues(ESCAPE));

        if (TO_STRING_ESCAPES_NOTHING &&
            method.getName().equals("toString")) {
            md.E.clear();
            System.out.println("  Clearing toString escapes...");
        }

        // done, hopefully
        return md;
        
        
    }


    private CALL callMapHeuristic(CALL call, Map quadMap) {
        return call;
    }

    // computes all quads from which the given sites are reachable
    // takes O(Nacc), where Nacc is the number of edges that can reach "sites"
    private Set canReach(Collection sites, boolean addInitial) {
        Set canreach = addInitial? new HashSet(sites) : new HashSet();
        WorkSet workset = new WorkSet(sites);
        
        while (!workset.isEmpty()) {
            Quad q = (Quad) workset.removeFirst();

            for (int i = 0; i<q.prevLength(); i++) {
                Quad prev = (Quad)  q.prevEdge(i).fromCFG();

                if (canreach.add(prev)) {
                    workset.add(prev);
                }
            }
        }

        return canreach;
    }

    // Fixed-point set computation for the 5 sets we need
    private void computeInitialSets() {

        System.out.print("Fixed point set (Ax, Rx, E)");
        WorkSet workset = new WorkSet();

        workset.addAll(allMethods);

        while (!workset.isEmpty()) {
            HMethod method = (HMethod) workset.removeFirst();

            if (recomputeInitialSets(method)) {
                for (Iterator it = callees.getValues(method).iterator(); it.hasNext(); ) {
                    HMethod callee = (HMethod) it.next();
                    workset.addLast(callee);
                }
            }
        }
        System.out.println();
    }

    private boolean recomputeInitialSets(HMethod method) {
        System.out.print(".");
        MethodData md = (MethodData) mdCache.get(method);
        assert !md.isNative;

        int anSize = md.An.size(); int aeSize = md.Ae.size();
        int rnSize = md.Rn.size(); int reSize = md.Re.size();
        int eSize = md.E.size();

        // This is terribly, terribly inefficient
        // Should really be using deltas...

        for (Iterator it = md.calls.keySet().iterator(); it.hasNext(); ) {
            CALL qCall = (CALL) it.next();
            boolean reachNN = md.reachNormal.contains(qCall.nextEdge(0).to());
            boolean reachNE = md.reachEx.contains(qCall.nextEdge(0).to());
            boolean reachEN = md.reachNormal.contains(qCall.nextEdge(1).to());
            boolean reachEE = md.reachEx.contains(qCall.nextEdge(1).to());

            // sanity check
            assert  (reachNN || reachNE) && (reachEN || reachEE) :
                         "Must have a way of getting out of the method (at " + Debug.code2str(qCall)+")";

            
            for (Iterator it2 = md.calls.getValues(qCall).iterator(); it2.hasNext(); ) {
                HMethod called = (HMethod) it2.next();
                MethodData mdCalled = (MethodData) mdCache.get(called);

                if (mdCalled == null || mdCalled.isNative) continue;

                // do An, Ae changes
                if (reachNN) md.An.addAll(mdCalled.An);
                if (reachNE) md.Ae.addAll(mdCalled.An);
                if (reachEN) md.An.addAll(mdCalled.Ae);
                if (reachEE) md.Ae.addAll(mdCalled.Ae);

                // System.out.println("  after adding " + called + ": An = " + md.An + "; Ae = "+md.Ae + " (called An: " + mdCalled.An + "; called Ae: " + mdCalled.Ae+"; reachEE is " + reachEE+")");
                
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
            
            // md.aliasedValues = MultiMapUtils.multiMapClosure(md.aliasedValues,
            //                                                 md.aliasedValues.keySet());
            // md.aliasedReturns = MultiMapUtils.multiMapInvert(md.aliasedValues,
            //                                               md.aliasedValues.keySet());
            // if we do deltas, we should re-computePointsTo here, or use
            //    just deltas if aliases haven't changed
            // but we do pointsTo anyway (for now; inefficient)
            System.out.print("o");
        }

        propagateExternalDeltas(md, externalPointsTo);


        
        // finally, add any deltas to final variables to the final sets
        md.Rn.addAll(externalPointsTo.getValues(RETVAL));
        md.Re.addAll(externalPointsTo.getValues(RETEX));
        if (REMOVE_RE_HACK) {
            md.An.removeAll(md.Re);
        }
        md.E.addAll(externalPointsTo.getValues(ESCAPE));

        if (TO_STRING_ESCAPES_NOTHING &&
            method.getName().equals("toString")) {
            md.E.clear();
        }


//         for (Iterator it = externalPointsTo.getValues(ESCAPE).iterator();
//              it.hasNext(); ) {

//             Object atom = it.next();
//             Object inMap = globalAllocMap.get(atom);

//             if (inMap == null) continue;

//             NEW qNew = (NEW) inMap;

//             if (exception.isSuperclassOf(qNew.hclass())) {
//                 System.out.println("exception escaped: " + Debug.code2str(qNew) + " in " +
//                                    method);

//                 System.out.println("variables equal to this: " );

//                 for (Iterator it2 = externalPointsTo.keySet().iterator();
//                      it2.hasNext(); ) {
//                     Object var = it2.next();
//                     if (externalPointsTo.getValues(var).contains(atom)) {
//                         System.out.println("  " + var + " (parameter of: " + md.param2calls.getValues(var));
//                     }
//                 }
                
                
//                 HCode hcode = codeFactory.convert(method);
//                 hcode.print(new java.io.PrintWriter(System.out));

//                 System.exit(1);
//             }
            
//         }
             
        
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
        for (Iterator it = md.externals.iterator(); it.hasNext(); ) {
            Temp temp = (Temp) it.next();
            externalPointsTo.add(temp, temp);
        }
        
        for (Iterator it = md.calls.keySet().iterator(); it.hasNext(); ) {
            CALL qCall = (CALL) it.next();
            Temp [] actParams = qCall.params();
            for (Iterator it2 = md.calls.getValues(qCall).iterator(); it2.hasNext(); ) {
                HMethod called = (HMethod) it2.next();
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
                    Condition condition = (Condition) md.conditions.get(assign);

                    // filter deltas through the condition
                    for (Iterator it2 = thisDelta.iterator(); it2.hasNext(); ) {
                        Temp temp = (Temp) it2.next();
                        if (condition.isSatisfiedFor(temp)) {
                            changed = deltas.add(assign, temp) || changed;
                        } else {

                        }
                    }
                } else {
                    // otherwise, add the whole delta
                    changed = deltas.addAll(assign, thisDelta) || changed;
                }

                // System.out.println("  propagate: adding: " + assign);
                if (changed) workset.addLast(assign);
            }
        }
        // System.out.println("Done propagate");
        
    }

    private void computeI() {

        I = new GenericMultiMap();

        System.out.print("Computing initial Is");
        
        for (Iterator it = allMethods.iterator(); it.hasNext(); ) {
            HMethod method = (HMethod) it.next();

            computeInitialI(method);
        }
        
        // fixed point set analysis for I
        // this is strinkingly similar to the one in stage 1
        //    we should really have a common interface of sorts
        WorkSet workset = new WorkSet();

        workset.addAll(allMethods);

        System.out.println();
        System.out.print("Fixed point set (I)");

        while (!workset.isEmpty()) {
            HMethod method = (HMethod) workset.removeFirst();

            if (recomputeI(method)) {
                for (Iterator it = callees.getValues(method).iterator(); it.hasNext(); ) {
                    HMethod callee = (HMethod) it.next();
                    workset.addLast(callee);
                }
            }
        }

        // finally, make sure I is symmetric
        MultiMapUtils.ensureSymmetric(I);
        System.out.println();
        
    }

    private void computeInitialI(HMethod method) {
        System.out.print(".");
        MethodData md = (MethodData) mdCache.get(method);
        if (md == null || md.isNative) return;

        Collection params = Arrays.asList(md.header.params());


        // init Ip
        md.Ip = new GenericMultiMap(new AggregateSetFactory());

        // compute pointsTo information
        MultiMap pointsTo = new GenericMultiMap();
        computePointsTo(md, pointsTo, null);
        propagateExternalDeltas(md, pointsTo);

        // compute "meta allocation nodes" 
        Collection interestingNodes = new ArrayList(md.allocationSites.size() + md.calls.keySet().size());
        interestingNodes.addAll(md.allocationSites);
        interestingNodes.addAll(md.calls.keySet());
        
        // compute mappings from escape sites to things that escape there
        MultiMap escapes = new GenericMultiMap();
        for (Iterator it = md.escapeSites.iterator(); it.hasNext(); ) {
            Quad q = (Quad) it.next();
            if (q.kind() == QuadKind.SET) {
                SET qSet = (SET) q;
                escapes.addAll(qSet, pointsTo.getValues(qSet.src()));
            } else if (q.kind() == QuadKind.ASET) {
                ASET qASet = (ASET) q;
                escapes.addAll(qASet, pointsTo.getValues(qASet.src()));
            }
        }

        for (Iterator it = md.calls.keySet().iterator(); it.hasNext(); ) {
            CALL qCall = (CALL) it.next();
            Temp[] actParams = qCall.params();

            for (Iterator it2 = md.calls.getValues(qCall).iterator();
                 it2.hasNext(); ) {
                HMethod called = (HMethod) it2.next();
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
                    

        // for each meta alloc edge:
        for (Iterator it = interestingNodes.iterator(); it.hasNext(); ) {
            Quad q = (Quad) it.next();

            // compute allocs here
            Set allocs;

            // allocs for outgoing edges
            Set[] edgeAllocs = new Set[q.nextLength()];
            
            if (q.kind() == QuadKind.NEW) {
                allocs = edgeAllocs[0] = Collections.singleton( ((NEW) q).dst());
            } else {
                assert q.kind() == QuadKind.CALL;
                
                edgeAllocs[0]= new HashSet();
                edgeAllocs[1] = new HashSet();
                
                for (Iterator it2 = md.calls.getValues(q).iterator(); it2.hasNext(); ) {
                    HMethod called = (HMethod) it2.next();
                    MethodData mdCalled = (MethodData) mdCache.get(called);

                    if (mdCalled == null || mdCalled.isNative) continue;

                    edgeAllocs[0].addAll(mdCalled.An);
                    edgeAllocs[1].addAll(mdCalled.Ae);
                }

                allocs = new HashSet(edgeAllocs[0].size()
                                     + edgeAllocs[1].size());
                allocs.addAll(edgeAllocs[0]);
                allocs.addAll(edgeAllocs[1]);
            }

            // add incompatibilities from escapes
            // compute reachability of this node (there are more efficient
            //   ways though...)

            Set canReachQ = canReach(Collections.singleton(q), false);

            // System.out.println("   canreachq: " + Debug.code2str(q) + " : " + canReachQ);
            
            for (Iterator it2 = escapes.keySet().iterator(); it2.hasNext(); ) {
                Quad qEscape = (Quad) it2.next();

                if (canReachQ.contains(qEscape)) {
                    // add incompatibilities between escaped vars and allocs
                    // the cartesian product scares me
                    for (Iterator it3 = escapes.getValues(qEscape).iterator(); it3.hasNext(); ) {
                        Temp escaped = (Temp) it3.next();

                        // System.out.println("escape incompat: " + escaped + " with " + allocs + " in method " + method);

                        if (params.contains(escaped)) {
                            md.Ip.addAll(escaped, allocs);
                        } else {
                            assert globalAllocMap.containsKey(escaped);
                            I.addAll(escaped, allocs);
                        }
                    }
                }
            }

            Collection liveInInternal = md.liveness.getValues(q.prevEdge(0));
            Set liveInObjects = MultiMapUtils.multiMapUnion(pointsTo,
                                                            liveInInternal);
                                                            

            // add incompatibilities from liveness
            // for each outgoing edge
            for (int i = 0; i<q.nextLength(); i++) {
                // edgeAllocs empty? no point going on
                if (edgeAllocs[i].isEmpty()) continue;
                
                Edge edge = q.nextEdge(i);

                Collection liveInternal = md.liveness.getValues(edge);
                Set liveObjects = MultiMapUtils.multiMapUnion(pointsTo,
                                                              liveInternal);

                Set liveOverObjects = MultiMapUtils.intersect(liveInObjects, liveObjects);


                // all live objects become incompatible with allocs here
                // again, a good ol' cartesian product
                for (Iterator it2 = liveOverObjects.iterator(); it2.hasNext(); ) {
                    Temp live = (Temp) it2.next();

                    if (params.contains(live)) {
                        md.Ip.addAll(live, edgeAllocs[i]);
                    } else {
                        // System.out.println("     adding to I..."); 
                        assert globalAllocMap.containsKey(live);

                        I.addAll(live, edgeAllocs[i]);
                    }
                }
            }

        }

    }


    public boolean recomputeI(HMethod method) {
        System.out.print(".");
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
        
        for (Iterator it = md.calls.keySet().iterator(); it.hasNext(); ) {
            CALL qCall = (CALL) it.next();
            Temp[] actParams = qCall.params();

            
            for (Iterator it2 = md.calls.getValues(qCall).iterator(); it2.hasNext(); ) {
                HMethod called = (HMethod) it2.next();
                MethodData mdCalled = (MethodData) mdCache.get(called);

                if (mdCalled == null || mdCalled.isNative) continue;
                Temp [] declParams = mdCalled.header.params();

                // replace params
                for (int i = 0; i<actParams.length; i++) {
                    if (mdCalled.Ip.containsKey(declParams[i])) {
                        for (Iterator it3 = pointsTo.getValues(actParams[i]).iterator(); it3.hasNext(); ) {
                            Temp temp = (Temp) it3.next();

                            if (params.contains(temp)) {
                                md.Ip.addAll(temp,
                                             mdCalled.Ip.getValues(declParams[i]));
                            }
                            else {
                                // System.out.println("Incompatibility: " + temp + " and " + mdCalled.Ip.getValues(declParams[i])+ " are incompatible on " + Debug.code2str(qCall) + " (method: " + method+")");
                                
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
        

    private void computeClasses() {
        Set selfCompatible = new HashSet();
        selfIncompatible = new HashSet();

        // compute self-compatibles and self-incompatibles
        for (Iterator it = globalAllocMap.keySet().iterator(); it.hasNext(); ) {
            Temp alloc = (Temp) it.next();

            if (I.contains(alloc, alloc)) {
                selfIncompatible.add(alloc);
            } else {
                selfCompatible.add(alloc);
            }

        }

        // MultiMap Itmp = MultiMapUtils.multiMapFilter(I, selfCompatible, selfCompatible);

        // classes = MultiMapUtils.computeCompatibleClasses(Itmp);

        classes = MyGraphColorer.colorGraph(selfCompatible, I);

        // System.out.println("Coloring via alt method: ");
        // Collection classesAlt = MultiMapUtils.computeCompatibleClassesAlt(Itmp);
     }
        
    public void printSomething() {
        // show off some end-results
        MethodData md = (MethodData) mdCache.get(entry);

        System.out.println("An for entry: " + md.An);        
        System.out.println("Ae for entry: " + md.Ae);

        HashSet allocs = new HashSet(md.An);
        allocs.add(md.Ae);

        System.out.println("Rn for entry: " + md.Rn);        
        System.out.println("Re for entry: " + md.Re);
        
        System.out.println("E for entry: " + md.E);

        // System.out.println("Liveness for entry: " + md.liveness);
        System.out.println("aliases for entry: " + md.aliasedValues);

        // System.out.println("Incompatibility: " + I);


        System.out.println("Classes: ");
        System.out.println(classes);

        System.out.println("selfIncompatible: " + selfIncompatible);

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
        for (Iterator it = classes.iterator(); it.hasNext(); ) {
            Collection thisClass = (Collection) it.next();
            nclass++;

            System.out.println(" *Class " + nclass);

            assert
                MultiMapUtils.intersect(thisClass, selfIncompatible).isEmpty();

            printTypeStatistics(thisClass);
        }

        
        System.out.println("Self-incompatible type statistics: ");
        printTypeStatistics(selfIncompatible);


        System.out.println("Statistics: ");
        System.out.println("   " + globalAllocMap.keySet().size() + " allocations");
        System.out.println("   " + I.size() + " incompatible pairs");
        System.out.println("   " + classes.size() + " classes ("
                           + (100 - (classes.size()*100/(globalAllocMap.keySet().size() - selfIncompatible.size()))) +"% reduction)");
        System.out.println("   " + selfIncompatible.size() + " self-incompatible vars ("+(selfIncompatible.size()*100/globalAllocMap.keySet().size())+"%)");
    }

    private void printmap(Map map) {
	for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    Quad from = (Quad) entry.getKey();
	    Quad to = (Quad) entry.getValue();

	    if (from instanceof NEW) { 
		System.out.println("  " +Debug.code2str(from) + " | " +
				   from.hashCode() + " -> ");
		System.out.println("  " +Debug.code2str(to) + " | " +
				   to.hashCode());
	    }
	}
    }

    private void printTypeStatistics(Collection temps) {
        final Map count = new HashMap();

        for (Iterator it = temps.iterator(); it.hasNext(); ) {
            Temp temp = (Temp) it.next();
            HClass type = ((NEW) globalAllocMap.get(temp)).hclass();
            if (count.containsKey(type)) {
                Integer inMap = (Integer) count.get(type);
                count.put(type, new Integer(inMap.intValue() + 1));
            }
            else {
                count.put(type, new Integer(1));
            }
        }


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
            System.out.println("   " + types[i] + ": " + count.get(types[i]));
            total+= ((Integer) count.get(types[i])).intValue();
        }
        System.out.println("   total: " + total);

    }

    // some helper methods for debugging
    
    private boolean isExceptionAlloc(Temp temp) {
        if (globalAllocMap.containsKey(temp)) {
            return exception.isSuperclassOf(((NEW) globalAllocMap.get(temp)).hclass());
        } else return false;
    }

    private boolean isIteratorAlloc(Temp temp) {
        if (globalAllocMap.containsKey(temp)) {
            return iterator.isSuperinterfaceOf(((NEW) globalAllocMap.get(temp)).hclass());
        } else return false;
    }

    private void printTempCollection(Collection temps) {
        for (Iterator it = temps.iterator(); it.hasNext(); ) {
            Temp alloc = (Temp) it.next();

            if (I.getValues(alloc).isEmpty()) continue;

            System.out.println("   " + alloc + ": "
                               + Debug.code2str((NEW) globalAllocMap.get(alloc)));
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
        
        // i would think hclass would have something like this...
        private boolean isA(HClass hclass, HClass what) {
            if (what.isInterface())
                return what.isSuperinterfaceOf(hclass);
            else 
                return what.isSuperclassOf(hclass);
        }

        public String toString() {
            return "{branch " + branch + " of " + typeswitch + "}";
        }
    }

}

class MethodData {
    // This is the data we keep for each method

    // initial (static) data


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
    Set externals;
    Set internals;

    // maps internals to the condition, if any, under which an object
    //    is assigned to an internal (think TYPESWITCH)
    Map conditions;

    MultiMap param2calls;

    Collection allocationSites;
    Collection escapeSites;

    // the header of this method
    METHOD header;

    // this is data that will be constructed through fixed-point

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
interface Condition {
    public boolean isSatisfiedFor(Object object);
}



        




