Purity Analysis Kit v0.00
Copyright (c) 2005 - Alexandru Salcianu <salcianu@alum.mit.edu>
GNU General Public Licence


TABLE OF CONTENTS:
 A. DEFINITION OF PURITY
 B. FILES
 C. TESTING
 D. USAGE
 E. PROGRAMMATIC ACCESS TO THE ANALYSIS RESULTS


A. DEFINITION OF PURITY
-----------------------

Our analysis considers a method pure if it does not mutate any object
that existed before the method was invoked; this is also the
definition used in JML.  Notice that a method that is pure according
to this definition may still create, mutate and even return new
objects.  The reason JML uses this definition is that if a method does
not mutate any pre-existent object, the invariants attached to those
objects propagate over calls to that method.  In addition, as a
special case, our (and JML's) definition of purity allows a pure
constructor to mutate fields of the "this" object.



B. FILES
----------------------

The purity analysis kit consists of a .tgz archive file.  It unzips
into a directory purity-kit, containg the following files/subdirs:

1. purity.jar - Contains the classes of the purity analysis, together
   with the rest of the FLEX compiler infrastructure.  The Java source
   code files are available from the FLEX website (get the latest CVS
   snapshot):

   http://flex-master.csail.mit.edu/Harpoon/

2. aux/ - Subdirectory with the two .jars used by the implementation of
   the purity analysis: jpaul.jar and jutil.jar.  These files are not
   really part of the purity analysis: they are provided here only for
   your convenience.  You may also find them, together with their
   sources and Javadoc documentation, on the websites of their
   corresponding projects:

   http://jpaul.sourceforge.net/
   http://cscott.net/Projects/JUtil/jutil-latest/doc/

3. lib/ - Subdirectory that contains the implementation of the Java
   standard library that FLEX compiles/analyzes Java applications
   against.

   The analyzed application consists of (1) the user code, and (2) the
   standard library from lib (actually, only those parts of (1) and
   (2) that are transitively callable from the main method).  Notice
   that the Java standard library from (2) may be different from the
   Java standard library used by your JVM.

   Currently, we use GNU Classpath 0.08 (glibj.zip); we also have two
   .jars that reconcile GNU Classpath with the object layout used by
   our compiler.  Not sure they are really necessary for the purity
   analyses, but better safe than sorry :)

   The full code of the GNU Classpath 0.08 can be found at

   http://www.gnu.org/software/classpath

4. jolden/ - Subdirectory containing the Java Olden bechmarks.  These
   applications are not part of the purity analysis: they are provided
   here only as testcases.

5. COPYING - The GNU GPL Licence Details.  Please read it and
   discontinue using this purity analysis kit if you have any problems
   witth the GNU GPL.

6. README.txt - The file you are reading right now.



C. TESTING
----------

Once you have all the files in place,

  cd purity-kit

and execute 

  ./purity-test

This script should analyze two Java Olden benchmarks from jolden/ and
generate a lot of output.  At the end of the analysis of each
application, the analysis will print a short statistic regarding the
total number of pure methods.  If everything goes fine, the analysis
should terminate (relatively fast, only a few minutes) on all Java
Olden applications.  If you want to analyze all of the ten Java Olden
benchmarks, execute

  ./purity-test all

./purity-test creates a "results" subdirectory containing files with
the analysis trace for each analyzed testcase.



D. USAGE
--------

[ Before using the purity analysis, you should make sure that
purity.jar, aux/jpaul.jar and aux/jutil.jar are on your JVM's
CLASSPATH. ]

The entry point for the purity analysis is the method 

  public static void harpoon.Main.Purity.main(String[] args)

args[0] - main class of the application you want to analyze.

args[1] - path where your application resides.  As any Java CLASSPATH,
this path can contain several elements (directors, .jar files, and
.zip files), separated by ":".  The analysis will load the analyzed
classes from this path and from the GNU classpath implementation of
the Java standard library (if your application invokes the standard
library); you do NOT need to pass the path to GNU classpath: we use
the <code>glibj.zip</code> included in the purity analysis kit.

The rest of the arguments are passed verbatim to
harpoon.Main.SAMain.main(String[] args)

[ WARNING: you can take a look at the ./purity-test script, but it is
more complex than what you actually need.  The reason for its
complexity is that I also use it for testing while doing development
in the Flex project, and the locations of certains elements are
different than in the kit. ]



E. PROGRAMMATIC ACCESS TO THE ANALYSIS RESULTS
----------------------------------------------

The purity analysis examines (almost) all methods that may be
transitively callable from the main method of the application (i.e.,
the "main" method of the main class).  After the analysis terminates,
all detected pure methods are put in a set stored in a static field in
the class harpoon.Analysis.PA2.Mutation.WPMutationAnalysisCompStage:

  public static Set<HMethod> pureMethods;

[ We say "almost" because, for efficiency reasons, the analysis skips
(i.e., considers unanalyzable) several large library methods, whose
analysis takes a long time and does not seem to produce anything
useful.  Those methods deal with the SecurityManager and the time
zones.  Let me know if you really care about them: the analysis can
analyze them, at the cost of a bigger analysis time. ]

Also, the analysis stores a map from each analyzed method to a list of
the indices of the "safe" parameters.  A parameter is safe if, for any
object transitively reachable from that parameters, (1) the method
does not mutate it, and (2) the method does not create new
externally-visible aliasing to that object (this precludes storing
references to such an object into a static, or returning such an
object).  This map is stored in another static field of 
harpoon.Analysis.PA2.Mutation.WPMutationAnalysisCompStage:

  public static Map<HMethod,List<Integer>> method2SafeParam;

The parameter indices start from 0 and take into account all
parameters, including those of primitive types, and, in the case of
instance methods, the implicit this argument (the first one, i.e.,
#0).

harpoon.ClassFile.HMethod is the FLEX handle for a method from the
analyzed code.  Full Javadoc available on the FLEX website

  http://flex-master.csail.mit.edu/Harpoon

or directly from

  http://flex-master.csail.mit.edu/Harpoon/doc/harpoon/ClassFile/HMethod.html

Good luck!