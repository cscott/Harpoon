% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.1 2005-06-07 17:24:27 cananian Exp $
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{phd-thesis}[2005/06/07 csa PhD thesis class]
\usepackage{beton}\usepackage{euler} % fonts

\usepackage{amstext} % the \text command for math mode (replaces \mbox)
\usepackage{varioref}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{book}}

% Process the options.
\ProcessOptions

% Load book class.
\LoadClass[12pt,titlepage,twoside]{book}
\usepackage{bibcontents} % required by MIT thesis specification
\usepackage[section,plain]{algorithm} % algorithm environment,\listofalgorithms

\linespread{1.2} % ``double-space''
