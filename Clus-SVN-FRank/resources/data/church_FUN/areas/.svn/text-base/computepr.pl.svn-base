
$file = $ARGV[0];

$base = $file;
$base =~ s/\.[^\.]+$//;

$NBPOS = 0;
$NBNEG = 0;

open(IN, $file) || die "Can't open '$file'";
while ($line = <IN>) {
	chomp($line);
	@arr = split(/\s*\,\s*/, $line);
	$thr = $arr[0];
	$cls = $arr[1];
	push @ELEMS, "$thr,$cls";
	if ($cls eq "pos") {
		$NBPOS++;
	} elsif ($cls eq "neg") {
		$NBNEG++;
	}
}

$NBTOT = $NBPOS + $NBNEG;

sub bythr {
	$x = $a;
	$y = $b;
	$x =~ s/\,.*$//;
	$y =~ s/\,.*$//;
	return $y <=> $x;
}

@SORTED = sort bythr @ELEMS;

foreach $e (@SORTED) {
	print "$e\n";
}

$TP = 0;
$FP = 0;

$PREV_TH = 1.1;

$NB_PRED = 0;

open(OUT, ">$base.pr") || die "Can't create '$base.pr'";
print OUT "POS: $NBPOS\n";
print OUT "TOT: $NBTOT\n";
for ($i = 0; $i <= $#SORTED; $i++) {
	@arr = split(/\s*\,\s*/, $SORTED[$i]);
	$thr = $arr[0];
	$cls = $arr[1];
	if (($thr != $PREV_TH) && ($i > 0)) {
		add_pr_point();
	}
	if ($cls eq "pos") {
		$TP++;
	} elsif ($cls eq "neg") {
		$FP++;
	} else {
		die "Illegal class '$cls'";
	}
	$NB_PRED++;
	$PREV_TH = $thr;
}
add_pr_point();
close(OUT);

sub add_pr_point {
	$PREC = $TP / $NB_PRED;
	$RECA = $TP / $NBPOS;
	$half = ($PREV_TH+$thr)/2;
	printf "THR = $half TP = $TP PREDPOS = $NB_PRED TOTPOS = $NBPOS PR = %.4f RC = %.4f\n", $PREC, $RECA;
	print OUT "$RECA,$PREC,$TP,$FP,x\n";
}
