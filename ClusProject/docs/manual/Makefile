DVIPS=dvips
DVIPSFLAGS= -P pdf -ta4 -G0

LATEX=latex
LATEXFLAGS=

PDFLATEX=pdflatex
PDFLATEXFLAGS=

PSTOPDF=ps2pdf
PSTOPDFFLAGS=

DOCUMENT=clus-manual

INPUT_FILES= 
BIBTEX_FILES = 

all: fig $(TEXFILES) $(DOCUMENT).pdf
#all: $(DOCUMENT).ps

.PHONY: fig
fig:
	make -C fig

cfig:
	make -C fig clean

%.dvi:
	$(LATEX) $(LATEXFLAGS) $<

%.ps:
	$(DVIPS) $(DVIPSFLAGS) -o $@ $<

#%.pdf: %.ps
#	$(PSTOPDF) $(PSTOPDFFLAGS) $<

%.pdf: %.tex
	($(PDFLATEX) $(PDFLATEXFLAGS) $<) ; ($(PDFLATEX) $(PDFLATEXFLAGS) $<)


almostclean:
	-rm -f $(DOCUMENT).out $(DOCUMENT).aux $(DOCUMENT).dvi $(DOCUMENT).log $(DOCUMENT).bbl $(DOCUMENT).blg $(DOCUMENT).toc

clean:
	-rm -f $(DOCUMENT).out $(DOCUMENT).aux $(DOCUMENT).log $(DOCUMENT).dvi $(DOCUMENT).pdf $(DOCUMENT).ps $(DOCUMENT).bbl $(DOCUMENT).blg $(DOCUMENT).idx $(DOCUMENT).ilg $(DOCUMENT).ind $(DOCUMENT).toc

real_clean:
	-rm -f $(DOCUMENT).out $(DOCUMENT).aux $(DOCUMENT).log $(DOCUMENT).dvi $(DOCUMENT).pdf $(BEAMER_FILES)

touch:
	touch $(DOCUMENT).tex
	
view: $(DOCUMENT).pdf
	acroread $(DOCUMENT).pdf &
#view: vps

#draft: dps

draft:
	$(PDFLATEX) $(PDFLATEXFLAGS) $(DOCUMENT).tex
	acroread $(DOCUMENT).pdf &

vps: $(DOCUMENT).ps
	gv $(DOCUMENT).ps &
	
dps:
	($(LATEX) $(LATEXFLAGS) $(DOCUMENT).tex) ; ($(LATEX) $(LATEXFLAGS) $(DOCUMENT).tex)
	$(DVIPS) $(DVIPSFLAGS) $(DOCUMENT).dvi -o $(DOCUMENT).ps
	gv $(DOCUMENT).ps &

index:
	makeindex -s styles/indexcapromanatend.ist $(DOCUMENT).idx 

bib:
	bibtex $(DOCUMENT)

dvi:
	$(LATEX) $(LATEXFLAGS) $(DOCUMENT).tex

ps:
	$(DVIPS) $(DVIPSFLAGS) -o $(DOCUMENT).ps $(DOCUMENT).dvi

gv:
	gv $(DOCUMENT).ps &

pdf: 
	$(PDFLATEX) $(PDFLATEXFLAGS) $(DOCUMENT).tex

ps_pdf: $(DOCUMENT).ps
	$(PSTOPDF) $(PSTOPDFFLAGS) $<

spell_check: $(DOCUMENT).tex
	ispell -t $(DOCUMENT).tex

bibtex: $(DOCUMENT).dvi $(INPUT_FILES)
	bibtex $(DOCUMENT)

