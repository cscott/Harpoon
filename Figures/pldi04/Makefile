PLOTS=tr-w-ten.eps tr-w-all.eps tr-sz-all.eps
all: $(PLOTS) 

copy: $(PLOTS)
	cp $^ ..

update:
	scp ygg:Transact/Binaries/MEMTRACE_SZ/"{freq,trsize}.*.*.txt" .

%.eps: %.plo
	ploticus -tightcrop -eps $<

tr-w-all.eps: acc-freq-all.pl
tr-w-ten.eps: acc-freq-ten.pl
tr-sz-all.eps: acc-trsize-all.pl
