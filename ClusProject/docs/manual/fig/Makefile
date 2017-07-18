
PDFFIG = iris-shot.pdf 3clusters.pdf

EPSFIG = $(addsuffix .eps, $(basename $(PDFFIG)))

all: $(PDFFIG)

iris-shot.pdf: iris-shot.gle
	gle -d eps -d pdf iris-shot.gle

3clusters.pdf: 3clusters.gle
	gle -d eps -d pdf 3clusters.gle

clean:
	rm -rf *.eps *.pdf

