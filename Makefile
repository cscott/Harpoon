#STDOBJS=header.o Java.a footer.o Runtime.a
STDOBJS=Java.a Runtime.a

ARCH=$(shell uname -m)

CFLAGS=-g -Iinclude -Iarch/$(ARCH)

print-fixup: src/print-fixup.o $(STDOBJS)
	gcc -o $@ $+

test-lookup: src/test-lookup.o $(STDOBJS)
	gcc -o $@ $+

Runtime.a: Runtime.a(src/stubs.o)
	ranlib $@

# show unresolved symbols
test: $(STDOBJS)
	gcc -o $@ $+

clean:
	$(RM) Runtime.a src/print-fixup.o src/stubs.o *~ \
		print-fixup test-lookup test
