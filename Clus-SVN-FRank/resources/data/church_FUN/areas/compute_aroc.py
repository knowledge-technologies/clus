
import re
import os
import string
import sys

def read_classes(fname):
	cls = []
	idx = 0
	file = open('../'+fname, 'r')
	regexp = re.compile('^\\@ATTRIBUTE Original-p-([^\\s]+)\\s')
	for line in file:
		line = line.rstrip()
		m = regexp.match(line)
		if m:
			cls.append((idx, m.group(1)))
			idx = idx + 1
	file.close()
	return cls

def stripExt(name):
	index = name.rfind('.')
	if index != -1:
		return name[ : index ]
	else:
		return name

def write_header(fout):
	fout.write('@RELATION "Areas"\n\n')
	fout.write('@ATTRIBUTE class string\n')
	fout.write('@ATTRIBUTE npos numeric\n')
	fout.write('@ATTRIBUTE aroc numeric\n')
	fout.write('@ATTRIBUTE arochull numeric\n')
	fout.write('@ATTRIBUTE aprc numeric\n')	
	fout.write('\n@DATA\n')

def do_gle(clname, aroc, arochull, aprc, dir):
	fgle = open(clname + '.gle', 'w')
	fgle.write('size 16 8\n')
	fgle.write('amove 0 0\n')
	fgle.write('begin graph\n')
	fgle.write('   size 8 8\n')
	fgle.write('   title "'+clname+'"\n')
	fgle.write('   xtitle "FP"\n')
	fgle.write('   ytitle "TP"\n')
	fgle.write('   xaxis min 0 max 1\n')
	fgle.write('   yaxis min 0 max 1\n')
	fgle.write('   data "'+clname+'.roc"\n')
	fgle.write('   data "'+clname+'.hull.roc"\n')
	fgle.write('   d1 line color blue\n')
	fgle.write('   d2 line color red\n')
	fgle.write('end graph\n')
	fgle.write('begin key\n')
	fgle.write('   pos br\n')	
	fgle.write('   text "Area = "+format$('+str(aroc)+',"fix 4")\n')
	fgle.write('   text "Area under hull = "+format$('+str(arochull)+',"fix 4")\n')
	fgle.write('end key\n')	
	fgle.write('amove 8 0\n')
	fgle.write('begin graph\n')
	fgle.write('   size 8 8\n')
	fgle.write('   title "'+clname+'"\n')
	fgle.write('   xtitle "Recall"\n')
	fgle.write('   ytitle "Precision"\n')
	fgle.write('   xaxis min 0 max 1\n')
	fgle.write('   yaxis min 0 max 1\n')
	fgle.write('   data "'+clname+'_ipol.dat"\n')
	fgle.write('   d1 line color blue\n')
	fgle.write('end graph\n')
	fgle.write('begin key\n')
	fgle.write('   pos tr\n')	
	fgle.write('   text "Area = "+format$('+str(aprc)+',"fix 4")\n')
	fgle.write('end key\n')		
	fgle.close()
	os.system('gle -d pdf -o '+dir+clname+'.pdf '+clname+'.gle')
	
def do_one_class(c, fname, fout, dir, cls):
	(idx, oclname) = c
	nbcls = len(cls)
	clname = oclname.replace('/','-')
	print "Class: "+clname
	fcls = open(clname + '.arff', 'w')
	fcls.write('@RELATION '+clname+'\n\n')
	fcls.write('@ATTRIBUTE class {pos,neg}\n')
	fcls.write('@ATTRIBUTE predicted numeric\n')
	fcls.write('\n@DATA\n')
	file = open('../'+fname, 'r')
	for line in file:
		line = line.rstrip()
		arr = line.split(',')
		if len(arr) > 100:
			if float(arr[idx+1]) > 0.5:
				fcls.write('pos,'+arr[idx+1+nbcls]+'\n')
			else:
				fcls.write('neg,'+arr[idx+1+nbcls]+'\n')
	file.close()
	fcls.close()
	os.system('csvconvert -target class -roc '+clname+'.arff '+clname+'.roc')
	os.system('csvconvert -type1 csvh -type3 csv '+clname+'.roc '+clname+'.roc')
	npos = 0
	nneg = 0
	aroc = 0.0
	aprc = 0.0
	arochull = 0.0
	farea = open(clname + '.area', 'r')
	for line in farea:
		line = line.rstrip()
		(name, value) = line.split(',')
		if name == '#Pos':
			npos = float(value)
		if name == '#Neg':
			nneg = float(value)
		if name == 'predicted':
			aroc = float(value)
	farea.close()
	os.system('perl ROCCH-AREA.pl '+clname+'.roc > '+clname+'.hull.roc 2> '+clname+'.hull.area')
	farea = open(clname + '.hull.area', 'r')
	for line in farea:
		arochull = line.rstrip().split('=')[1].lstrip()
	farea.close()	
	os.system('csvconvert -selcols A2,A1 '+clname+'.arff '+clname+'.csv')
	os.system('perl computepr.pl '+clname+'.csv')
	os.system('perl ../ipol_pr.pl '+clname+'.pr > '+clname+'.prarea')
	farea = open(clname + '.prarea', 'r')
	regexp = re.compile('^Area = (.+)$')
	for line in farea:
		m = regexp.match(line.rstrip())
		if m:
			aprc = m.group(1)
	farea.close()	
	do_gle(clname, aroc, arochull, aprc, dir)
	os.system('rm '+clname+'.*')
	os.system('rm '+clname+'_ipol.dat')	
	fout.write(oclname+','+str(npos)+','+str(aroc)+','+str(arochull)+','+str(aprc)+'\n')
		
def do_all_classes(fname, dir):
	cls = read_classes(fname)
	foutname = stripExt(fname) + '.aroc.arff'
	fout = open(foutname, 'w')
	write_header(fout)
	#print cls
	#do_one_class((35, '01/01/06/06/01'), fname, fout, dir, cls)
	for c in cls:
		do_one_class(c, fname, fout, dir, cls)
	fout.close()

do_all_classes("church_FUN.test.pred.arff", "pdfs/")
