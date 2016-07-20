
$file = $ARGV[0];

open(IN, $file) || die "Can't open '$file'";
@LINES = <IN>;
close(IN);

open(OUT, ">$file") || die "Can't create '$file'";

print OUT "/*************************************************************************\n";
print OUT " * Clus - Software for Predictive Clustering                             *\n";
print OUT " * Copyright (C) 2007                                                    *\n";
print OUT " *    Katholieke Universiteit Leuven, Leuven, Belgium                    *\n";
print OUT " *    Jozef Stefan Institute, Ljubljana, Slovenia                        *\n";
print OUT " *                                                                       *\n";
print OUT " * This program is free software: you can redistribute it and/or modify  *\n";
print OUT " * it under the terms of the GNU General Public License as published by  *\n";
print OUT " * the Free Software Foundation, either version 3 of the License, or     *\n";
print OUT " * (at your option) any later version.                                   *\n";
print OUT " *                                                                       *\n";
print OUT " * This program is distributed in the hope that it will be useful,       *\n";
print OUT " * but WITHOUT ANY WARRANTY; without even the implied warranty of        *\n";
print OUT " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *\n";
print OUT " * GNU General Public License for more details.                          *\n";
print OUT " *                                                                       *\n";
print OUT " * You should have received a copy of the GNU General Public License     *\n";
print OUT " * along with this program.  If not, see <http://www.gnu.org/licenses/>. *\n";
print OUT " *                                                                       *\n";
print OUT " * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *\n";
print OUT " *************************************************************************/\n\n";

$skiphead = 1;

foreach $l (@LINES) {
	chomp($l);
	if (!($l =~ /^\s*$/)) {
		$skiphead = 0;
	}
	if ($skiphead == 0) {
		print OUT "$l\n";
	}
}
close(OUT);
