#!/usr/bin/env perl

# missing: freq.{201,205,209,222,228}.100

@benchmarks = ( 201, 202, 205, 209, 213, 222, 228 );
%fullname = ( 200 => "check", 201 => "compress", 202 => "jess",
	      205 => "raytrace", 209 => "db", 213 => "javac",
	      222 => "mpegaudio", 227 => "mtrt", 228 => "jess" );
%runsize = ( 201 => 10, 202 => 100, 205 => 10, 209 => 10, 213 => 100, 222 => 10 , 228 => 10);

foreach my $run (keys %runsize) {
    my $file = "freq.".$run.".".$runsize{$run}.".txt";
    open(FH, "< $file") or die "Can't open $file for reading.\n";
    while (<FH>) {
        my ($sz, $op, $freq) = split;
	$count{$run}{$sz}{$op} = $freq;
	$total{$run}{$op} += $freq;
	$allsizes{$sz} = 1; # keep track of all sizes seen.
    }
    close FH;
}
$allsizes{12} = 1; # min x
@allops = ( "r", "w", "R", "W" );
# now accumulate in reverse size order
# (so we get "number of accesses to objects larger than X")
# also, only pay attention to 'w' (transactional writes)
print "SIZE";
foreach my $run (sort keys %runsize) {
    foreach my $op ( @allops ) {
	print " ".$fullname{$run}."_".$op;
    }
}
print "\n";
my $lastsz = 1e10;
foreach my $sz (sort {$b <=> $a} keys %allsizes) {
    my $emit = 0;
    if ($sz <= $lastsz/1.35) { $emit = 1; $lastsz = $sz; }
    print "$sz" if $emit;
    foreach my $run (sort keys %runsize) {
	foreach my $op ( @allops ) {
	    $acc{$run}{$op} += $count{$run}{$sz}{$op};
	    print "\t" if $emit;
	    my $pct = (100.0*$acc{$run}{$op}/$total{$run}{$op});
	    print $pct if $emit;
	}
    }
    print "\n" if $emit;
}
