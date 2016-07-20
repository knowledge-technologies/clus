$file = $ARGV[0];

open(IN,"$file") || die "Can't open '$file'";
@LINES = <IN>;
close(IN);

$count = 0;
for ($i = 0; $i <= $#LINES; $i++) {
	$line = $LINES[$i];
	$line =~ s/[\r\n]+//g;
	$prev = $line;
	$line =~ s/\s+$//;
	if ($line ne $prev) {
		$count++;
	}
	$LINES[$i] = $line;
}

if ($count > 0) {
	print "F: $file - $count\n";
	open(OUT,">$file") || die "Can't create '$file'";
	for ($i = 0; $i <= $#LINES; $i++) {
		$line = $LINES[$i];
		print OUT "$line\n";
	}
	close(OUT);
}
