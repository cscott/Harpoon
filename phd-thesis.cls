% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.2 2005-06-07 17:41:15 cananian Exp $
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{phd-thesis}[2005/06/07 csa PhD thesis class]
\usepackage{beton}\usepackage{euler} % fonts

\usepackage{amstext} % the \text command for math mode (replaces \mbox)
\usepackage{varioref}
\usepackage{xspace}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{book}}

% Process the options.
\ProcessOptions

% Load book class.
\LoadClass[12pt,titlepage,twoside]{book}
\usepackage{bibcontents} % required by MIT thesis specification
\usepackage[section,plain]{algorithm} % algorithm environment,\listofalgorithms

\linespread{1.2} % ``double-space''

% definitions
\newcommand{\defn}[1]           {{\textit{\textbf{\boldmath #1}}}}
% A breaking non-space for URLs
\newcommand{\bp}{\hspace{0pt}}
% single-spacing for code examples.
\newcommand{\sis}{\linespread{1.0}\setlength{\baselineskip}{0.8\baselineskip}}
\newcommand{\punt}[1]{}% ignore punted stuff
