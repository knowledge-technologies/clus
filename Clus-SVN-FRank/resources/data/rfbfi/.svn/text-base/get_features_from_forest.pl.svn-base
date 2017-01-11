use strict;

# Given a forest of trees, in directory $file/forest, we take the first $nbtrees treeX.path files, and construct for each example a new binary feature vector. 

my $trainfile = $ARGV[0]; # training file name
#my $testfile = $ARGV[1]; # test file name
my $nbtrees = $ARGV[1]; # number of trees in the forest

&all_trees();
# &all_trees_hash(500);

sub all_trees()
{
	my %feathash = ();
	my $offset = 0;
	chdir("forest");
	for (my $i=1; $i<=$nbtrees; $i++)
	{
		my %treefeathash = (); # hash to store for each example the features it takes in the tree
		my $nbnodes = &one_tree($i,\%treefeathash);
		
		# put everything from treefeathash to global feathash, and update features with offset for current tree
		# foreach my $set ("train","test")
		foreach my $set ("train")
		{
			my @keys = keys %{$treefeathash{$set}};
			foreach my $key (@keys)
			{
				my @feats = @{$treefeathash{$set}{$key}};
	 			#print "set $set key $key feats: " . @feats . "\n";
				foreach my $feat (@feats)
				{
					$feat += $offset;
					push @{ $feathash{$set}{$key} }, $feat;
				}
			}
		}
		$offset += $nbnodes;
		if ($offset >= 4294967295)
		{
			die "feature vector runs over address space of 2^32\n";
		}
 		# print "nbnodes = $nbnodes, new offset = $offset\n";
	}	
		
	# construct transformed arff file
	chdir("..");
	construct_transformed_files("train",$trainfile,\%feathash,$offset);
#	construct_transformed_files("test",$testfile,\%feathash,$offset);
}


sub one_tree()
{
	my $tree = $_[0];
	my $hash = $_[1];
	my $nbnodes = 0;
	open(IN,"tree_$tree.path") or die "file tree_$tree.path not found\n";
	my @lines = <IN>;
	
	# put lines in random order and process them one by one, then we will get one random path for cases with missing values
	&fisher_yates_shuffle(\@lines);

	foreach my $line (@lines)
	{
		my @parts = split(/\s+/, $line);
		my $index = $parts[0]; # example index
		my $path = $parts[2];
		my @features = split("_",$path);
		
		# each line consists of the index and path of the example (+ some leaf information)
		if ($line =~ /TEST/) # the test examples
		{
			$$hash{"test"}{$index} = [ @features ]; # missing values get the last assignment in a random sequence
		}
		else # both OOB and Train (= everything except test examples)
		{
			$$hash{"train"}{$index} = [ @features ]; # missing values get the last assignment in a random sequence
		}
		
		my $nbfeatures = @features;
		if ($features[$nbfeatures-1] > $nbnodes)
		{
			$nbnodes = $features[$nbfeatures-1];
		}
	}
	close(IN);
	return $nbnodes;
}


sub construct_transformed_files()
{
	my $set = $_[0];
	my $file = $_[1];
	my $hash = $_[2];
	my $nbatts = $_[3];
	print "constructing transformed files\n";
	&write_arff_header("${file}.transformed.arff","${file}.arff", $nbatts);
	open(ARFF,">>${file}.transformed.arff");
	my $index = -1;

	open(IN,"${file}.arff") or die "can not open file ${file}.arff\n";
	my $l = <IN>;
	while ($l !~ /\@data/)
	{
		$l = <IN>;
	}
	while ($l=<IN>)
	{
		$index++;
		$l =~ s/\s+$//; #remove trailing spaces
		$l =~ /,\s*([^,\s]+)$/ or die "wrong line $l"; # get class value
		my $class = $1;
		my @features = @{ $$hash{$set}{$index} };
		my @sortedfeatures = sort { $a <=> $b } @features; # numerical sort 
		my $uniquesortedfeatures = remove_duplicates_keep_order(\@sortedfeatures); # to deal with missing values, which have multiple paths per tree
		
		print ARFF "\{";
		foreach my $feat (@$uniquesortedfeatures)
		{
			print ARFF "$feat 1, ";
		}
		my $arffclassfeat = $nbatts + 1;
		print ARFF "$arffclassfeat $class\}\n";
	}
	close(IN);
	close(ARFF);
}


sub write_arff_header()
{
	my $file = $_[0];
	my $oldfile = $_[1]; # file to extract class info, assumes that class attribute is denoted with "class" or "Class"
	my $nbatts = $_[2];
	print "writing arff header with $nbatts attributes\n";
	open(OUT,">$file") or die "can not write to $file\n";
	print OUT "\@relation transformed\n\n";
	for (my $i=1;$i<=$nbatts;$i++)
	{
		print OUT "\@attribute att$i numeric\n";
	}
	open(OLD,"$oldfile") or die "can not open $oldfile\n";
	my $line=<OLD>;
	while (($line !~ /attribute.+class/) and ($line !~ /attribute.+Class/))
	{
		$line=<OLD>;
	}
	close(OLD);
	print OUT "$line\n";
	print OUT "\@data\n";
	close(OUT);
	print "arff header written\n";
}

sub remove_duplicates_keep_order()
{
	my $array = $_[0];
	my @unique = ();
   	my %seen = ();
	foreach my $elem ( @$array )
	{
		next if $seen{ $elem }++;
		push @unique, $elem;
	}
	return \@unique;
}

# permutes @array in place
sub fisher_yates_shuffle()
{
	my $array = shift;
	my $i;
	for ($i = @$array; --$i; ) 
	{
		my $j = int rand ($i+1);
		next if $i == $j;
		@$array[$i,$j] = @$array[$j,$i];
	}
}
