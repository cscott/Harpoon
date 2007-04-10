#!/usr/bin/gnuplot -persist
#
#    
#    	G N U P L O T
#    	Linux version 3.7
#    	patchlevel 1
#    	last modified Fri Oct 22 18:00:00 BST 1999
#    
#    	Copyright(C) 1986 - 1993, 1998, 1999
#    	Thomas Williams, Colin Kelley and many others
#    
#    	Type `help` to access the on-line reference manual
#    	The gnuplot FAQ is available from
#    	<http://www.ucc.ie/gnuplot/gnuplot-faq.html>
#    
#    	Send comments and requests for help to <info-gnuplot@dartmouth.edu>
#    	Send bugs, suggestions and mods to <bug-gnuplot@dartmouth.edu>
#    
set terminal emf color dashed "Helvetica Bold" 14
set output 'linux-color.emf'
set noclip points
set clip one
set noclip two
set bar 1.000000
set border 31 lt 13
set nogrid
set key title ""
set key right top Right noreverse box linetype -2 linewidth 1.000 samplen 4 spacing 1 width 0
set nolabel
set noarrow
set nolinestyle
set nologscale
set logscale xy 10
#set offsets 0, 0, 0, 0
set pointsize 1
set encoding default
set nopolar
set noparametric
#set view 60, 30, 1, 1
set samples 100, 100
set isosamples 10, 10
set surface
set nocontour
set clabel '%8.3g'
set mapping cartesian
set nohidden3d
#set size ratio 0 0.65,0.5
set size ratio .5
set origin 0,0
set data style points
set function style lines
set xzeroaxis lt 15 lw 1.000
set x2zeroaxis lt 15 lw 1.000
set yzeroaxis lt 15 lw 1.000
set y2zeroaxis lt 15 lw 1.000
set tics in
set ticslevel 0.5
set ticscale 1 0.5
set mxtics default
set mytics default
set mx2tics default
set my2tics default
set xtics border nomirror norotate autofreq 
set xtics (1,10,100,1000,"" 7047,8144)
set ytics border nomirror norotate autofreq 
set ytics (1,"" 10,"10^2" 100,"" 1000,"10^4" 10000,"" 100000,"10^6" 1000000,"9.355x10^6" 9354949)
set ztics border nomirror norotate autofreq 
set nox2tics
set noy2tics
set title ""
set rrange [ * : * ] noreverse nowriteback  # (currently [-0.00000:10.0000] )
set trange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set urange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set vrange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set xlabel "Fully associative cache size (64 byte lines)" 0,.5 tc lt 15
set x2label ""
set timefmt "%d/%m/%y\n%H:%M"
set xrange [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set x2range [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set ylabel "Number of overflowing transactions" 5.5,0 tc lt 15
set y2label ""
set yrange [ * : 10000000 ] noreverse nowriteback  # (currently [:10.0000] )
set y2range [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set zlabel ""
set zrange [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set zero 1e-08
# add some grid lines; use line style 15, 24, or 28
set arrow 1 from 54, graph 0 to 54, graph 1 nohead lt 24
set arrow 2 from 1, 10000 to 8144, 10000 nohead lt 24
# can't figure out how to set key title color =(
set label "make" at graph .73, graph .94 right tc lt 13
set label "dbench" at graph .73, graph .86 right tc lt 13
plot "make.prefix"  using 2:(17804273-$1) title " " with lines lw 4, "dbench.prefix" using 2:(2795323-$1) title " " with lines lw 4
#    EOF
