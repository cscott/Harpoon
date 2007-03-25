% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.12 2007-03-25 01:08:27 cananian Exp $
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{phd-thesis}[2005/06/07 csa PhD thesis class]
\usepackage{beton}\usepackage{euler} % fonts

\usepackage{amstext} % the \text command for math mode (replaces \mbox)
\usepackage{varioref}
\usepackage{xspace}
\usepackage{epsfig}
\usepackage{url}
\usepackage{ifthen}
\usepackage{verbatim}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{book}}

% Process the options.
\ProcessOptions

% Load book class.
\LoadClass[11pt,titlepage,twoside]{book}
\usepackage{bibcontents} % required by MIT thesis specification
\usepackage[section,plain]{algorithm} % algorithm environment,\listofalgorithms

\linespread{1.2} % ``double-space''

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
\newcommand{\charef}[1]         {Chapter~\ref{sec:#1}}

\newcommand{\figput}[3][]{%
\begin{figure}[t]\sis%
 \begin{center}%
  \input{Figures/#2.pstex_t}%
 \end{center}%
 \ifthenelse{\equal{#1}{}}{\caption{#3}}{\caption[#1]{#3}}%
 \label{fig:#2}%
\end{figure}%
}
\newcommand{\epsfigput}[3][]{%
\begin{figure}[t]\sis%
\begin{center}%
\includegraphics[height=2.75in,clip=true]{Figures/#2.eps}%
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
