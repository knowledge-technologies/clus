
$target = "release/Clus";

$has_line = 0;
open(IN, "data/release-s-files.txt") || die "Can't open 'release-s-files.txt'";
while (($has_line == 1) || ($line = <IN>)) {
	chomp($line);
	$has_line = 0;
	if ($line =~ /^\$\$FILE\:\s+(\S+)\/([^\/]+)$/) {
		$dir = $1;
		$file = $2;
		system("mkdir -p $target/$dir");
		print "$dir/$file\n";
		open(OUT, ">$target/$dir/$file") || die "Can't create '$target/$dir/$file'"; 
		while (($has_line == 0) && ($line = <IN>)) {
			chomp($line);
			if ($line =~ /^\$\$FILE\:/) {
				$has_line = 1;
			} elsif ($line =~ /^\$\$COPY\:\s*(\S+)$/i) {
				try_copy($1);
			} else {
				print OUT "$line\n";
				if ($line =~ /^File\s*\=\s*(\S+)$/i) {
					try_copy($1);
				}
				if ($line =~ /^PruneSet\s*\=\s*(\S+)$/i) {
					try_copy($1);
				}
				if ($line =~ /^TestSet\s*\=\s*(\S+)$/i) {
					try_copy($1);
				}
			}
		}
		close(OUT);
		$arff_name = $file;
		$arff_name =~ s/\.s$/.arff/;
		try_copy($arff_name);
		try_copy("README.txt");
	} elsif (!($line =~ /^\s*$/)) {
		die "Illegal line: '$line'";
	}
}
close(IN);

foreach $file (keys(%TOCOPY)) {
	print "Copy $file\n";
	system("cp $file $target/$file");
}

sub try_copy {
	my ($file) = @_;
	my ($name) = "$dir/$file";
	while (($name ne "") && ($name =~ s/\/[^\/]+\/\.\.\//\//)) {
	}
	if ($name ne "") {
		if ((-f $name) && !defined($TOCOPY{$name})) {
			$TOCOPY{$name} = 1;
		}
	}
}
