/* timesys.c, created by wbeebee
   Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "timesys.h"

#ifdef WITH_REALTIME_THREADS_TIMESYS
#include <time.h>
#include <rk/rk.h>
#include <sys/types.h>
#include <unistd.h>
#endif

#include <stdio.h>
#include <stdlib.h>

#ifdef WITH_REALTIME_THREADS_TIMESYS
inline static rk_resource_set_t getRES() {
  rk_resource_set_t rk = rk_proc_get_rset(getpid());
  
  if (rk == NULL) {
    rk = rk_resource_set_create("FLEX Executable");
    rk_resource_set_attach_process(rk, getpid());
  }

  return rk;
}

int createCPU(unsigned long long int compute, 
	      unsigned long long int period, 
	      unsigned long long int begin) {
  rk_resource_set_t rk = getRES();
  rk_reserve_t res;
  cpu_reserve_attr_data_t cpu;
  
  cpu.reserve_type.sch_mode = cpu.reserve_type.enf_mode = 
    cpu.reserve_type.rep_mode = RSV_HARD;
  
  cpu.blocking_time.tv_sec = cpu.blocking_time.tv_nsec = 0;
  
  cpu.deadline.tv_sec = cpu.period.tv_sec = 
    ((unsigned long long int)period)/1000000000;
  cpu.deadline.tv_nsec = cpu.period.tv_nsec = 
    ((unsigned long long int)period)%1000000000;
  
  cpu.compute_time.tv_sec = ((unsigned long long int)compute)/1000000000;
  cpu.compute_time.tv_nsec = ((unsigned long long int)compute)%1000000000;
  
  cpu.processor = RK_ANY_CPU; /* For single CPU systems */
  
  if (begin == 0) {
    gettimeofday(&cpu.start_time, NULL);
  } else {
    cpu.start_time.tv_sec = ((unsigned long long int)begin)/1000000000;
    cpu.start_time.tv_nsec = ((unsigned long long int)begin)%1000000000;
  }
  
  if (rk_resource_set_get_cpu_rsv(rk)==NULL) {
    return rk_cpu_reserve_create(rk, &res, &cpu);
  } else {
    return rk_cpu_reserve_ctl(rk, &cpu);
  }
}

int createNET(size_t bytes, unsigned long long int transfer,
	      unsigned long long int period,
	      unsigned long long int begin) {
  rk_resource_set_t rk = getRES();
  rk_reserve_t res;
  net_reserve_attr_data_t net;
  
  net.reserve_type.sch_mode = net.reserve_type.enf_mode = 
    net.reserve_type.rep_mode = RSV_HARD;

  net.blocking_time.tv_sec = net.blocking_time.tv_nsec = 0;

  net.deadline.tv_sec = ((unsigned long long int)transfer)/1000000000;
  net.deadline.tv_nsec = ((unsigned long long int)transfer)%1000000000;

  net.period.tv_sec = ((unsigned long long int)period)/1000000000;
  net.period.tv_nsec = ((unsigned long long int)period)%1000000000;

  if (begin == 0) {
    gettimeofday(&net.start_time, NULL);
  } else {
    net.start_time.tv_sec = ((unsigned long long int)begin)/1000000000;
    net.start_time.tv_nsec = ((unsigned long long int)begin)%1000000000;
  }

  net.amount = bytes;
  
  net.buffer_space = 0;
  
  if (rk_resource_set_get_net_rsv(rk)==NULL) {
    return rk_net_reserve_create(rk, &res, &net);
  } else {
    return rk_net_reserve_ctl(rk, &net);
  }
}

void destroyRES() {
  rk_resource_set_t rk = rk_proc_get_rset(getpid());
  if (rk != NULL) {
    if (rk_resource_set_get_cpu_rsv(rk) != NULL) {
      rk_cpu_reserve_delete(rk);
    }
    if (rk_resource_set_get_net_rsv(rk) != NULL) {
      rk_net_reserve_delete(rk);
    }
    rk_resource_set_detach_process(rk, getpid());
    rk_resource_set_destroy(rk);
  }
}
#else
inline static jboolean errorNotImpl() {
  printf("This function is only available on the TimeSys operating system\n");
  printf("with the Real-Time CPU/NET development kit.\n");
  exit(-1);
  return JNI_FALSE;
}
#endif

/*
 * Class:     javax_realtime_Scheduler
 * Method:    reserveCPU
 * Signature: (JJJ)V
 */
JNIEXPORT jboolean JNICALL Java_javax_realtime_Scheduler_reserveCPU
(JNIEnv *env, jobject scheduler, jlong compute, jlong period, jlong begin) {
#ifdef WITH_REALTIME_THREADS_TIMESYS
  return createCPU((unsigned long long int)compute, 
		   (unsigned long long int)period, 
		   (unsigned long long int)begin)?JNI_TRUE:JNI_FALSE;
#else
  return errorNotImpl();
#endif
}

/*
 * Class:     javax_realtime_Scheduler
 * Method:    reserveNET
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_reserveNET
(JNIEnv *env, jobject scheduler, jlong bytes, jlong transfer, j) {
#ifdef WITH_REALTIME_THREADS_TIMESYS
  return createNET()?JNI_TRUE:JNI_FALSE;
#else
  return errorNotImpl();
#endif
}
