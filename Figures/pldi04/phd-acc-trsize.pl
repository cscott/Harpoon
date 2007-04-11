#!/usr/bin/env perl

@benchmarks = ( 201, 202, 205, 209, 213, 222, 228 );
%fullname = ( 200 => "check", 201 => "compress", 202 => "jess",
	      205 => "raytrace", 209 => "db", 213 => "javac",
	      222 => "mpegaudio", 227 => "mtrt", 228 => "jack" );
%baseline = (202=>11.98, 209=>34.04, 213=>12.21, 222=>10.42, 228=>6.62);
%runsize = ( 201=>100, 202 => 100, 205 => 100, 209 => 100, 213 => 100, 222 => 100, 228 => 100);

foreach my $run (keys %runsize) {
    my $file = "freq.".$run.".".$runsize{$run}.".txt";
    open(FH, "< $file") or die "Can't open $file for reading.\n";
    while (<FH>) {
        my ($sz, $op, $freq) = split;
	$count{$run}{$sz}{$op} = $freq;
	$total{$run}{$op} += $freq;
	$inttotal{$run}{$op} += $freq*$sz;
    }
    close FH;
}
@allops = ( "r", "w", "R", "W" );

foreach my $op (@allops) {
    foreach my $run ( keys %baseline ) {
	$norm{$run}{$op} = $inttotal{$run}{$op} / $total{$run}{$op};
    }
}

foreach my $op (@allops) {
    print $op.":\n";
    foreach my $run (sort { $norm{$a}{$op} <=> $norm{$b}{$op} } keys %total){
	my $name = $fullname{$run};
	$name.=" " while length($name) < 9;
	print " ".$name."\t".$norm{$run}{$op}."\t".$inttotal{$run}{$op}."\n";
    }
    print "\n";
}
