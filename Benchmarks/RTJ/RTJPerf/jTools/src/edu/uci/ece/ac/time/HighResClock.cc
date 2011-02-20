// This file was created by `gcjh -stubs'. -*- c++ -*-
//
// This file is intended to give you a head start on implementing native
// methods using CNI.
// Be aware: running `gcjh -stubs ' once more for this class may
// overwrite any edits you have made to this file.

#include "HighResClock.h"
#include "HighResTime.h"
#include <gcj/cni.h>
#include <sys/time.h>
#include "SysInfo.h"
#include <stdio.h>

void
edu::uci::ece::ac::time::HighResClock::getTime (::edu::uci::ece::ac::time::HighResTime *hrtime)
{
  // Here I use the RDTSC instruction provided by the Pentium
  // Processor family to access the clock counter. To avoid problem
  // with out of order execution, and to make sure that the clock
  // cycle number is accessed really where the call appears in the
  // source code a cpuid instruction is added. 
  //
  unsigned long cpuid;
  unsigned long long int rdtsc;
  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));

  // Convert clock number into nano seconds
  long double timeNS = rdtsc * CLOCK_PERIOD_NS;

  long cMilliSec = static_cast<long>(timeNS/POW_6_TEN);
  long cRoundNanoSec = static_cast<long>(cMilliSec * POW_6_TEN);
  long cMicroSec = static_cast<long>((timeNS - cRoundNanoSec)/POW_3_TEN);
  long cNanoSec = static_cast<long>(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));
  
  hrtime->setTime(static_cast<jlong>(cMilliSec),
                  static_cast<jlong>(cMicroSec),
                  static_cast<jlong>(cNanoSec));
}

jlong
edu::uci::ece::ac::time::HighResClock::getClockTickCount ()
{
  unsigned long long int rdtsc = 0;
  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));

  return static_cast<jlong>(rdtsc);
}

void
edu::uci::ece::ac::time::HighResClock::clockTick2HighResTime(jlong rdtsc,
                                                                      ::edu::uci::ece::ac::time::HighResTime *time)
{
  long double timeNS = rdtsc * CLOCK_PERIOD_NS;

  long cMilliSec = static_cast<long>(timeNS/POW_6_TEN);
  long cRoundNanoSec = static_cast<long>(cMilliSec * POW_6_TEN);
  long cMicroSec = static_cast<long>((timeNS - cRoundNanoSec)/POW_3_TEN);
  long cNanoSec = static_cast<long>(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));
  
  time->setTime(static_cast<jlong>(cMilliSec),
                static_cast<jlong>(cMicroSec),
                static_cast<jlong>(cNanoSec));
  
}

jfloat
edu::uci::ece::ac::time::HighResClock::getClockFrequency ()
{
  float clockFreq = CLOCK_FREQUENCY;
  jfloat freq = static_cast<jlong>(clockFreq);
  
  return freq;

}

jdouble
edu::uci::ece::ac::time::HighResClock::getClockPeriod ()
{
  double period = CLOCK_PERIOD_NS;
  jdouble jp = static_cast<jdouble>(period);
  return jp;
}
