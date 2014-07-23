Perfclipse
-----

This application is intended to help developers optimize their applications using LoopPerforation. 

To get started, clone the repository, and import the project into eclipse as an existing project. After importing the project, find the file "Perfclipse/META-INF/MANIFEST.MF". Right click on the file and select Run As Eclipse Application. This will open a new eclipse instance. In this instance you can create a test project with a few methods and for loops to test. 

Extracting a for loop
-----

You can begin playing with perforations by first extracting the for loop you would like to perforate into a separate method. Highlight the for loop, then find the perforation menu in the top menu bar, then select extract loop. This will extract the for loop into a new method, and add an @Perforated annotation to it. 

The annotation will have a factor property. Change the value of this factor to choose the perforation factor for the function. Notice that the the for loop is initially unperforated. The factor lets the program know how much to perforate the for loop while running the program.

Running Individual Analysis on Extracted Loops
-----

Once you've extracted the loops that you would like to perforate, and set the factor at which you would like to perforate them at, you can run an individual analysis on them to see which loops are better for perforation. To do so, go to the Perforation menu and choose "Run Individual Analysis".  
