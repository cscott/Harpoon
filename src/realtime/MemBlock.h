/* MemBlock.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __MEMBLOCK_H__
#define __MEMBLOCK_H__

#include <jni.h>
#include <assert.h>
#include "jni-private.h"
#include "heapMemory.h"
#include "blockAllocator.h"
#include "linkedListAllocator.h"
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

struct BlockInfo {
  jobject memoryArea;
  jobject realtimeThread;
  void* (*alloc) (struct MemBlock* mem, size_t size);
  void  (*free)  (struct MemBlock* mem);
#ifdef WITH_PRECISE_GC
  void  (*gc) (struct MemBlock* mem);
#endif
  Allocator allocator;
  struct MemBlock* superBlock;
};

struct RefInfo {
  long refCount;
  int reuse;
};

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
flex_mutex_t ptr_info_lock;
#endif

struct MemBlock {
  struct BlockInfo* block_info;
  struct RefInfo* ref_info;
  struct Block* block;
  struct MemBlock* next;
  flex_mutex_t list_lock;
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* ptr_info;
  flex_mutex_t ptr_info_lock;
#endif
};

#ifdef WITH_PRECISE_GC
struct GCinfo {
  struct MemBlock* memBlock;
  struct GCinfo* next;
};

struct GCinfo* gc_info;
flex_mutex_t gc_info_lock;
#endif

struct RefInfo* RefInfo_new(int reuse);

struct MemBlock* MemBlock_new(JNIEnv* env,
			      jobject memoryArea, 
			      jobject realtimeThread,
			      struct MemBlock* superBlock);
inline struct MemBlock* HeapMemory_RThread_MemBlock_new();
inline void* MemBlock_alloc( struct MemBlock* memBlock, 
			     size_t size);
inline void MemBlock_free(struct MemBlock* memBlock);
inline struct MemBlock* MemBlock_prevMemBlock( struct MemBlock* memBlock);
inline long MemBlock_INCREF(struct MemBlock* memBlock);
inline long MemBlock_DECREF(struct MemBlock* memBlock);
inline struct inflated_oobj* getInflatedObject(JNIEnv* env, 
					        jobject obj);
const char* className(jobject obj);
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
void printPointerInfo(void* obj, int printClassInfo);
void dumpMemoryInfo(int printClassInfo);
#endif

#ifdef WITH_PRECISE_GC
inline void add_MemBlock_to_roots(struct MemBlock* mem);
inline void remove_MemBlock_from_roots(struct MemBlock* mem);
#endif

#define MemBlockDECLThread(type, thread) \
void* type##_##thread##_MemBlock_alloc(struct MemBlock* mem, size_t size); \
void  type##_##thread##_MemBlock_free( struct MemBlock* mem); \
inline Allocator type##_##thread##_MemBlock_allocator(jobject memoryArea); 

#ifdef WITH_PRECISE_GC
#ifdef WITH_NOHEAP_SUPPORT
#define MemBlockDECL(type) \
MemBlockDECLThread(type, RThread); \
MemBlockDECLThread(type, NoHeapRThread); \
void type##_RThread_MemBlock_gc(struct MemBlock* mem);
#else
#define MemBlockDECL(type) \
MemBlockDECLThread(type, RThread); \
void  type##_RThread_MemBlock_gc(struct MemBlock* mem);
#endif
#else
#ifdef WITH_NOHEAP_SUPPORT
#define MemBlockDECL(type) \
MemBlockDECLThread(type, RThread); \
MemBlockDECLThread(type, NoHeapRThread);
#else
#define MemBlockDECL(type) \
MemBlockDECLThread(type, RThread);
#endif
#endif

MemBlockDECL(ScopedPhysical);
MemBlockDECL(CTScope);
MemBlockDECL(VTScope);
MemBlockDECL(LTScope);
MemBlockDECL(ImmortalPhysical);
MemBlockDECL(Immortal);
MemBlockDECL(Heap);

#undef MemBlockDECL
#undef MemBlockDECLThread

#endif /* __MEMBLOCK_H__ */
