VAR=BASE RCHECK WCHECK RWCHECK RWCHECKOPT

all: $(foreach v,$(VAR),bench-$(v).s bench-$(v))

bench-%.s: bench.c llsc-unimpl.h llsc-ppc32.h
	gcc -D$* -S -O9 $<
	mv bench.s bench-$*.s
bench-%: bench-%.s
	gcc -o $@ -O9 $<

clean:
	$(RM) bench-*.s
	$(RM) bench-*[A-Z]
run: all run-init $(foreach v,$(VAR),run-$(v)) run-done
run-init:
	touch results.txt
	echo >> results.txt
	echo -n "--- Run started: " >> results.txt
	date >> results.txt
run-done:
	echo "--- Run complete ---" >> results.txt
run-%: bench-%
	-./bench-$* # warm up cache
	-/usr/bin/time -f $*" %U" -o results.txt -a ./bench-$*
	tail -1 results.txt

####
print.ps:
	a2ps -o$@ -Mletter --prologue=color -E -g notes.txt results.txt Makefile bench.c bench-split.c llsc-ppc32.h llsc-unimpl.h 
