#ifndef INCLUDED_PRECISE_GC_CONSTS_H
#define INCLUDED_PRECISE_GC_CONSTS_H

#include "config.h"

#ifdef WITH_SINGLE_WORD_ALIGN
# define ALIGN_TO  4 /* bytes */
#else
# define ALIGN_TO  8 /* bytes */
#endif

#endif // INCLUDED_PRECISE_GC_CONSTS_H
