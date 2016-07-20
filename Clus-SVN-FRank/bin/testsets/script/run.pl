#!/usr/bin/perl

$file = $ARGV[0];
$file =~ s/^\.\///;

$stem = $file;
$stem =~ s/\.s$//;

if ($file eq $stem) {
   die "Not a settings file: '$file'";
}

open(LOG, ">>log.txt") || die "Can't create 'log.txt'";
print LOG "RUN: $file\n";
close(LOG);

$hasrun = 0;
open(IN, $file) || die "Can't open '$file'";
while (($hasrun == 0) && ($line = <IN>)) {
   chomp($line);
   if ($line =~ /^\%\s*\*EXCLUDE\*\s*$/) {
      open(LOG, ">>log.txt") || die "Can't create 'log.txt'";
      print LOG "*** EXCLUDED: $file!\n";
      close(LOG);
      $hasrun = 1;
   }
   if ($line =~ /^\%\s*\*RUN\*\s*$/) {
      $done = 0;
      $hasrun = 1;
      while (($done == 0) && ($line = <IN>)) {
         chomp($line);
         if (($line =~ /^\s*$/) || !($line =~ /^\s*\%/)) {
            $done = 1;
         } else {
            if (!($line =~ /^\s*\%\%/)) {
               $line =~ s/^\s*\%\s*//;
               $line =~ s/cdiff/perl script\/cdiff.pl/;
               $line =~ s/mdiff/perl script\/mdiff.pl/;
               $eval = "\$line =~ s/\\%f/$stem/g";
               eval($eval);
               print "$line\n";
               system($line);
            }
         }
      }
   }
}
close(IN);

if ($hasrun == 0) {
   die "No running information in '$file'";
}
