open(A, "../../../testsets/AUC_average/church_FUN-PR-byclass.pr") || die "Can't open 'church_FUN-PR-byclass.pr'";
while ($line = <A>) {
	chomp($line);
	if ($line =~ /^NEXT\s+\S+\s+(\S+)\s+(\S+)\s+(\S+)$/) {
		$auprc_b = $1;
		$freq_b = $2;
		$cls = $3;
		$AUPRCB{$cls} = $auprc_b;
		$FREQB{$cls} = $freq_b;
	}
}
close(A);
open(B, "../church_FUN-9095.out") || die "Can't open .out file'";
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
	print "ERROR: Can't find 'Hierarchical error measures'\n";
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
		$cls_a = $1;
		$auroc_a = $2;
		$auprc_a = $3;
		$freq_a = $4;
		if (!defined($AUPRCB{$cls_a})) {
			print "ERROR: No area found for class '$cls_a'\n";
		} else {
			$tol = 1e-6;
			$auprc_b = $AUPRCB{$cls_a};
			$freq_b = $FREQB{$cls_a};
			if ((abs($auprc_a - $auprc_b) > $tol) || (abs($freq_a - $freq_b) > $tol)) {
				print "ERROR: $cls_a: $auprc_a <> $auprc_b, $freq_a <> $freq_b\n";
			} else {
				print "OK: $cls_a: $auprc_a = $auprc_b, $freq_a = $freq_b\n";
			}
		}
	}
}
close(B);

