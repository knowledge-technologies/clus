$dataset = $ARGV[0]; # name of dataset, e.g. credit

$nbtrees = 50;
$ensemble = "RForest";
$maxdepth = "Infinity";
$rf_feat = &calculate_rf_features("$dataset/$dataset.arff");

chdir($dataset);
system("perl ../run_forest.pl $dataset $nbtrees $rf_feat $maxdepth $ensemble");
system("perl ../get_features_from_forest.pl $dataset $nbtrees");
chdir ("..");	


sub calculate_rf_features()
{
	my $arfffile = $_[0];
	
	my $totnbfeat = `grep '\@attribute' $arfffile | wc -l`;
	$totnbfeat = $totnbfeat - 1; # don't count class attribute
	my $rf_feat = int(sqrt($totnbfeat) + .5);
	return $rf_feat;
}
