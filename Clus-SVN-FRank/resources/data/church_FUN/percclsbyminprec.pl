
$ifile = $ARGV[0];
$ofile = $ifile;
$ofile =~ s/\.out/.pcbmp.csv/;

open(RES, $ifile) || die "Can't open '$ifile'";
skip_till("^Testing error");
skip_till("^\\s+[0-9]+\:");
$done = 0;
$nbcls = 0;
while ($done == 0) {
	if ($line =~ /^\s+[0-9]+\:.*P20R\:\s+([^\,]+)\,/) {
		$prec = $1;
		$nbcls++;
		push @PRECS, $prec;
	} else {
		$done = 1;
	}
	if ($line = <RES>) {
		$line =~ s/[\n\r]//g;
	} else {
		$done = 1;
	}
}
close(RES);

@PREC_S = sort { $b <=> $a } @PRECS;

print "Number of classes: $nbcls\n";

$count = 0;
$prev = 200;
open(OUT, ">$ofile") || die "Can't create $ofile";
print OUT "100,0\n";
for ($i = 0; $i < $nbcls; $i++) {
	$value = $PREC_S[$i];
	if (($value != $prev) && ($prev != 200)) {
		# $splitpoint = ($value + $prev) / 2.0;
		$splitpoint = $prev;
		$perc = $count / $nbcls * 100;
		print OUT "$splitpoint,$perc\n";
	}
	$count++;
	$prev = $value;
}
print OUT "0,100\n";
close(OUT);

sub skip_till {
    my ($to) = @_;
    $notfound = 0;
    $toeval = "\$line =~ /$to/";
    $line = <RES>;
    $line =~ s/[\n\r]//g;
    while ((!eval($toeval)) && ($notfound == 0)) {
        if ($line = <RES>) {
            $line =~ s/[\n\r]//g;
        } else {
            $notfound = 1;
        }
    }
    if ($notfound == 1) {
        die "Could not find: $to";
    }
}
