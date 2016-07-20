
$in = $ARGV[0];
$out = $in;
$out =~ s/\.arff//;
$out = $out . ".sparse.arff";

open(IN, "$in") || die "Can't open input file";
open(OUT, ">$out") || die "Can't create '$out'";
$done = 0;
while (($done == 0) && ($line = <IN>)) {
	chomp($line);
	print OUT "$line\n";
	if ($line =~ /^\@DATA/) {
		$done = 1;
	}
}

if ($done == 0) {
	die "Can't find \@DATA section in '$in'";
}

$avg = 0;
$row = 0;
$nb_att = -1;
while ($line = <IN>) {
	chomp($line);
	@attr = split(/\s*\,\s*/, $line);
	$nb = $#attr+1;
	if ($nb_att == -1) {
		$nb_att = $nb;
	} elsif ($nb_att != $nb) {
		$nr = $row + 1;
		die "Data line $nr has incorrec number of attributes: $nb <> $nb_att";
	}
	$idx = 0;
	print OUT "{";
	for ($i = 0; $i < $nb; $i++) {
		if ($attr[$i] ne "0") {
			if ($idx != 0) { print OUT ","; }
			$ai = $i+1;
			print OUT "$ai $attr[$i]";
			$idx++;
		}
	}
	print OUT "}\n";
	$avg += $idx;
	$row++;
}

close(OUT);
close(IN);

$avg = $avg / $row;
print "Rows: $row, Attributes: $nb_att, Average nonzero: $avg\n";



