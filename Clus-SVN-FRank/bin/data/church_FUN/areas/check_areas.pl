
open(A, "church_FUN.test.pred.aroc.arff.orig") || die "Can't open 'church_FUN.test.pred.aroc.arff'";
open(B, "../church_FUN_write_predictions.out") || die "Can't open '../church_FUN_write_predictions.out'";
$found = 0;
while (($found == 0) && ($line = <A>)) {
	if ($line =~ /^\@DATA/) {
		$found = 1;
	}
}
while ($line = <A>) {
	chomp($line);
	@arr = split(/\,/, $line);
	$cls = $arr[0];
	$AUROCB{$cls} = $arr[2];
	$AUPRCB{$cls} = $arr[4];
}
close(A);
$found = 0;
while (($found == 0) && ($line = <B>)) {
	chomp($line);
	if ($line =~ /^Testing error/) {
		$found = 1;
	}
}
if ($found == 0) {
	print "ERROR: Can't find 'Testing error'\n";
}
$found = 0;
while (($found == 0) && ($line = <B>)) {
	chomp($line);
	if ($line =~ /^Hierarchical error measures/) {
		$found = 1;
	}
}
if ($found == 0) {
	print "ERROR: Can't find 'Hierarchical Error Measures'\n";
}
$found = 0;
while (($found == 0) && ($line = <B>)) {
	chomp($line);
	if ($line =~ /^\s*Original/) {
		$found = 1;
	}
}
if ($found == 0) {
	print "ERROR: Can't find 'Original'\n";
}
while ($line = <B>) {
	if ($line =~ /\s+(\S+)\,\s*AUROC\:\s*([^\,]+)\,\s*AUPRC\:\s*([^\,]+)\,\s*Freq\:\s*(\S+)/) {
		$cls = $1;
		$auroc_a = $2;
		$auprc_a = $3;
		$auroc_b = $AUROCB{$cls};
		$auprc_b = $AUPRCB{$cls};
		$tol = 1e-6;
		if ((abs($auroc_a - $auroc_b) > $tol) || (abs($auprc_a - $auprc_b) > $tol)) {
			print "ERROR: $cls: $auroc_a <> $auroc_b, $auprc_a <> $auprc_b\n";
		} else {
			print "OK: $cls: $auroc_a = $auroc_b, $auprc_a = $auprc_b\n";
		}
	}
}
close(B);

