
$input = `./ROCCH $ARGV[0]`;

@lines = split(/[\n\r]+/, $input);

$first = 0;
$area = 0.0;
foreach $line (@lines) {
	if ($line =~ /^([0-9\.\+\-Ee]+)\s+([0-9\.\+\-Ee]+)\s+/) {
		$FP = $1; $TP = $2;
		print "$FP,$TP\n";
		if ($first != 0) {
			$mean = ($TP + $TP_prev)/2;
			$delta = $FP - $FP_prev;
			$area += $mean * $delta;
		}
		$TP_prev = $TP;
		$FP_prev = $FP;
		$first = 1;
	}
}
print STDERR "Area = $area\n";
