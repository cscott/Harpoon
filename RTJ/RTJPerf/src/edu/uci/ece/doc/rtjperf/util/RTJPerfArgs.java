// ************************************************************************
//    $Id: RTJPerfArgs.java,v 1.1 2002-07-02 15:55:07 wbeebee Exp $
// ************************************************************************
//
//                               RTJPerf
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.doc.rtjperf.util;

// -- jTools Import --
import edu.uci.ece.ac.jargo.ArgSpec;

/**
 * This is an utility class that contains a list of the command line
 * argument that are used by RTJPerf benchmark applications.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class RTJPerfArgs {

    private static final String THREAD_BOUND_STR               = "threadBoundHandler";
    private static final String NO_HEAP_STR                    = "noHeap";
    private static final String HANDLER_PRIORITY_STR           = "handlerPriority";
    private static final String FIRE_COUNT_STR                 = "fireCount";
    private static final String MEMORY_AREA_STR                = "memoryArea";
    private static final String OUT_DIR_STR                    = "outDir";
    private static final String MEM_PROFILE_STR                = "memProfiling";
    private static final String BACKGROUND_THREAD_NUMBER_STR   = "backgroundThreadNumber";
    private static final String BACKGROUND_THREAD_PRIORITY_STR = "backgroundThreadPriority";
    private static final String BACKGROUND_THREAD_TYPE_STR     = "backgroundThreadType";
    private static final String LP_ASYNC_HANDLER_NUMBER_STR    = "lpAsyncHandlerNumber";
    private static final String LP_ASYNC_HANDLER_PRIORITY_STR  = "lpAsyncHandlerPriority";
    private static final String MEM_SIZE_STR                   = "memSize";
    private static final String COUNT_STR                      = "count";
    private static final String SCOPED_MEMORY_TYPE_STR         = "scopedMemoryType";
    private static final String ALLOC_SIZE_STR                 = "allocSize";
    
    // -- Validators --
    private static final String[] PRIORITY_VALIDATOR =
        new String[] {"edu.uci.ece.doc.rtjperf.util.PriorityArgumentValidator"};

    private static final String[] NATURAL_NUM_VALIDATOR =
        new String[]{"edu.uci.ece.ac.jargo.NaturalValidator"};

    private static final String[] MEMORY_AREA_VALIDATORS =
            new String[]{"edu.uci.ece.doc.rtjperf.util.MemoryTypeArgumentValidator"};

    private static final String[] PATH_VALIDATOR =
        new String[] {"edu.uci.ece.ac.jargo.NullValidator"};

    private static final String[] SCOPED_MEMORY_TYPE_VALIDATOR =
        new String[] {"edu.uci.ece.doc.rtjperf.util.ScopedMemoryTypeValidator"};
    
    public static final ArgSpec THREAD_BOUND_OPT =
        new ArgSpec(THREAD_BOUND_STR);
    
    public static final ArgSpec NO_HEAP_OPT =
        new ArgSpec(NO_HEAP_STR);

    public static final ArgSpec HANDLER_PRIORITY_OPT =
        new ArgSpec(HANDLER_PRIORITY_STR,
                    PRIORITY_VALIDATOR);

    public static final ArgSpec FIRE_COUNT_OPT =
        new ArgSpec(FIRE_COUNT_STR,
                    NATURAL_NUM_VALIDATOR);

    public static final ArgSpec MEMORY_AREA_OPT =
        new ArgSpec(MEMORY_AREA_STR,
                    MEMORY_AREA_VALIDATORS);

    public static final ArgSpec OUT_DIR_OPT =
        new ArgSpec(OUT_DIR_STR,
                    PATH_VALIDATOR);

    public static final ArgSpec MEM_PROFILE_OPT =
        new ArgSpec(MEM_PROFILE_STR,
                    NATURAL_NUM_VALIDATOR);

    public static final ArgSpec BACKGROUND_THREAD_NUMBER_OPT =
        new ArgSpec(BACKGROUND_THREAD_NUMBER_STR,
                    NATURAL_NUM_VALIDATOR);

    public static final ArgSpec BACKGROUND_THREAD_PRIORITY_OPT =
        new ArgSpec(BACKGROUND_THREAD_PRIORITY_STR,
                    PRIORITY_VALIDATOR);


    public static final ArgSpec LP_ASYNC_HANDLER_NUMBER_OPT =
        new ArgSpec(LP_ASYNC_HANDLER_NUMBER_STR,
                    NATURAL_NUM_VALIDATOR);

    public static final ArgSpec LP_ASYNC_HANDLER_PRIORITY_OPT =
        new ArgSpec(LP_ASYNC_HANDLER_NUMBER_STR,
                    PRIORITY_VALIDATOR);

    public static final ArgSpec MEM_SIZE_OPT =
        new ArgSpec(MEM_SIZE_STR,
                    NATURAL_NUM_VALIDATOR);


    public static final ArgSpec COUNT_OPT =
        new ArgSpec(COUNT_STR,
                    NATURAL_NUM_VALIDATOR);

    public static final ArgSpec SCOPED_MEMORY_TYPE_OPT =
        new ArgSpec(SCOPED_MEMORY_TYPE_STR,
                    SCOPED_MEMORY_TYPE_VALIDATOR);

    public static final ArgSpec ALLOC_SIZE_OPT =
        new ArgSpec(ALLOC_SIZE_STR,
                    NATURAL_NUM_VALIDATOR);
}
