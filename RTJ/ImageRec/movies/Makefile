all: 	images.dir movies.dir plane.dir

%.tgz:	
	wget -q http://flexc.lcs.mit.edu/Harpoon/movies/$@

%.dir:	%.tgz
	tar zxvpf $<
	rm -rf $<
