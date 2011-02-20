#ifndef _SYM_H
#define _SYM_H
#include <config.h>

#ifdef NO_UNDERSCORES
#define csymbol(x) x
#else
#define csymbol(x) _##x
#endif

#endif
