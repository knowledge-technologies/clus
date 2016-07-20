###########################################################################################
#
# This script runs Clus-HSC, starting from the HMC data and settings file.
# (The settings file can be changed to optimise the ftest for HSC.)
#
# RESULT:
# file.combined.out
#
# USAGE:
# Put the HMC datafiles, the HMC settingsfile, and this file in the same directory and
# run "perl run_hsc.pl name" where "name" is the name of the settingsfile, without ".s".
#
# To only compute the ".hsc.combined.out" file from an existing set of runs,
# run "perl run_hcs.pl -avg name" instead
###########################################################################################

# Overwrite this variable to point to the Clus directory
$clusdir = "$ENV{HOME}/Clus";

$clusmem = "1800000000";

$do_run_clus = 1;

if ($ARGV[0] eq "-avg") {
	$sfile = $ARGV[1];
	$do_run_clus = 0;
} else {
	$sfile = $ARGV[0];
}

# Run clus for each parent-child pair separately
# ----------------------------------------------
if ($do_run_clus == 1) {
    run_clus("addon.hmc.HMCNodeWiseModels.hmcnwmodels.HMCNodeWiseModels ${sfile}.s");
}

# Combine the output
# ------------------
run_clus("addon.hmc.HMCAverageSingleClass.HMCAverageSingleClass -hsc -models hsc/model ${sfile}.s");


# Run clus
sub run_clus {
    my($args) = @_;
    if (-f "$clusdir/bin/clus/Clus.class") {
        # Using the CVS version or a self-compiled Clus
        $clus_cp = "$clusdir/bin:$clusdir/jars/commons-math-1.0.jar:$clusdir/jars/jgap.jar";
    } else {
        # If Clus was downloaded, then use included pre-compiled .jar file
        $clus_cp = "$clusdir/Clus.jar";
    }
    $cmd = "java -Xmx$clusmem -cp $clus_cp $args";
    print "Running: $cmd\n";
    system($cmd);
}
