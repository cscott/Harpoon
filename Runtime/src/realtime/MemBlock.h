/* MemBlock.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __MEMBLOCK_H__
#define __MEMBLOCK_H__

#include <jni.h>
#include <assert.h>
#include "jni-private.h"
#include "blockAllocator.h"
#include "linkedListAllocator.h"
#include "refCountAllocator.h"
#include "RTJconfig.h"
#include "flexthread.h"
#include "RTJgc.h"
#ifdef WITH_PRECISE_GC
#include "../gc/precise_gc.h"
#endif

/* To avoid unwanted macro expansions in strange places */
#ifdef WITH_DMALLOC  
#undef free
#undef malloc
#endif

typedef void* Allocator;

struct MemBlock;

#ifdef RTJ_DEBUG_REF
struct PTRinfo {
  char* file;
  int line;
  size_t size;
  void* ptr;
  struct MemBlock* memBlock;
  struct PTRinfo* next;
};
struct PTRinfo* ptr_info;
#endif

struct MemBlock {
  jobject memoryArea; /* Only valid if refCount != 0 */
  void* (*alloc) (struct MemBlock* mem, size_t size);
  void  (*free)  (struct MemBlock* mem);
  void  (*finalize) (struct MemBlock* mem);
#ifdef WITH_PRECISE_GC
  void  (*gc) (struct MemBlock* mem);
#endif
  union { 
    void* allocator; 
    struct ListAllocator* ls; 
    struct linkedListAllocator* lls; 
    struct refCountAllocator* rc;
  } alloc_union;
  struct Block* block;
  long refCount;
  pthread_cond_t finalize_cond;
  pthread_mutex_t finalize_lock;
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* ptr_info;
#endif
};

#ifdef WITH_PRECISE_GC
struct refCountAllocator* gc_info; 
#endif

inline void MemBlock_finalize(struct refCountAllocator* rc, void* obj);
struct MemBlock* MemBlock_new(JNIEnv* env, jobject memoryArea);
inline struct MemBlock* Heap_MemBlock_new(JNIEnv* env, jobject heapMem);
inline void* MemBlock_alloc( struct MemBlock* memBlock, 
			     size_t size);
inline int MemBlock_free(struct MemBlock* memBlock);
inline long MemBlock_INCREF(struct MemBlock* memBlock);
inline long MemBlock_DECREF(struct MemBlock* memBlock);
inline struct inflated_oobj* getInflatedObject(JNIEnv* env, jobject obj);
const char* className(jobject obj);
const char* classNameUnwrap(struct oobj* obj);
#ifdef WITH_NOHEAP_SUPPORT
inline int IsNoHeapRealtimeThread(JNIEnv *env, 
				  jobject realtimeThread);
inline void _heapCheck_leap(struct oobj* ptr, const int line, const char* file);
#define heapCheck(ptr) _heapCheck_leap(ptr, __LINE__, __FILE__)
#ifdef RTJ_DEBUG_REF
inline void heapCheckRef(struct oobj* ptr, const int line, const char* file,
			 const char* op);
#else
inline void heapCheckJava(struct oobj* ptr);
#endif
#endif

void checkException();

/* Identify pointers locations, def points, MemBlocks, MemoryArea, etc. */
#ifdef RTJ_DEBUG_REF
void printPointerInfo(struct oobj* obj, int printClassInfo);
void dumpMemoryInfo(int printClassInfo);
#endif

#ifdef WITH_PRECISE_GC
#define MemBlockDECL(type) \
void* type##_MemBlock_alloc(struct MemBlock* mem, size_t size); \
void  type##_MemBlock_free( struct MemBlock* mem); \
void  type##_MemBlock_gc(struct MemBlock* mem); \
void  type##_MemBlock_finalize(struct MemBlock* mem);
#else
#define MemBlockDECL(type) \
void* type##_MemBlock_alloc(struct MemBlock* mem, size_t size); \
void  type##_MemBlock_free( struct MemBlock* mem); \
void  type##_MemBlock_finalize(struct MemBlock* mem);
#endif

MemBlockDECL(ScopedPhysical)
MemBlockDECL(CTScope)
MemBlockDECL(VTScope)
MemBlockDECL(LTScope)
MemBlockDECL(ImmortalPhysical)
MemBlockDECL(Immortal)
MemBlockDECL(Heap)
MemBlockDECL(RefCount)

#undef MemBlockDECL

#endif /* __MEMBLOCK_H__ */

