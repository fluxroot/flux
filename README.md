Flux Chess 2.2.1 UCI
====================

Copyright 2007-2012 Phokham Nonava <pn@nonava.com>  
http://www.fluxchess.com/


Introduction
------------
Flux Chess is an attempt to write a computer chess engine entirely in 
Java. The speed of most Java Virtual Machines is improving nowadays. 
However compared to other C/C++ chess engines Flux is still slow (or 
maybe my algorithms are not that sophisticated?). 

As a pure UCI chess engine, Flux does not have a graphical user 
interface. You can load him however into your favorite chess program if 
it supports the UCI protocol. I suggest you take a look at the free 
chess program Arena. I'm personally testing Flux with the Shredder 
Classic chess program. 

If you find any bugs or just want to write a comment, don't hesitate to 
contact me. 


Features
--------
Below you will find a small list of what I have already implemented in 
Flux. Nothing special though as many computer chess engines have those 
features as well. 

In addition Flux supports multi pv output, which means Flux can show 
multiple best pv lines. 

### Board
- 0x88 Board Representation

### Search
- Alpha-Beta Search
- Quiescent Search
- Iterative Deepening
- Internal Iterative Deepening
- Principal Variation Search
- Aspiration Windows

### Tables
- Transposition Table with Zobrist Keys
- Killer Moves
- History Heuristic

### Prunings
- Mate Distance Pruning
- Verified Null-Move Pruning
- Futility Pruning, Extended Futility Pruning
- Late Move Reduction

### Extensions
- Single-Reply Extension
- Check Extension
- Pawn Extension
- Recapture Extension
- Mate Threat Extension


System Requirements
-------------------
Because Flux is written in Java, you will have to install the Java 
Runtime Environment on your machine. Currently he only supports UCI for 
communicating with the GUI. If you want to use the XBoard interface, 
have a look at PolyGlot. 


Installation
------------
Just untar the tar file anywhere you like and add the Flux.bat (Windows) 
or Flux (Linux) as a new engine in your GUI. Make sure you select UCI as 
the protocol type. If you want to use the XBoard protocol there's a 
minimal PolyGlot INI file called polyglot.ini- Windows or 
polyglot.ini-Linux. Here's how to use the engine on linux with xboard: 

	xboard -fd <directory of unzipped Flux> -fcp 'polyglot polyglot.ini-Linux'


Configuration
-------------
Flux has its own configuration file and supports the UCI 
option/setoption commands. The configuration file is a plain ASCII file 
and should be placed in the same directory as Flux. Please note that the 
file should be called "Flux.ini". 

As it is also possible to set the same options in your chess GUI, Flux 
reads the options as follows: 

1. If a UCI setoption command is recognized, the option will be set 
	according to this value. 

2. If there's no setoption command, Flux tries to read the Flux.ini 
	file. 

3. If the option is not set in the Flux.ini file a compiled-in default 
	value is used. 

Flux recognizes the following options:

- Hash (4MB - 256MB, Default: 16MB)  
	The Hash Table size in megabytes

- Evaluation Table (4MB - 64MB, Default: 4MB)  
	The Evaluation Table size in megabytes

- Pawn Table (4MB - 16MB, Default: 4MB)  
	The Pawn Table size in megabytes


Build him
---------
Flux uses [Gradle](http://gradle.org) as build system. To build him from 
source, use the following steps. 

### Get him
`git clone https://github.com/fluxroot/fluxchess.git`

### Build him
`./gradlew build`

### Grab him
`cp build/distributions/Flux-2.2.1.tgz <very important engine directory>`


License
-------
Flux Chess is released under version 3 of the 
[GPL](http://www.gnu.org/copyleft/gpl.html). 


Acknowledgments
---------------
Writing a computer chess engine is not an easy task. I have learned a 
lot from the following sources. 

- Jonatan Pettersson  
	Mediocre Chess http://mediocrechess.blogspot.com/

- Ed Schröder  
	REBEL/Pro Deo http://members.home.nl/matador/chess840.htm

- Bruce Moreland  
	Gebril

- Fabien Letouzey  
	Fruit http://www.fruitchess.com/

- Leo Dijksman  
	http://wbec-ridderkerk.nl/

- The UCI Protocol  
	http://www.shredderchess.com/download.html

- The XBoard Protocol  
	http://home.hccnet.nl/h.g.muller/engine-intf.html


Changelog
---------
version 2.2.1 (26.06.2012):
* Prepared for open source. Licensed under GPL.
* This is a GitHub feature parity release of 2.2

version 2.2 (04.08.2007):
* Improved several internal data structures to speed up search
* Improved memory management
* Improved time management and pondering
* Improved game phase recognition
* Changed maximum output depth from 256 to 64 to work around a limitation in polyglot
* Changed extension handling to speed up search
* Fixed a race condition with UCI stop command
* Fixed several small bugs

version 2.1 (27.06.2007):
* Fixed major bug in time management
* Added UCI searchmoves
* Changed eval

version 2.0 (18.06.2007):
* Stable release of the 2.x series
* Added Clear Hash option
* Added multi pv output
* Added refutation output
* Changed version numbering
* Changed root move generation for multi pv output
* Changed from instable quick sort to stable insertion sort
* Changed pruning conditions
* Removed Limited Razoring
* Fixed... a lot of bugs

version II-0.9 (23.05.2007):
* Added Futility Pruning in Quiescent
* Added Mate Threat Extension
* Added more draw eval
* Improved time control
* Improved mate values in Transposition Table
* Fixed unstoppable passer eval
* Fixed race condition with pondering

version II-0.8 (11.05.2007):
* Added Late Move Reduction
* Added Pawn Hashtable
* Added Recapture Extension
* Completely revamped evaluation
* Extracting PV from search instead of the Transposition Table
* Reduced output on lower search depth
* Improved time control
* Improved search response to UCI stop command
* Changed search and pruning conditions
* Changed from Launch4j to JSmooth
* Fixed SEE with en passant moves
* Fixed deadlock in threading

version II-0.7 (22.04.2007):
* Added Static Exchange Evaluation (SEE)
* Added Futility Pruning, Extended Futility Pruning, Limited Razoring
* Added Verified Null-Move Pruning (currently disabled)
* Added Single-Reply Extension
* Added Transposition Table aging
* Added Transposition Table size configuration option
* Added engine properties file
* Added UCI movestogo
* Improved checking moves in quiescent
* Changed output to send each root move

version II-0.6 (11.04.2007):
* Rewrite move and chessman representation from object to int
* Rewrite search algorithm
* Rewrite evaluation
* Rewrite move generation
* Added Internal Iterative Deepening
* Added Evaluation Table
* Added UCI seldepth
* Added Mate Distance Pruning
* Added checking moves in quiescent
* Improved threading support
* Improved UCI protocol handling
* Changed exception preconditions to assertions
* Removed Refutation Table

version II-0.5 (09.03.2007):
* Rewrite check extension
* Added pondering
* Changed move generation order
* Changed evaluation function and position values
* Removed external JAR dependencies
* Fixed thinking output
* Fixed en passant bug

version II-0.4 (06.03.2007):
* First public release
* Added Flux.ini for PolyGlot
* Added README.txt
* Added FluxII.exe
* Changed Refutation Table implementation

version II-0.3:
* Changed check mate order
* Bugfix check mate/stale mate
* Added game phase recognition
* Added move number recognition
* Added double/triple pawn recognition
* Added check extension

version II-0.2:
* Changed position values
* Added final statements to improve speed
* Added Null-Move Forward Pruning
* Changed Refutation Table implementation

version II-0.1:
* Changed board representation to 0x88
* Added en passant move generation
* Added Perft tests
* Changed time control
* Added contempt factor
* Changed Transposition Table replacement scheme to "replace always"
* Added Refutation Table
* Added UCI go command time increment option
* Changed evaluation function

version 1.0:
* Added array board representation
* Moved protocol communication infrastructure to own project
* Changed search algorithm
* Changed evaluation function
* Added Zobrist Code generation
* Added Transposition Table
* Added move ordering
* Added quiescent search

version 0.4:
* Added centipawn calculation
* Added position values
* Added mate depth recognition
* Added FEN parsing

version 0.3:
* Added castling move generation
* Added en passant move
* Added stale mate check
* Added UCI ucinewgame command

version 0.2:
* Added Timer functionality
* Added UCI go command parameters
* Added UCI debug command
* Added UCI info command
* Added UCI stop command

version 0.1:
* Launch4j used to build windows exe
* Prepared protocol interface for XBoard and UCI
* Board representation supports 8x8 map board
