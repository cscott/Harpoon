#include "GCstats.h"

void* GC_malloc_stats(size_t size_in_bytes) {
  struct timeval begin; 
  struct timeval end; 
  void* result; 
  gettimeofday(&begin, NULL); 
  result = GC_malloc(size_in_bytes); 
  gettimeofday(&end, NULL); 
  printf("GC_malloc_stats: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec); 
  return result;
}

void* GC_malloc_atomic_stats(size_t size_in_bytes) {
  struct timeval begin;
  struct timeval end;
  void* result;
  gettimeofday(&begin, NULL);
  result = GC_malloc_atomic(size_in_bytes);
  gettimeofday(&end, NULL);
  printf("GC_malloc_atomic_stats: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
  return result;
}

void* GC_malloc_uncollectable_stats(size_t size_in_bytes) {
  struct timeval begin;
  struct timeval end;
  void* result;
  gettimeofday(&begin, NULL);
  result = GC_malloc_uncollectable(size_in_bytes);
  gettimeofday(&end, NULL);
  printf("GC_malloc_uncollectable_stats: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
  return result;
}

void GC_free_stats(void* object_addr) {
  struct timeval begin;
  struct timeval end;
  gettimeofday(&begin, NULL);
  GC_free(object_addr);
  gettimeofday(&end, NULL);
  printf("GC_free_stats: %ds %dus\n",
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
}
