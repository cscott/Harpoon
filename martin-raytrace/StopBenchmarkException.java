/*
 * @(#)StopBenchmarkException.java	1.4 06/17/98
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

/**
 * This class mainly handles the the runtime exceptions caused during the 
 * execution of the benchmarks. This is useful to have smooth exit from the 
 * program, even the program produces unexpected problems.
 */
public class StopBenchmarkException extends RuntimeException
{

public StopBenchmarkException( String s )
    {
    super( s );
    }
}
