## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build transaction support.
noinst_LTLIBRARIES = libtransact.la

TRANSACTSRC=  transact.h \
	java_lang_Object.c java_lang_Object.h \
	harpoon_Runtime_Transactions_CommitRecord.c \
	harpoon_Runtime_Transactions_CommitRecord.h

if TRANSACTIONS
libtransact_la_SOURCES = $(TRANSACTSRC)
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when TRANSACTIONS is set)
EXTRA_DIST = $(TRANSACTSRC)
