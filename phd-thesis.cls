% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.9 2005-06-22 18:11:19 cananian Exp $
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
\LoadClass[12pt,titlepage,twoside]{book}
\usepackage{bibcontents} % required by MIT thesis specification
\usepackage[section,plain]{algorithm} % algorithm environment,\listofalgorithms

\linespread{1.2} % ``double-space''

% definitions
\newcommand{\defn}[1]           {{\textbf{\boldmath\defnlt{#1}}}}
\newcommand{\defnlt}[1]         {{\textit{#1}}}
\newcommand{\defni}[1]          {\defn{#1}\index{#1}}
\newcommand{\defnlti}[1]        {\defnlt{#1}\index{#1}}

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

% work around fig2dev wanting to add colors to .figures.
\providecommand{\color}[2][rgb]{}
