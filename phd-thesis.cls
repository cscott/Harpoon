% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.18 2007-04-08 20:43:39 cananian Exp $
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{phd-thesis}[2005/06/07 csa PhD thesis class]
\RequirePackage{beton}\RequirePackage{euler} % fonts

\RequirePackage{amstext} % the \text command for math mode (replaces \mbox)
\RequirePackage{varioref}
\RequirePackage{xspace}
\RequirePackage{epsfig}
\RequirePackage{url}
\RequirePackage{ifthen}
\RequirePackage{verbatim}

% draft option puts timestamp in footer
\newboolean{@phddraft}\setboolean{@phddraft}{false}
\DeclareOption{draftnotice}{\setboolean{@phddraft}{true}}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{book}}

% Process the options.
\ProcessOptions

% Load book class.
\LoadClass[11pt,titlepage,twoside]{book}
\RequirePackage{bibcontents} % required by MIT thesis specification
\RequirePackage[section,plain]{algorithm} % algorithm environment,\listofalgorithms
\RequirePackage{fancyhdr}

\linespread{1.2} % ``double-space''

% header/footer layout
\pagestyle{fancy}
\fancyhead{}
\fancyhead[RO]{\slshape \rightmark}
\fancyhead[LE]{\slshape \leftmark}
\renewcommand{\headrulewidth}{0pt}
\renewcommand{\footrulewidth}{0pt}
\fancyhfoffset[RO]{.5in}
\fancyhfoffset[LE]{.5in}
\addtolength{\headheight}{3pt}
\ifthenelse{\boolean{@phddraft}}{\fancyfoot[LE,RO]{\small Draft: \today}}{}

% definitions
\newcommand{\defn}[1]           {{\textbf{\boldmath\defnlt{#1}}}}
\newcommand{\defnlt}[1]         {{\textit{#1}}}
\newcommand{\defni}[1]          {\defn{\MakeLowercase{#1}}\index{#1}}
\newcommand{\defnlti}[1]        {\defnlt{\MakeLowercase{#1}}\index{#1}}

\newcommand{\indexed}[1] {#1\index{#1}}
% A breaking non-space for URLs
\newcommand{\bp}{\hspace{0pt}}
% single-spacing for code examples.
\newcommand{\sis}{\linespread{1.0}\normalsize}
\newcommand{\punt}[1]{}% ignore punted stuff

\newcommand{\appref}[1]         {Appendix~\ref{sec:#1}}
\newcommand{\figref}[1]         {Figure~\ref{fig:#1}}
\newcommand{\secref}[1]         {Section~\ref{sec:#1}}
\newcommand{\Secref}[1]         {Section~\ref{sec:#1}}
\newcommand{\charef}[1]         {Chapter~\ref{cha:#1}}
\newcommand{\Charef}[1]         {Chapter~\ref{cha:#1}}

\newcommand{\figput}[3][]{%
\begin{figure}[tp]\sis%
 \begin{center}%
  \input{Figures/#2.pstex_t}%
 \end{center}%
 \ifthenelse{\equal{#1}{}}{\caption{#3}}{\caption[#1]{#3}}%
 \label{fig:#2}%
\end{figure}%
}
\newcommand{\epsfigput}[3][]{%
\begin{figure}[tp]\sis%
\begin{center}%
\includegraphics[height=2.75in,clip=true]{Figures/#2}% .eps/.pdf ext is default
\end{center}%
\ifthenelse{\equal{#1}{}}{\caption{#3}}{\caption[#1]{#3}}%
\label{fig:#2}%
\end{figure}%
}

% similar to verbatim, except it changes the size/spacing/etc
\newenvironment{inlinecode}%
  {\par\linespread{0.9}\footnotesize\samepage\verbatim}%
  {\endverbatim}

% marginal notes, lifted from supertech.sty
%\setlength{\marginparwidth}{0.6in}
%\reversemarginpar
\newif\ifnotes
\notestrue
\newcommand{\longnote}[1]{
  \ifnotes
    {\medskip\noindent Note: \marginpar[\hfill$\Longrightarrow$]
      {$\Longleftarrow$}{#1}\medskip}
  \fi}
\newcommand{\note}[1]{% lines that do not end with control sequences should end with macros to avoid introducing spurious space -Bradley
  \ifnotes
    {\marginpar{\tiny \raggedright{#1}}}%
  \fi}


% work around fig2dev wanting to add colors to .figures.
\providecommand{\color}[2][rgb]{}
