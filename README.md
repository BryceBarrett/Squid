<img src="https://github.com/CIRDLES/DRAKE/blob/master/logos/Squid/SquidLogo.png" width="100">

Squid 3
=======

[![Build Status](https://travis-ci.org/CIRDLES/Squid.svg?branch=master)](https://travis-ci.org/CIRDLES/Squid)

Please visit [wiki](https://github.com/CIRDLES/Squid/wiki).

We encourage you to download
and evaluate the latest release, the ".jar" file found <a href="https://github.com/CIRDLES/Squid/releases" target="_blank">here</a>.  
Squid requires <a href="http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html" target="_blank">Java 1.8</a>.

To contribute issues and feedback, please join GitHub <a href="https://github.com/" target="_blank">here</a>. While using Squid3,
the help menu provides a link that
initializes a new GitHub issue for you - please use freely.

**Request** The current version is only tested on a few 9-, 10-, and 11-species Prawn xml files.  We would like to have more examples of Prawn files and Squid2.5 tasks in Excel for testing.

The current versions - Squid3-**1.0.n.jar** - prototype the development of the
workflow support including project, task, and expression management, reporting,
and visualization features
of Squid.  Squid provides for creating and saving Squid Project files that have an
extension of ".squid" and are intended to save the current state of work on a project.

The Project menu provides for creating a new project from a single Prawn XML file
or by joining together two Prawn XML files.  The project manager currently allows for naming the project,
naming the analyst, and reviewing statistics about the Prawn XML file.
We encourage you to
use it to experiment with managing your Prawn XML files by merging, splitting,
and editing them.  

The PrawnFile menu provides 1) Manage Spots and Reference Materials, 
2) Manage Sample Naming, and 3) Audit Masses.
These give a summary and in-depth look at spots, duplicate names for spots,
species statistics, and scans statistics.  Manage Spots supports editing spot names,
filtering spots by name, and setting both the project's reference material spots and 
concentration reference material spots by using the same
filtering feature.  The context menu on the left-side list is accessed by a
right-mouse click and has items for removing a spot and
splitting the Prawn file into two files.  Any modifications to the Prawn file can be saved
to a new Prawn XML file if needed.  Note that saving the project preserves the changes to the
Prawn XML data within the project file but without changing the source XML file.  Manage Sample Naming automates with overrides the naming of samples basedon delimeters or leading character counts.  Audit masses provides 
time-based views of masses and is configuable to show mass diferences as well.

The Task management functionality provides for customization, parameter-setting, isotope-labeling,
ratio-choosing, and expression management.  Squid supports importing tasks from Squid2.5 task files -
this is a work-in-progress that requires editing of expressions.  One key concept to remember
is that Squid2.5 conflated "equations" with report columns.  Squid 3 separates "expressions" from
report columns and thus simplifies expressions and reduces the population of them.

Thank you in advance for any suggestions you contribute.  We are available for walk-throughs, etc.
if needed to help convince you to participate and contribute.

Additional features will appear over time and may not be documented here concurrently.
Please be patient and contribute your ideas via the issues tracker.

Updated 4 August 2018
