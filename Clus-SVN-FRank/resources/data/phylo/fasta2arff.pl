###########################################################################################
#
# This script converts a fasta file into arff format.
#
# USAGE:
# run "perl fasta2arff.pl fasta_file arff_file type" 
# where "type" is "dna" or "protein" for dna or protein sequences, respectively.
#
###########################################################################################

$fastafile = $ARGV[0];
$arfffile = $ARGV[1];
$seqtype = $ARGV[2];

open(IN,$fastafile) or die "can not open fasta file $fastafile\n";
$sequence_index = -1;
@names = ();
@sequences = ();
while ($line = <IN>)
{
    chomp($line);
    if ($line =~ /^\>\s*(\S+)\s*/)	# description line, takes everything between ">" and the first space as sequence identifier
    {
		$sequence_index++;
		$names[$sequence_index] = $1;
		$sequences[$sequence_index] = "";
    }
    else	# sequence line
    {
		$previousstring = @sequences[$sequence_index];
		$newstring = $previousstring . $line;
		$sequences[$sequence_index] = $newstring;
    }
}
close(IN);

$seqlength = length($sequences[0]);

open(ARFF,">$arfffile") or die "can not open arff file $arfffile\n";
print ARFF "\@relation $arfffile\n";
print ARFF "\@attribute name string\n";
if (($seqtype eq "dna") or ($seqtype eq "DNA"))
{
	for ($i=1; $i<=$seqlength; $i++)
	{
	    print ARFF "\@attribute p$i {A,C,G,T,-}\n";
	}
}
else
{
	for ($i=1; $i<=$seqlength; $i++)
	{
	    print ARFF "\@attribute p$i {A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y,-}\n";
	}
}

print ARFF "\n\@data\n";
for ($i=0;$i<=$sequence_index;$i++)
{
    $string = $sequences[$i];
    @array = split(//, $string);
    $newstring = join(',',@array);
    print ARFF "\"$names[$i]\",$newstring\n";
}
close(ARFF);