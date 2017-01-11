use strict;

my $trainfile = $ARGV[0]; # training file
#my $testfile = $ARGV[1]; # test file
my $nbtrees = $ARGV[1]; # number of trees to be run in the forest
my $rf_feat = $ARGV[2]; # number of features to use in the random forest, e.g. sqrt(total number)
my $maxdepth = $ARGV[3]; # max depth of the trees in the forest (a number or "Infinity")

my $path_to_clus = "/home/celine/clus/trunk/";

system("rm -rf forest");
system("mkdir forest");
chdir("forest");

system("cp ../$trainfile.arff .");
#system("cp ../$testfile.arff .");

&construct_s_and_run_forest_real();
chdir("..");


# construct settings file and run forest
sub construct_s_and_run_forest_real()
{
	open(S,">$trainfile.s");
	print S "[Model]\n";
	print S "MinimalWeight = 1.0\n\n";
	print S "[Tree]\n";
	print S "PruningMethod = None\n\n";
	print S "[Constraints]\n";
	print S "MaxDepth = $maxdepth\n\n";
	print S "[Output]\n";
	print S "WritePredictions = Test\n\n";
	print S "[Ensemble]\n";
	print S "EnsembleMethod = RForest\n";
	print S "Iterations = $nbtrees\n";
	print S "OOBestimate = Yes\n";
	print S "Optimize = Yes\n";
	print S "PrintPaths = Yes\n";
	print S "SelectRandomSubspaces = $rf_feat\n";
	print S "\n";
	print S "[Data]\n";
	print S "File = $trainfile.arff\n";
#	print S "TestSet = $testfile.arff\n";
	close(S);
	print "running forest\n";
	system("$path_to_clus/clus.sh -forest $trainfile.s > forestoutput.txt");
}

