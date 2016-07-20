#!/bin/sh
find . -name "*.s" -exec perl script/clean.pl {} \;
