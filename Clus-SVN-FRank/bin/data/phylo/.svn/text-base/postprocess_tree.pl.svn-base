###########################################################################################
#
# This script postprocesses the .out file. It extracts the tree from it and produces
# four files: .clus-phy_tree (that contains the tree in two formats), .clus-phy_newick
# (that contains the tree in newick format), .clus-phy_newick_labels (that contains the
# tree in newick format with the tests in the internal nodels of the tree), and .clusters
# (that lists the leaf node clusters, one per line, only relevant if a pruning method is used).
#
# USAGE:
# run "perl postprocess_tree.pl appl"
# where "appl" is the name of the settings file without ".s".
#
# The script is going to look first for the pruned tree
# If the pruned tree is not there, then it will look for the original tree
#
###########################################################################################

$appl=$ARGV[0];

# compact tree

open(OUT,"$appl.out") or die "$appl.out file was not found!\n";
open(POST,">$appl.clus-phy_tree") or die "can not open $appl.clus-phy_tree!\n";
open(TMP,">$appl.tmp") or die "can not open $appl.tmp!\n";

print "Producing phylogenetic tree structure...\n";

print POST "Phylogenetic tree structure:\n\n";

$line=<OUT>;
$PrunedTree=1;
while (($line !~ /Pruned Model/) & (!eof(OUT)))
{
	$line=<OUT>;
}
if(eof(OUT)){
  $PrunedTree=0;
  close(OUT);
  open(OUT,"$appl.out") or die "$appl.out file was not found!\n";
  $line=<OUT>;
  while (($line !~ /Original Model/) & (!eof(OUT)))
  {
	$line=<OUT>;
  }
}
$line=<OUT>;
$line=<OUT>;

while ($line=<OUT>)
{


	if (($line =~ /(.*)LEAF : (.+) sequence/)|(($line =~ /(.+)\[.*\] \[.*\]: (\d+)/)))
	{

		$prefix = $1;
		$nbseq = $2;

		# For the case when no tree is induced
		# The sequences remain all in one cluster
		if(!($prefix =~ /\+--/)){
			$prefix = "\+--yes: ";
		}

		print TMP "$prefix";
		$prefix =~ s/yes:/ /g;
		$prefix =~ s/no://g;
		print POST "$prefix";

		for $i (1..$nbseq)
		{
			$line=<OUT>;
			if ($line =~ /"(.+)"/)
			{
				print POST " $1";
				print TMP " $1";
			}
			else
			{
				print "no leaf name found\n";
			}
		}
		print POST "\n";
		print TMP "\n";
		$line=<OUT>;
		$line=<OUT>;
		$line=<OUT>;
		$line=<OUT>;
		$line=<OUT>;
		$line=<OUT>;
		$line=<OUT>;
		}
	elsif (($line =~ /(.+:).+=/) or ($line =~ /(.+:).+in/))
	{
		$prefix = $1;
		$prefix =~ s/yes://g;
		$prefix =~ s/no://g;
		print POST "$prefix CLUSTER\n";
		print TMP "$line";
	}
	elsif (($line =~ /=/) or ($line =~ /in/))
	{
		print TMP "$line";
	}
	else
	{
		print POST $line;
		print TMP $line;
	}
}
close(OUT);
close(POST);
close(TMP);


# newick

if(1){

print "Producing newick format...\n";

open(IN,"$appl.clus-phy_tree");
open(OUT,">$appl.clus-phy_newick") or die "can not open $appl.clus-phy_newick!\n";

if($PrunedTree){
  open(CLUS,">$appl.clusters") or die "can not open $appl.clusters!\n";
}
$line=<IN>;
&produce_newick_string_and_clustering();
print OUT ";";
close(IN);
close(OUT);
if($PrunedTree){
  close(CLUS);
}

# full tree

print "Producing phylogenetic tree with all splits listed...\n";

open(IN,"$appl.tmp");
@lines = <IN>;
close(IN);
open(OUT,">>$appl.clus-phy_tree");
print OUT "\n\nPhylogenetic tree with all splits listed:\n\n";
print OUT @lines;
close(OUT);
system("rm -f $appl.tmp");

# newick with labels

print "Producing newick format with all splits listed...\n";

open(IN,"$appl.clus-phy_tree");
open(OUT,">$appl.clus-phy_newick_labels") or die "can not open $appl.clus-phy_newick_labels!\n";
$line=<IN>;
while ($line !~ /Phylogenetic tree with all splits listed/)
{
	$line=<IN>;
}
$line=<IN>;
&produce_newick_string_with_labels();
print OUT ";";

close(IN);
close(OUT);

print "Done!\n";


}

# subroutines

sub produce_newick_string_with_labels {
	my $line=<IN>;

	if (($line =~ /\+--\S+:\s+(p\d+.*)$/) or ($line =~ /^(p\d+.*)$/))
	{
		my $name = "$1\n";
		chomp($name);
		print OUT "(";
		&produce_newick_string_with_labels();
		print OUT ",";
		&produce_newick_string_with_labels();
		print OUT ")\'$name\'";
	}
	else
	{
		$line =~ /\+--\S+:\s+(\S.+)$/ or die "wrong format sequence line: $line\n";
		my $seqs = $1;
		@sequences = split(" ",$seqs);
		produce_sequences(@sequences);
	}
}

sub produce_newick_string_and_clustering {
	my $line=<IN>;


	if (($line =~ /CLUSTER/) or ($line =~ /^\n$/))
	{
		print OUT "(";
		&produce_newick_string_and_clustering();
		print OUT ",";
		&produce_newick_string_and_clustering();
		print OUT ")";
	}
	else
	{
		$line =~ /\+--\s+(\S.+)$/ or die "wrong format sequence line: $line\n";

		if($PrunedTree){
		  print CLUS "$1\n";
		}
		@sequences = split(" ",$1);
		produce_sequences(@sequences);

	}
}

sub produce_sequences {
	@seqs = @_;
	$nbseq = @seqs;
	if ($nbseq == 1)
	{
		print OUT $seqs[0];
	}
	else
	{
		print OUT "$seqs[0]";
		for($i=1;$i<$nbseq;$i++){
		    print OUT "/$seqs[$i]";
		}
		print OUT "";

	}
}
