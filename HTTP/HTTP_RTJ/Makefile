all: norealtime realtime old-realtime

norealtime:
	cd ../HTTP; jikes *.java; mv *.class /home/wbeebee/Harpoon/Code/
	cd /home/wbeebee/Harpoon/Runtime; ./configure --with-precise-c --with-gc=conservative --with-thread-model=heavy --disable-shared --with-full-stack-trace; make
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec JhttpServer HTTP-norealtime

realtime: 
	jikes *.java
	mv *.class /home/wbeebee/Harpoon/Code/
	cd /home/wbeebee/Harpoon/Runtime; ./configure --with-precise-c --with-gc=conservative --with-thread-model=heavy --disable-shared --with-full-stack-trace --with-realtime-java; make
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec -t SIMPLE JhttpServer HTTP-checks
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec -t ALL JhttpServer HTTP-nochecks

realtime-debug:
	jikes *.java
	mv *.class /home/wbeebee/Harpoon/Code/
	cd /home/wbeebee/Harpoon/Runtime; ./configure --with-precise-c --with-gc=conservative --with-thread-model=heavy --disable-shared --with-full-stack-trace --with-realtime-java; make
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec-debug -t SIMPLE JhttpServer HTTP-checks
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec-debug -t ALL JhttpServer HTTP-nochecks

old-realtime: 
	cd ../HTTP; jikes *.java; mv *.class /home/wbeebee/Harpoon/Code/
	cd /home/wbeebee/Harpoon/Runtime; ./configure --with-precise-c --with-gc=conservative --with-thread-model=heavy --disable-shared --with-full-stack-trace --with-realtime-java; make
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec -t SIMPLE JhttpServer HTTP-checks-old
	cd /home/wbeebee/Harpoon/Code; bin/build-precisec -t ALL JhttpServer HTTP-nochecks-old
