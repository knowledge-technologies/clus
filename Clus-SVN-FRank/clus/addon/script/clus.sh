#!/bin/sh
export CLUS_DIR=$HOME/Clus
java -Xmx800m -cp "$CLUS_DIR/bin:$CLUS_DIR/jars/commons-math-1.0.jar:$CLUS_DIR/jars/jgap.jar" -Djava.library.path=$CLUS_DIR/jars clus.Clus $*
