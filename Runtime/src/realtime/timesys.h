/* timesys.h, created by wbeebee
   Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __WITH_TIMESYS_H__ /* guard against multiple includes */

#include <config.h>
#include "Scheduler.h"

#ifdef WITH_REALTIME_THREADS_TIMESYS
int createCPU(unsigned long long int compute,
	      unsigned long long int period,
	      unsigned long long int begin);

int createNET();

void destroyRES();
#endif /* WITH_REALTIME_THREADS_TIMESYS */

#endif /* __WITH_TIMESYS_H__ */
