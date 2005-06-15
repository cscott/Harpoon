% -*- latex -*- This is a LaTeX document.
% $Id: phd-thesis.cls,v 1.4 2005-06-15 23:36:21 cananian Exp $
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{phd-thesis}[2005/06/07 csa PhD thesis class]
\usepackage{beton}\usepackage{euler} % fonts

\usepackage{amstext} % the \text command for math mode (replaces \mbox)
\usepackage{varioref}
\usepackage{xspace}
\usepackage{epsfig}
\usepackage{url}

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
% A breaking non-space for URLs
\newcommand{\bp}{\hspace{0pt}}
% single-spacing for code examples.
\newcommand{\sis}{\linespread{1.0}\setlength{\baselineskip}{0.8\baselineskip}}
\newcommand{\punt}[1]{}% ignore punted stuff

\newcommand{\appref}[1]         {Appendix~\ref{sec:#1}}
\newcommand{\figref}[1]         {Figure~\ref{fig:#1}}
\newcommand{\secref}[1]         {Section~\ref{sec:#1}}
\newcommand{\charef}[1]         {Chapter~\ref{cha:#1}}

\newcommand{\figput}[2]{%
\begin{figure}[t]\sis%
 \begin{center}%
  \input{Figures/#1.pstex_t}%
 \end{center}%
%\caption{\mbox{\tt (#1.eps)} #2}%
 \caption{#2}%
 \label{fig:#1}%
\end{figure}%
}
\newcommand{\epsfigput}[2]{%
\begin{figure}[t]\sis%
\begin{center}%
\includegraphics[height=2.75in,clip=true]{Figures/#1.eps}%
\end{center}%
\caption{#2}%
\label{fig:#1}%
\end{figure}%
}
