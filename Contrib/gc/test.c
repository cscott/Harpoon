#define GC_LINUX_THREADS
#define _REENTRANT
#include"gc.h"


int main() {
	void *ptr;
	while(1) {
		ptr=GC_malloc(1);
		printf("%ld\n",ptr);
	}
}
