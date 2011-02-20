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
set output '../uvsimCounterRuntime-color-2.emf'
set noclip points
set clip one
set noclip two
set bar 1.000000
set border 31 lt 0
set nogrid
set key title ""
set key left top Left reverse box linetype -2 linewidth 1.000 samplen 4 spacing 1 width 0
set nolabel
set noarrow
set nolinestyle
set nologscale
#set logscale x 10
#set logscale y 10
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
#set size ratio 0 0.6,0.35
set size ratio .4
set origin 0,0
set data style points
set function style lines
set xzeroaxis lt 0 lw 1.000
set x2zeroaxis lt 0 lw 1.000
set yzeroaxis lt 0 lw 1.000
set y2zeroaxis lt 0 lw 1.000
set tics in
set ticslevel 0.5
set ticscale 1 0.5
set mxtics default
set mytics default
set mx2tics default
set my2tics default
set xtics border nomirror norotate autofreq 
#set xtics (2,4,8,16)
set ytics border nomirror norotate autofreq 
# set ytics (0,120,"" 200, "" 400, "" 600, 680)
set ztics border nomirror norotate autofreq 
set nox2tics
set noy2tics
set title ""
set rrange [ * : * ] noreverse nowriteback  # (currently [-0.00000:10.0000] )
set trange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set urange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set vrange [ * : * ] noreverse nowriteback  # (currently [-5.00000:5.00000] )
set xlabel "Number of processors" 0,.5 textcolor lt 6
set timefmt "%d/%m/%y\n%H:%M"
set xrange [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set ylabel "Avg. cycles per iteration" 4 textcolor lt 6
set yrange [ * : * ] noreverse nowriteback  # (currently [-10.0000:10.0000] )
set zero 1e-08
#set locale "C"
# first title is 'locks'; second is 'transactions', but i can't figure out how
# to correctly set the color used to title these
set label "locks" at graph .23, graph .93 tc lt 1
set label "transactions" at graph .23, graph .85 tc lt 3
plot "uvsimCounterRuntime.txt" using 1:2 title " " with linespoints lw 4 ps 3, "uvsimCounterRuntime.txt" using 1:3 title " " with linespoints lw 4 ps 3 lt 3
#    EOF
