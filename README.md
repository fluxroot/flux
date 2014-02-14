Flux Chess
==========

Copyright 2007-2014 Phokham Nonava <pn@nonava.com>  
http://fluxchess.com

[![Build Status](https://travis-ci.org/fluxroot/flux.png?branch=2.x)](https://travis-ci.org/fluxroot/flux) [![Coverage Status](https://coveralls.io/repos/fluxroot/flux/badge.png?branch=2.x)](https://coveralls.io/r/fluxroot/flux?branch=2.x)


Introduction
------------
Flux Chess is an attempt to write a computer chess engine entirely in 
Java. The speed of most Java Virtual Machines is improving nowadays. 
However compared to other C/C++ chess engines Flux is still slow (or 
maybe my algorithms are not that sophisticated?). 

As a pure UCI chess engine, Flux does not have a graphical user 
interface. You can load him however into your favorite chess program if 
it supports the UCI protocol. 


Features
--------
Below you will find a small list of what I have already implemented in 
Flux. Nothing special though as many computer chess engines have those 
features as well. 

In addition Flux supports multi pv output, which means Flux can show 
multiple best pv lines. 

### Search
- 0x88 Board Representation
- Iterative Deepening
- Internal Iterative Deepening
- Principal Variation Search
- Aspiration Windows
- Static Exchange Evaluation
- Killer Heuristic
- History Heuristic

### Prunings and Extensions
- Mate Distance Pruning
- Verified Null Move Pruning
- Futility Pruning, Extended Futility Pruning
- Late Move Reductions
- One Reply Extensions
- Check Extensions
- Passed Pawn Extensions
- Recapture Extensions
- Mate Threat Extensions

### Tables
- Transposition Table with Zobrist Hashing
- Evaluation Hash Table
- Pawn Hash Table


License
-------
Flux Chess is released under version 3 of the [LGPL].


[LGPL]: http://www.gnu.org/copyleft/lgpl.html
