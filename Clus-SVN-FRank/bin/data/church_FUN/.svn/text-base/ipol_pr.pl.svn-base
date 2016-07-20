
# Input file given in ARGV[0]
# Output file is input file without extension with "_ipol.dat" added
$file = $ARGV[0];
$out = $file;
$out =~ s/\.[^\.]+$//;
$out = $out . "_ipol.dat";

# Reset counters
$nb = 0;
$TOT = 0.0;
$POS = 0.0;
$posfound = 0;

# Open input file
open(IN, "$file") || die "Can't open $file";
while ($line = <IN>) {
	$line =~ s/[\n\r]//g;
	if (!($line =~ /^\s*$/)) {
		# Each line is of the form RECALL, PRECISION, TP, FP, THRESHOLD
		if ($line =~ /^([0-9\.e\+\-]+)\,\s*([0-9\.e\+\-]+)\,\s*([0-9\.e\+\-]+)\,\s*([0-9\.e\+\-]+)/) {
			$reca = $1; $prec = $2;
			$tp   = $3; $fp   = $4;
			# Recompute recall and precision more accurately
			$reca = 1.0*$tp/$POS;
			$prec = 1.0*$tp/($tp+$fp);
			if (($nb == 0) && ($reca > 0)) {
				# Add a first point with zero recall!
				$PREC[$nb] = $prec;
				$RECA[$nb] = 0.0;
				$TP[$nb]   = $tp;
				$FP[$nb]   = $fp;
				$nb++;
			}
			# Store lines in memory
			$PREC[$nb] = $prec;
			$RECA[$nb] = $reca;
			$TP[$nb]   = $tp;
			$FP[$nb]   = $fp;
			$nb++;
		} elsif ($line =~ /^POS\:\s*([0-9\.e\+\-]+)$/) {
			# File must also contain number of positive examples
			$POS = $1;
			$posfound = 1;
		} elsif ($line =~ /^TOT\:\s*([0-9\.e\+\-]+)$/) {
			# File must also contain number of total examples
			$TOT = $1;
		} else {
			die "Illegal line: '$line'";
		}
	}
}
close(IN);

# If no positive examples, then area is zero
if (($POS == 0.0) && ($posfound == 1)) {
	print "Area = 0\n";
	open(OUT, ">$out") || die "Can't create $out";
	close(OUT);
	exit(0);
}

if ($TOT == 0.0) { die "Total number of examples not found in file"; }
if ($POS == 0.0) { die "Total number of positives not found in file"; }

$NEG = $TOT - $POS;

# Extend recall to 1.0 if not available
if ($RECA[$nb-1] != 1.0) {
	$RECA[$nb] = 1.0;
	$PREC[$nb] = 1.0*$POS/$TOT;
	$TP[$nb]   = $POS;
	$FP[$nb]   = $TOT - $POS;
	$nb++;
}

# Reset output arrays
$OUTCNT  = 0;
@OUTRECA = ();
@OUTPREC = ();

sub append_out {
	# Append point to output
	# - Filter duplicates
	# - Filter sequences of constant precision
	my($reca, $prec) = @_;
	if ($OUTCNT > 0) {
		if (!(($OUTRECA[$OUTCNT-1] == $reca) && ($OUTPREC[$OUTCNT-1] == $prec))) {
			if (($OUTCNT > 1) && (abs($OUTPREC[$OUTCNT-1]-$prec) < 1e-15) && (abs($OUTPREC[$OUTCNT-2]-$prec) < 1e-15)) {
				# Constant precision (horizontal line)
				$OUTRECA[$OUTCNT-1] = $reca;
			} elsif (($OUTCNT > 1) && (abs($OUTRECA[$OUTCNT-1]-$reca) < 1e-15) && (abs($OUTRECA[$OUTCNT-2]-$reca) < 1e-15)) {
				# Constant recalll (vertical line)
				$OUTPREC[$OUTCNT-1] = $prec;
			} else {
				push @OUTRECA, $reca;
				push @OUTPREC, $prec;
				$OUTCNT++;
			}
		}
	} else {
		push @OUTRECA, $reca;
		push @OUTPREC, $prec;
		$OUTCNT++;
	}
}

# Perform the interpolation
append_out($RECA[0],$PREC[0]);
$tp_a = $TP[0]; $fp_a = $FP[0];
for ($i = 0; $i < $nb; $i++) {
	$tp_b = $TP[$i]; $fp_b = $FP[$i];
	for ($tp = $tp_a + 1; $tp < $tp_b; $tp++) {
		$fp = $fp_a + ($fp_b-$fp_a)/($tp_b-$tp_a)*($tp - $tp_a);
		$reca = $tp/$POS;
		$prec = $tp/($tp+$fp);
		append_out($reca,$prec);
	}
	$tp_a = $tp_b; $fp_a = $fp_b;
	append_out($RECA[$i],$PREC[$i]);
}

# Compute the area
$area = 0;
$prev_RECA = $OUTRECA[0]; $prev_PREC = $OUTPREC[0];
for ($i = 1; $i < $OUTCNT; $i++) {
	$reca = $OUTRECA[$i];
	$prec = $OUTPREC[$i];
	$area += ($prec + $prev_PREC)/2.0*($reca - $prev_RECA);
	$prev_RECA = $reca; $prev_PREC = $prec;
}

# Write the points to the output file
open(OUT, ">$out") || die "Can't create $out";
for ($i = 0; $i < $OUTCNT; $i++) {
	print OUT "$OUTRECA[$i],$OUTPREC[$i]\n";
}
close(OUT);

print "Area = $area\n";
