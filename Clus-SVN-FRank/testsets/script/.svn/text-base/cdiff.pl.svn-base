#!/usr/bin/perl

$f1 = $ARGV[0];
$f2 = $ARGV[1];

sub complain_die {
   my($msg) = @_;
   if (open(LOG, ">>log.txt")) {
      print LOG "ERROR: $msg\n";
      close(LOG);
   }
   die $msg;
}

sub copy_filter {
    my($a, $b) = @_;
    open(IN, $a) || complain_die "Can't open '$a'";
    open(OUT, ">$b") || complain_die "Can't create '$b'";
    $found_run = 0;
    while (($found_run == 0) && ($line = <IN>)) {
       chomp($line);
       if ($line =~ /^Run\:/) {
          $found_run = 1;
       }
    }
    while ($line = <IN>) {
       $line =~ s/[\n\r]//g;
       if (!($line =~ /^Induction Time\:/i) &&
           !($line =~ /^Pruning Time\:/i)) {
          print OUT "$line\n";
       }
    }
    close(OUT);
    close(IN);
}

copy_filter($f1, "/tmp/clus-diff-1.txt");
copy_filter($f2, "/tmp/clus-diff-2.txt");

$diff = `diff /tmp/clus-diff-1.txt /tmp/clus-diff-2.txt`;

if (!($diff =~ /^[\n\r\t\s]*$/)) {
    print "Diff: $f1 <-> $f2\n";
    print "$diff";
    open(LOG, ">>log.txt") || die "Can't create 'log.txt'";
    print LOG "Diff: $f1 <-> $f2\n";
    print LOG "$diff\n";
    close(LOG);
}
