###########################################################################################
#
# This script converts a phy file into arff format.
#
# We assume that the sequence identifier is 10 characters long and is immediately
# followed by the sequence. There should be no more than one line per sequence.
#
# USAGE:
# run "perl phy2arff.pl phy_file arff_file type" 
# where "type" is "dna" or "protein" for dna or protein sequences, respectively.
#
###########################################################################################

$phyfile = $ARGV[0];
$arfffile = $ARGV[1];
$seqtype = $ARGV[2];

open(IN,$phyfile) or die "can not open phy file $phyfile\n";

open(OUT,">$name.arff");

# first line contains number of sequences and sequence length
$line = <IN>;
$line =~ /\s*(\d+)\s*(\d+)/;
$n = $1;
$l = $2;

@names = ();
@sequences = ();
for ($i=0; $i<$n; $i++) {
  $line = <IN>;
  chomp($line);
  $name = substr($line, 0, 10);
  $sequence =  substr($line, 10);
  $sequence =~ s/\s//g; # delete spaces that could occur in the sequence
  $names[$i] = $name; 
  $sequences[$i] = $sequence;
}

# .arff output
open(ARFF,">$arfffile") or die "can not open arff file $arfffile\n";
print ARFF "\@relation $arfffile\n";
print ARFF "\@attribute name string\n";
if (($seqtype eq "dna") or ($seqtype eq "DNA"))
{
	for ($i=1; $i<=$l; $i++)
	{
	    print ARFF "\@attribute p$i {A,C,G,T,-}\n";
	}
}
else
{
	for ($i=1; $i<=$l; $i++)
	{
	    print ARFF "\@attribute p$i {A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y,-}\n";
	}
}

print ARFF "\n\@data\n";
for ($i=0;$i<$n;$i++)
{
    $string = $sequences[$i];
    @array = split(//, $string);
    $newstring = join(',',@array);
    print ARFF "\"$names[$i]\",$newstring\n";
}
close(ARFF);
