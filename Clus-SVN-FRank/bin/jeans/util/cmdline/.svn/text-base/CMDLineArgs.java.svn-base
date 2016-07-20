/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package jeans.util.cmdline;

import java.util.*;

public class CMDLineArgs {

	protected CMDLineArgsProvider $prov;
	protected Hashtable $optargs = new Hashtable();
	protected String[] $mainargs;
	protected boolean $ok;
	protected int m_NbMainArgs;

	public CMDLineArgs(CMDLineArgsProvider prov) {
		$prov = prov;
		$mainargs = new String[prov.getNbMainArgs()];
	}

	public void process(String[] args) {
		int idx = 0;
		boolean done = false;
		String[] options = $prov.getOptionArgs();
		int[] arities = $prov.getOptionArgArities();
		while (idx < args.length && !done) {
			String arg = args[idx];
			if (arg.charAt(0) == '-' && arg.length() > 1) {
				idx++;
				arg = arg.substring(1);
				boolean found = false;
				for (int i = 0; i < options.length && !found; i++) {
					if (arg.equals(options[i])) {
						int arity = arities[i];
						if (args.length - idx >= arity) {
							if (arity == 0) $optargs.put(arg, this);
							else if (arity == 1) $optargs.put(arg, args[idx++]);
							else {
								String[] vals = new String[arity];
								for (int j = 0; j < arity; j++)
									vals[j] = args[idx+j];
								idx += arity;
								$optargs.put(arg, vals);
							}
						} else {
							$prov.showHelp();
							System.out.println();
							System.out.println("Option -"+arg+" requires "+arity+" arguments");
							return;
						}
						found = true;
					}
				}
				if (!found) {
					$prov.showHelp();
					System.out.println();
					System.out.println("Unknown option: -"+arg);
					return;
				}
			} else {
				done = true;
			}
		}
		int nbleft = args.length - idx;
		if (nbleft == $prov.getNbMainArgs()) {
			for (int i = 0; i < nbleft; i++)
				$mainargs[i] = args[idx+i];
			$ok = true;
		}
		m_NbMainArgs = nbleft;
	}

	public boolean allOK() {
		return $ok;
	}

	public String getMainArg(int idx) {
		return $mainargs[idx];
	}

	public boolean hasOption(String option) {
		return $optargs.get(option) != null;
	}

	public String getOptionValue(String option) {
		return (String)$optargs.get(option);
	}

	public int getOptionInteger(String option) {
		return Integer.parseInt((String)$optargs.get(option));
	}

	public int getOptionInteger(String option, int min, int max) throws IllegalArgumentException {
		String val = (String)$optargs.get(option);
		try {
			int res = Integer.parseInt(val);
			if (res < min || res > max)
				throw new IllegalArgumentException("Value "+val+" supplied for option '"+option+"' is out of range");
			return res;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal value '"+val+"' supplied for option '"+option+"': expected integer");
		}
	}

	public double getOptionDouble(String option, double min, double max) throws IllegalArgumentException {
		String val = (String)$optargs.get(option);
		try {
			double res = Double.parseDouble(val);
			if (res < min || res > max)
				throw new IllegalArgumentException("Value "+val+" supplied for option '"+option+"' is out of range");
			return res;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal value '"+val+"' supplied for option '"+option+"': expected real");
		}
	}

	public int getNbMainArgs() {
		return m_NbMainArgs;
	}

	public String getOptionValue(String option, int index) {
		String[] vals = (String[])$optargs.get(option);
		return vals[index];
	}

	public String[] getOptionValues(String option) {
		return (String[])$optargs.get(option);
	}
}
