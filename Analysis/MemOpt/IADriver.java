package harpoon.Analysis.MemOpt;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;

import harpoon.IR.Quads.QuadSSI;

// alex's metacallgraph
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.Quads.CallGraph;

import harpoon.Util.Timer;

import java.util.*;

/**
 * <code>IADriver</code> contains a test harness for <code>IncompatibilityAnalysis</code>. It is also meant as an usage example.
 * Run it with two arguments: the class name and the method name.
 *
 * @author <a href="mailto:Ovidiu Gheorghioiu <ovy@mit.edu>">ovy</a>
 * @version 1.0
 */


public class IADriver {
    public static final boolean PREBUILD_SSI = true;

    public static void main(String args[]) {
        String defaultClassName = "MemTest";
        String defaultMethodName = "main";

        String className, methodName;

        // check args
        if (args.length > 2) {
            System.out.println("Usage: java IncompatibilityAnalysis [className] [methodName]");
            System.exit(1);
        }

        // find default args
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

        // our linker
        Linker linker = Loader.systemLinker;

        // load class
 	HClass hclass = linker.forName(className);
        HMethod[] methods = hclass.getDeclaredMethods();

        // find specified methods
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
        


        // build call graph using alex's SmartCallGraph
        //   this is std building code for SCG

        
        // first, we get the methods
        // code factory
        HCodeFactory hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory();
        // caching is IMPORTANT
        hcf = new CachingCodeFactory(hcf, true);

        // root methods
        Set mroots = createRoots(entry);

        System.out.println("Creating class hierarchy...");
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

        // enable below to use Alex's collections hack (unsafe as of yet)
        // MetaCallGraphImpl.COLL_HACK = true;

        // build call graph
        CallGraph cg = new SmartCallGraph((CachingCodeFactory) hcf, linker, ch,
                                          mroots);

        // IA needs SSI
        HCodeFactory hcf_ssi =
            new CachingCodeFactory(harpoon.IR.Quads.QuadSSI.codeFactory(hcf));
        
        // this is IMPORTANT
        QuadSSI.KEEP_QUAD_MAP_HACK = true;

        Timer timer;

         if (PREBUILD_SSI) {
            System.out.println("Prebuilding SSI...");
            timer = new Timer();
            timer.start();

            IncompatibilityAnalysis.sizeStatistics(cg.callableMethods(), hcf_ssi);
            
            timer.stop();
            System.out.println("SSI prebuild: " + timer);
        }

         IncompatibilityAnalysis analysis =
             new IncompatibilityAnalysis(entry, hcf_ssi, cg);
        
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
    
}
