## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libtransact.la

# this library will be empty unless we've elected to build transaction support.
if TRANSACTIONS
TRANSACTSRC=  transact.h \
	java_lang_Object.c java_lang_Object.h \
	harpoon_Runtime_Transactions_CommitRecord.c \
	harpoon_Runtime_Transactions_CommitRecord.h
endif

libtransact_la_SOURCES = $(TRANSACTSRC)
