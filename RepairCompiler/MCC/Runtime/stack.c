/*
Copyright 1988, 1989 Hans-J. Boehm, Alan J. Demers
Copyright (c) 1991-1995 by Xerox Corporation.  All rights reserved.
Copyright (c) 1996-1999 by Silicon Graphics.  All rights reserved.
Copyright (c) 1999-2001 by Hewlett-Packard. All rights reserved.

THIS MATERIAL IS PROVIDED AS IS, WITH ABSOLUTELY NO WARRANTY EXPRESSED
OR IMPLIED.  ANY USE IS AT YOUR OWN RISK.

Permission is hereby granted to use or copy this program
for any purpose,  provided the above notices are retained on all copies.
Permission to modify the code and to distribute modified code is granted,
provided the above notices are retained, and a notice that the code was
modified is included with the above copyright notice.
 */


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <ctype.h>
#include <stdio.h>
#include <unistd.h>

#define ptr_t void *
#pragma weak __libc_stack_end
extern ptr_t __libc_stack_end;
#define word unsigned int
# define STAT_SKIP 27   /* Number of fields preceding startstack	*/
			/* field in /proc/self/stat			*/
#define ABORT printf

  ptr_t GC_linux_stack_base(void)
  {
    /* We read the stack base value from /proc/self/stat.  We do this	*/
    /* using direct I/O system calls in order to avoid calling malloc   */
    /* in case REDIRECT_MALLOC is defined.				*/
#   define STAT_BUF_SIZE 4096
#   define STAT_READ read
	  /* Should probably call the real read, if read is wrapped.	*/
    char stat_buf[STAT_BUF_SIZE];
    int f;
    char c;
    word result = 0;
    size_t i, buf_offset = 0;

    /* First try the easy way.  This should work for glibc 2.2	*/
      if (0 != &__libc_stack_end) {
#       ifdef IA64
	  /* Some versions of glibc set the address 16 bytes too	*/
	  /* low while the initialization code is running.		*/
	  if (((word)__libc_stack_end & 0xfff) + 0x10 < 0x1000) {
	    return __libc_stack_end + 0x10;
	  } /* Otherwise it's not safe to add 16 bytes and we fall	*/
	    /* back to using /proc.					*/
#	else
	  return __libc_stack_end;
#	endif
      }
    f = open("/proc/self/stat", O_RDONLY);
    if (f < 0 || STAT_READ(f, stat_buf, STAT_BUF_SIZE) < 2 * STAT_SKIP) {
	ABORT("Couldn't read /proc/self/stat");
    }
    c = stat_buf[buf_offset++];
    /* Skip the required number of fields.  This number is hopefully	*/
    /* constant across all Linux implementations.			*/
      for (i = 0; i < STAT_SKIP; ++i) {
	while (isspace(c)) c = stat_buf[buf_offset++];
	while (!isspace(c)) c = stat_buf[buf_offset++];
      }
    while (isspace(c)) c = stat_buf[buf_offset++];
    while (isdigit(c)) {
      result *= 10;
      result += c - '0';
      c = stat_buf[buf_offset++];
    }
    close(f);
    if (result < 0x10000000) ABORT("Absurd stack bottom value");
    return (ptr_t)result;
  }
