
# Usage: perl prcurves.pl filename.out

# This creates a directory "AUC_average" with these files:
#    filename.areas:         file with computed areas for three measures
#    filename-overall.dat:   average curve for AU(\overline{PRC})
#    filename-avg.dat:       average curve for \overline{AUPRC}
#    filename-wavg.dat:      average curve for \overline{AUPRC_w}
#    filename-byclass.pr:    file with class-wise PR curve points

# Note: this script requires the script "ipol_pr.pl"

# The result of running this script can be plotted with "prcurves.gle"
# Using the GLE plotting tool available at <www.gle-graphics.org>

# The ILLEGAL variable contains those (root) classes every example belongs to, these are not taken into account in the calculation

$outfile = $ARGV[0];
$mainname = $outfile;
$mainname =~ s/\.[^\.]+$//;

@methods = ("avg", "wavg");

$ILLEGAL{"root"} = 1;
$ILLEGAL{"GO0003674"} = 1;
$ILLEGAL{"GO0005575"} = 1;
$ILLEGAL{"GO0008150"} = 1;

get_all_classes($outfile);
read_class_pr_values($outfile);

system("mkdir -p AUC_average/");
$prfile = "AUC_average/" . $mainname . "-overall.pr";
for ($i = $#allthres; $i >= 0; $i--) {
    calculate_threshold_pr_values($allthres[$i]);
}
get_area_file_overall($mainname . "-overall");

$prfile_bc = "AUC_average/" . $mainname . "-byclass.pr";
open(PRCURV, ">$prfile_bc") || die "Can't create '$prfile_bc'";
foreach $cls (@classes) {
   get_area_file_byclass($cls, $mainname);
}
close(PRCURV);
compute_other_avg_curves($prfile_bc, $prfile, "AUC_average/" . $mainname);
foreach $meth (@methods) {
   $file = "AUC_average/$mainname-$meth.dat";
   open(OUT, ">$file") || die "Can't create $file";
   print_file($meth);
   close(OUT);
}

sub skip_till {
    my ($to) = @_;
    $notfound = 0;
    $toeval = "\$line =~ /$to/";
    $line = <RES>;
    $line =~ s/[\n\r]//g;
    while ((!eval($toeval)) && ($notfound == 0)) {
        if ($line = <RES>) {
         $line =~ s/[\n\r]//g;
        } else {
            $notfound = 1;
        }
    }
    if ($notfound == 1) {
        die "Could not find: $to";
    }
}

sub find_colon {
    my($to) = @_;
    skip_till($to);
    if ($line =~ /[^\:]\:\s*(\S+)\s*$/) {
        return $1;
    } else {
        die "No colon on line $line, while searching $to";
    }
}

sub get_all_classes {
   # Read all classes with their frequencies from the .out file
   my ($file) = @_;
   open(RES, "$file") || die "get_all_classes: can't open '$file'";
   skip_till("^Testing error");
   $NBEXAMPLES = find_colon("Number of examples");
   skip_till("Hierarchical accuracy by class");
   skip_till("^\\s+T\\(0\\.0\\)\\s*\\:");
   $done = 0;
   $count_cls = 0;
   @classes = ();
   %default = ();
   if ($line =~ /precision:\s\S+,\srecall:\s\S+,\scoverage:\s\S+,\sTP:\s\S+,\sFP:\s\S+,\snbPos:\s(\S+)/) {
      $totNbPos = $1;
   } else {
      print "totNbPos not found on $line";
   }
   while (($line = <RES>) && ($done != 1)) {
      chomp($line);
      if ($line =~ /^\s+T\(/) {
         $done = 1;
      } elsif ($line =~ /^\s+(\S+)\[\S+\]\,\s+def\:\s*(\S+)\,.+nbPos\:\s*(\S+)/) {
         $cls = $1;
         $def = $2;
         $nbpc = $3;
	 # Compute class frequency (default) more accurately
	 $def = 1.0*$nbpc/$NBEXAMPLES;
      } elsif ($line =~ /^\s+([^\s\,]+)\,\s+def\:\s*(\S+)\,.+nbPos\:\s*(\S+)/) {
         $cls = $1;
         $def = $2;
         $nbpc = $3;
	 # Compute class frequency (default) more accurately
	 $def = 1.0*$nbpc/$NBEXAMPLES;
      } else {
         die "get_all_classes: illegal line: '$line'";
      }
      if ($done == 0) {
         if (($def != 0) && ($ILLEGAL{$cls} != 1)) {
            $default{$cls} = $def;
            $nbpos{$cls} = $nbpc;
            push @classes, $cls;
            $count_cls++;
         } else {
            $totNbPos -= $nbpc;
         }
       }

   }
   close(RES);
   print "totNbPos: $totNbPos \n";
}

sub read_class_pr_values {
   # Read all PR/RC values from the .out file
   my ($file) = @_;
   open(RES, "$file") || die "read_class_pr_values: can't open '$file'";
   skip_till("^Testing error");
   skip_till("Hierarchical accuracy by class");
   $done = 0;
   $thres = -1;
   @allthres = ();
   %precision = (); %recall = (); %TP = (); %FP = (); %nbPos = ();
   while (($line = <RES>) && ($done != 1)) {
      chomp($line);
      if ($line =~ /^\s*$/) {
         $done = 1;
      } elsif ($line =~ /^Hierarchical error measures/) {
         $done = 1;
      } elsif ($line =~ /^\s+T\(([0-9\.]+)\)/) {
         $thres = $1;
         push @allthres, $thres;
      } elsif ($line =~ /^\s+(\S+)\[\S+\], def: \S+, prec: (\S+), rec: (\S+), TP: (\S+), FP: (\S+), nbPos: (\S+)/) {
         # GO
         $cls = $1;
         if ($thres != -1) {
            $precision{$cls}{$thres} = $2;
            $recall{$cls}{$thres} = $3;
            $TP{$cls}{$thres} = $4;
            $FP{$cls}{$thres} = $5;
            $nbPos{$cls}{$thres} = $6;
         }
      } elsif ($line =~ /^\s+(\S+), def: \S+, prec: (\S+), rec: (\S+), TP: (\S+), FP: (\S+), nbPos: (\S+)/) {
         # FUNCAT
         $cls = $1;
         if ($thres != -1) {
            $precision{$cls}{$thres} = $2;
            $recall{$cls}{$thres} = $3;
            $TP{$cls}{$thres} = $4;
            $FP{$cls}{$thres} = $5;
            $nbPos{$cls}{$thres} = $6;
         }
      } else {
         die "read_class_pr_values: illegal line '$line'";
      }
   }
   close(RES);
}

sub calculate_threshold_pr_values {
   # From the read class_pr_values, calculate the values per threshold
   my($thres) = @_;
   $sumTP = 0;
   $sumFP = 0;
   foreach $cls (@classes) {
      # print "$cls, $thres, $TP{$cls}{$thres}, $FP{$cls}{$thres} \n";
      $sumTP += $TP{$cls}{$thres};
      $sumFP += $FP{$cls}{$thres};
   }
   $sumFN = $totNbPos - $sumTP;
   if ($sumTP + $sumFP != 0) {
      $thres_precision{$thres} = $sumTP / ($sumTP+$sumFP);
   }
   $thres_recall{$thres} = $sumTP / ($sumTP+$sumFN);
   $thres_TP{$thres} = $sumTP;
   $thres_FP{$thres} = $sumFP;
}

sub append_out {
   my($meth, $reca, $prec) = @_;
   # Append point to output
   # - Filter duplicates
   # - Filter sequences of constant precision
   if ($OUTCNT{$meth} > 0) {
      if (!(($OUTRECA{$meth}[$OUTCNT{$meth}-1] == $reca) && ($OUTPREC{$meth}[$OUTCNT{$meth}-1] == $prec))) {
         if (($OUTCNT{$meth} > 1) && (abs($OUTPREC{$meth}[$OUTCNT{$meth}-1]-$prec) < 1e-15) && (abs($OUTPREC{$meth}[$OUTCNT{$meth}-2]-$prec) < 1e-15)) {
            # Constant precision (horizontal line)
            $OUTRECA{$meth}[$OUTCNT{$meth}-1] = $reca;
         } elsif (($OUTCNT{$meth} > 1) && (abs($OUTRECA{$meth}[$OUTCNT{$meth}-1]-$reca) < 1e-15) && (abs($OUTRECA{$meth}[$OUTCNT{$meth}-2]-$reca) < 1e-15)) {
            # Constant recalll (vertical line)
            $OUTPREC{$meth}[$OUTCNT{$meth}-1] = $prec;
         } else {
            $OUTRECA{$meth}[$OUTCNT{$meth}] = $reca;
            $OUTPREC{$meth}[$OUTCNT{$meth}] = $prec;
            $OUTCNT{$meth}++;
         }
      }
   } else {
      $OUTRECA{$meth}[$OUTCNT{$meth}] = $reca;
      $OUTPREC{$meth}[$OUTCNT{$meth}] = $prec;
      $OUTCNT{$meth}++;
   }
}

sub compute_other_avg_curves {
   # Read data file for given data set / f-test pair
   my ($infile, $origpr, $outfile) = @_;
   open(IN, "$infile") || die "Can't open '$infile'";
   while ($line = <IN>) {
      chomp($line);
      if ($line =~ /^NEXT\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)$/) {
         $dname = $1;
         $area  = $2;
         $def   = $3;
         $cls   = $4;
         $DEF{$cls} = $default{$cls};
         $AREA{$cls} = $area;
         $COUNT{$cls} = 0;
         $ALLCLS{$cls} = 1;
      } elsif ($line =~ /^([0-9\+\-e\.]+)\,\s*([0-9\+\-e\.]+)$/) {
         $RC = $1; $PR = $2;
         $cnt = $COUNT{$cls};
         if (($cnt > 0) && ($RC_C{$cls}[$cnt-1] == $RC)) {
            # Found discontinuity in precision/recall curve
            $PR_C2{$cls}[$cnt-1] = $PR;
         } else {
            $RC_C{$cls}[$cnt] = $RC;
            $PR_C1{$cls}[$cnt] = $PR;
            $PR_C2{$cls}[$cnt] = $PR;
            $COUNT{$cls}++;
         }
         $ALLRC{$RC} = 1;
      } else {
         die "Error in line '$line'";
      }
   }
   close(IN);

   @CLASSES = sort keys %ALLCLS;
   $NBCLASSES = $#CLASSES+1;
   print "NB: $NBCLASSES\n";

   @RECLIST = keys %ALLRC;
   @RECALLS = sort {$a <=> $b} @RECLIST;

   foreach $meth (@methods) {
      $AVGSUM{$meth} = 0;
      $AVGCNT{$meth} = 0;
   }
   foreach $cls (@CLASSES) {
      if (($ILLEGAL{$cls} != 1) && ($DEF{$cls} != 0)) {
         $AVGSUM{"avg"} += $AREA{$cls};
         $AVGCNT{"avg"} += 1.0;
         $AVGSUM{"wavg"} += $DEF{$cls}*$AREA{$cls};
         $AVGCNT{"wavg"} += $DEF{$cls};
      }
   }
   foreach $meth (@methods) {
      $AVGAREA{$meth} = $AVGSUM{$meth} / $AVGCNT{$meth};
   }

   # Reset output arrays
   foreach $meth (@methods) {
      $OUTCNT{$meth} = 0;
   }

   foreach $cls (@CLASSES) {
      $POINTER{$cls} = 0;
   }

   $i = 0;
   $recalldiff = 0.005;
   $PREVPROG = 0;
   while ($i <= $#RECALLS) {
      $RC = $RECALLS[$i];
      $PROGRESS = int(10.0*$i/$#RECALLS);
      if ($PROGRESS != $PREVPROG) {
         print "Progress: $PROGRESS\n";
         $PREVPROG = $PROGRESS;
      }
      foreach $meth (@methods) {
         $SUMPR1{$meth} = 0; $SUMPR2{$meth} = 0; $CNTPR{$meth} = 0;
      }
      foreach $cls (@CLASSES) {
         if (($ILLEGAL{$cls} != 1) && ($DEF{$cls} != 0)) {
               $PRC1 = 0; $PRC2 = 0;
               $point = $POINTER{$cls};
               while (($point+1 < $COUNT{$cls}) && ($RC > $RC_C{$cls}[$point+1])) {
                  $point++;
               }
               if ($RC_C{$cls}[$point] == $RC) {
                  $PRC1 = $PR_C1{$cls}[$point];
                  $PRC2 = $PR_C2{$cls}[$point];
               } elsif ($RC_C{$cls}[$point+1] == $RC) {
                  $PRC1 = $PR_C1{$cls}[$point+1];
                  $PRC2 = $PR_C2{$cls}[$point+1];
               } elsif (($RC > $RC_C{$cls}[$point]) && ($RC < $RC_C{$cls}[$point+1])) {
                  $x0 = $RC_C{$cls}[$point];
                  $x1 = $RC_C{$cls}[$point+1];
                  $y0 = $PR_C2{$cls}[$point];
                  $y1 = $PR_C1{$cls}[$point+1];
                  $PRC1 = $y0 + ($y1-$y0)/($x1-$x0)*($RC-$x0);
                  $PRC2 = $PRC1;
               } else {
                  die "Error $RC_C{$cls}[$point] < $RC < $RC_C{$cls}[$point+1]";
               }
               $POINTER{$cls} = $point;
               $SUMPR1{"avg"} += $PRC1;
               $SUMPR2{"avg"} += $PRC2;
               $CNTPR{"avg"} += 1.0;
               $SUMPR1{"wavg"} += $DEF{$cls}*$PRC1;
               $SUMPR2{"wavg"} += $DEF{$cls}*$PRC2;
               $CNTPR{"wavg"} += $DEF{$cls};
         }
      }
      foreach $meth (@methods) {
         if ($CNTPR{$meth} == 0) {
            $AVGPR1 = 0;
            $AVGPR2 = 0;
         } else {
            $AVGPR1 = $SUMPR1{$meth} / $CNTPR{$meth};
            $AVGPR2 = $SUMPR2{$meth} / $CNTPR{$meth};
         }
         append_out($meth, $RC, $AVGPR1);
         if (abs($AVGPR1-$AVGPR2) > 1e-15) {
            append_out($meth, $RC, $AVGPR2);
         }
      }
      # Increase recall a bit further
      $ni = $i+1;
      while (($ni < $#RECALLS) && (abs($RECALLS[$ni]-$RC) < $recalldiff)) {
         $ni += 1;
      }
      $ni -= 1;
      if ($ni <= $i) {
         $ni = $i+1;
      }
      if ((abs($RECALLS[$ni]-$RC) >= $recalldiff) && ($ni > $i+1)) {
         die "ni = $ni, i = $i, prev-recall = $RC, new-recall = $RECALLS[$ni]";
      }
      $i = $ni;
   }

   $file = "$outfile.areas";
   open(OUT, ">$file") || die "Can't create $file";
   print OUT "overall,$ORIG_AREA\n";
   foreach $meth (@methods) {
      $area = compute_area($meth);
      print OUT "$meth,$AVGAREA{$meth},$area\n";
   }
   close(OUT);
   system("cat $outfile.areas");
}

sub compute_area {
   my($meth) = @_;
   $area = 0;
   $prev_RECA = $OUTRECA{$meth}[0]; $prev_PREC = $OUTPREC{$meth}[0];
   for ($i = 1; $i < $OUTCNT{$meth}; $i++) {
      $reca = $OUTRECA{$meth}[$i];
      $prec = $OUTPREC{$meth}[$i];
      $area += ($prec + $prev_PREC)/2.0*($reca - $prev_RECA);
      $prev_RECA = $reca; $prev_PREC = $prec;
   }
   return $area;
}

sub print_file {
   my($meth) = @_;
   for ($i = 0; $i < $OUTCNT{$meth}; $i++) {
      $reca = $OUTRECA{$meth}[$i];
      $prec = $OUTPREC{$meth}[$i];
      print OUT "$reca,$prec\n";
   }
}

sub get_area_file_overall {
   # Create RC/PR file
   my($dname) = @_;
   $fname = "AUC_average/${dname}.dat";
   open(OUT, ">$fname") || die "Can't create $fname";
   $nb_classes = @classes;
   $nb_examples = $NBEXAMPLES * $nb_classes;
   print OUT "TOT: $nb_examples\n";
   print OUT "POS: $totNbPos\n";
   $prev_line = "";
   for ($i = $#allthres; $i >= 0; $i--) {
      $thres = $allthres[$i];
      $line = "$thres_recall{$thres},$thres_precision{$thres},$thres_TP{$thres},$thres_FP{$thres}";
      if (defined($thres_precision{$thres}) && ($line ne $prev_line)) {
         print OUT "$line,$thres\n";
         $prev_line = $line;
      }
   }
   close(OUT);
   $result = `perl ipol_pr.pl $fname`;
   if ($result =~ /^Area\s\=\s+(\S+)$/) {
      $area = $1;
   } else {
      die "ipol_pr.pl results in: '$result'";
   }
   $ORIG_AREA = $area;
   system("rm -f AUC_average/${dname}.dat");
   system("mv AUC_average/${dname}_ipol.dat AUC_average/${dname}.dat");
}

sub get_area_file_byclass {
   # Create RC/PR file for given class and compute corresponding area
   my($cls, $dname) = @_;
   $printclass = $cls;
   $printclass =~ tr/\//\-/;
   $fname = "AUC_average/${dname}_${printclass}.dat";
   open(OUT, ">$fname") || die "Can't create $fname";
   print OUT "TOT: $NBEXAMPLES\n";
   print OUT "POS: $nbpos{$cls}\n";
   $prev_line = "";
   for ($i = $#allthres; $i >= 0; $i--) {
      $thres = $allthres[$i];
      $line = "$recall{$cls}{$thres},$precision{$cls}{$thres},$TP{$cls}{$thres},$FP{$cls}{$thres}";
      if (defined($recall{$cls}{$thres}) && ($line ne $prev_line)) {
         print OUT "$line,$thres\n";
         $prev_line = $line;
      }
   }
   close(OUT);
   $result = `perl ipol_pr.pl $fname`;
   if ($result =~ /^Area\s\=\s+(\S+)$/) {
      $area = $1;
   } else {
      die "ipol_pr.pl results in: '$result'";
   }
   $diff = abs($leander - $area);
   print ERR "$diff,$cls\n";
   print "NEXT $dname A=$area D=$default{$cls} $cls\n";
   print PRCURV "NEXT $dname $area $default{$cls} $cls\n";
   $fin = "AUC_average/${dname}_${printclass}_ipol.dat";
   open(IN, "$fin") || die "Can't open $fin";
   while ($line = <IN>) {
      chomp($line);
      print PRCURV "$line\n";
   }
   close(IN);
   system("rm -f AUC_average/${dname}_${printclass}*");
}
