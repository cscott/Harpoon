% -*-latex-*-
% $Id: csaacmconfbig.cls,v 1.1 2004-07-13 03:11:27 cananian Exp $
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{csaacmconfbig}[2003/12/01 csa acmconfbig class]
\usepackage{url}
\usepackage{latexsym}
\usepackage{bbm}
%\usepackage[small,bf]{caption}
\usepackage{graphics}
\usepackage{cite}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{acmconfbig}}

% Process the options.
\ProcessOptions

% Load llncs class.
\LoadClass{acmconfbig}

%% Misc

% Some hacks to make HTML happier.
\newcommand{\vskipargs}[1]{\vskip #1}
\newcommand{\newproc}{\hrule\vskipargs{1ex plus 0.1ex minus 0.1ex}}

% Tilde for webstuff
\newcommand{\webtilde}{\raisebox{-.8ex}\~}

% Comment out chunks.
\newcommand{\punt}[1]{}

% Footnote commands.
\newcommand{\footnotenonumber}[1]{{\def\thempfn{}\footnotetext{#1}}}
\newcommand{\footnotetight}[1]{\footnote{\renewcommand\baselinestretch{1}\footnotesize #1}}

%% Math Stuff, Symbols

% Number sets.
\newcommand{\reals}             {\mathbbm{R}}
\newcommand{\integers}          {\mathbbm{Z}}
\newcommand{\naturals}          {\mathbbm{N}}
\newcommand{\rationals}         {\mathbbm{Q}}
\newcommand{\complex}           {\mathbbm{C}}

% Useful shorthands.

\newcommand{\norm}[1]           {\left\| #1\right\|}
\newcommand{\set}[1]            {\left\{ #1 \right\}}
\newcommand{\abs}[1]            {\left| #1\right|}
\newcommand{\card}[1]           {\left| #1\right|}
\newcommand{\floor}[1]          {\left\lfloor #1 \right\rfloor}
\newcommand{\ceil}[1]           {\left\lceil #1 \right\rceil}
\newcommand{\ang}[1]            {\ifmmode{\left\langle #1 \right\rangle}
                                 \else{$\left\langle${#1}$\right\rangle$}\fi}
                                        % the \if allows use outside mathmode,
                                        % but will swallow following space there!
\newcommand{\sbrace}[1]{\left[#1\right]}
\newcommand{\cbrace}[1]{\left\{#1\right\}}
\newcommand{\paren}[1]          {\left( #1 \right)}
\newcommand{\prob}[1]           {\Pr\left\{ #1 \right\}}
\newcommand{\Var}               {\mathop{\rm Var}\nolimits}
\newcommand{\expect}[1]         {{\rm E}\left[ #1 \right]}
\newcommand{\expectsq}[1]       {{\rm E}^2\left[ #1 \right]}
\newcommand{\variance}[1]       {{\rm Var}\left[ #1 \right]}
\renewcommand{\choose}[2]       {{{#1}\atopwithdelims(){#2}}}

\def\pmod#1{\allowbreak\mkern12mu({\rm mod}\,\,#1)}

\newcommand{\matx}[2]           {\left(\!\!\!\begin{array}{*{#1}{c}}#2\end{array}\!\!\!\right)}
\newcommand{\Adj}               {\mathop{\rm Adj}\nolimits}
\newcommand{\percent}[1]        {#1\%}

%\newtheorem{principle}          {Principle}
%\newtheorem{theorem}            {Theorem}
%\newtheorem{lemma}[theorem]     {Lemma}
%\newtheorem{corollary}[theorem] {Corollary}
%\newtheorem{xample}             {Example}

%\newenvironment{example}        {\begin{xample}\rm}{\end{xample}}

%\newcommand{\proof}             {\noindent{\em Proof.}\hspace{1em}}

\def\squarebox#1{\hbox to #1{\hfill\vbox to #1{\vfill}}}

\newcommand{\qedbox}            {\vbox{\hrule\hbox{\vrule\squarebox{.667em}\vrule}\hrule}}
%\newcommand{\qed}               {\nopagebreak\mbox{}\hfill\qedbox\smallskip}
\newcommand{\eqnref}[1]         {(\protect\ref{#1})}

\newcommand{\twodots}           {\mathinner{\ldotp\ldotp}}
\newcommand{\transpose}         {^{\mbox{\scriptsize \sf T}}}
\newcommand{\amortized}[1]      {\widehat{#1}}

\renewcommand{\cases}[1]        {\left\{ \begin{array}{ll}#1\end{array}\right.}
\newcommand{\cif}[1]            {\mbox{if $#1$}}
\newcommand{\cwhen}[1]          {\mbox{when $#1$}}
\newcommand{\cotherw}           {\mbox{otherwise}}

%Narrow arrays
\newlength{\savearraycolsep}
\newcommand{\narrowarray}[1]{\setlength{\savearraycolsep}{\arraycolsep}\setlength{\arraycolsep}{#1\arraycolsep}}
\newcommand{\normalarray}{\setlength{\arraycolsep}{\savearraycolsep}}

% definitions and special styles

\newcommand{\defn}[1]           {{\textit{\textbf{\boldmath #1}}}}
\newcommand{\defi}[1]           {{\textit{\textbf{\boldmath #1\/}}}}
\newcommand{\const}[1]          {\ifmmode\mbox{\textsc{#1}}\else\textsc{#1}\fi}
\newcommand{\reduc}             {\leq_{\rm P}}
\newcommand{\lang}[1]           {\ifmmode\mathord{\mathcode`-="702D\rm#1\mathcode`\-="2200}\else{\rm#1}\fi}
\newcommand{\ckt}[1]            {\ifmmode \sc #1\else$\sc #1$\fi}
\newcommand{\id}[1]             {\ifmmode\mathit{#1}\else\textit{#1}\fi}
\newcommand{\proc}[1]           {\ifmmode\mbox{\textsc{#1}}\else\textsc{#1}\fi}
\newcommand{\procdecl}[1]       {\proc{#1}\vrule width0pt height0pt depth 7pt \relax}
\newcommand{\func}[1]           {\ifmmode\mathop{\mathcode`-="702D\rm #1\mathcode`\-="2200}\nolimits
                                 \else$\mathop{\mathcode`-="702D\rm #1\mathcode`\-="2200}\nolimits$\fi}

%% Margin notes - use \notesfalse to turn off notes.
\setlength{\marginparwidth}{0.6in}
\reversemarginpar
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

%% Section and figure placement

\newcommand{\secput}[2]{\section{#2}\label{sec:#1}}
\newcommand{\subsecput}[2]{\subsection{#2}\label{sec:#1}}

\newcommand{\figput}[2]{%
\begin{figure}[t]%
 \begin{center}%
  \input{Figures/#1.pstex_t}%
 \end{center}%
%\caption{\mbox{\tt (#1.eps)} #2}%
 \caption{#2}%
 \label{fig:#1}%
\end{figure}%
}

\newcommand{\figputstar}[2]{%
\begin{figure*}%
 \begin{center}%
  \input{Figures/#1.pstex_t}%
 \end{center}%
%\caption{\mbox{\tt (#1.eps)} #2}%
 \caption{#2}%
 \label{fig:#1}%
\end{figure*}%
}

\newcommand{\epsfigput}[2]{%
\begin{figure}[t]%
\begin{center}%
\includegraphics[height=2.75in,clip=true]{Figures/#1.eps}%
\end{center}%
\caption{#2}%
\label{fig:#1}%
\end{figure}%
}

%% References

\newcommand{\chapref}[1]        {Chapter~\ref{chap:#1}}
\newcommand{\secref}[1]         {Section~\ref{sec:#1}}
\newcommand{\secreftwo}[2]      {Sections \ref{sec:#1} and~\ref{sec:#2}}
\newcommand{\secrefthree}[3]    {Sections \ref{sec:#1}, \ref{sec:#2}, and \ref{sec:#3}}
\newcommand{\secreffour}[4]     {Sections \ref{sec:#1}, \ref{sec:#2}, \ref{sec:#3}, and~\ref{sec:#4}}
\newcommand{\appref}[1]         {Appendix~\ref{app:#1}}
\newcommand{\figref}[1]         {Figure~\ref{fig:#1}}
\newcommand{\figreftwo}[2]      {Figures \ref{fig:#1} and~\ref{fig:#2}}
\newcommand{\tabref}[1]         {Table~\ref{tab:#1}}
\newcommand{\stref}[1]          {Step~\ref{st:#1}}
\newcommand{\thmref}[1]         {Theorem~\ref{thm:#1}}
\newcommand{\lemref}[1]         {Lemma~\ref{lem:#1}}
\newcommand{\lemreftwo}[2]      {Lemmas \ref{lem:#1} and~\ref{lem:#2}}
\newcommand{\lemrefthree}[3]    {Lemmas \ref{lem:#1}, \ref{lem:#2} and~\ref{lem:#3}}
\newcommand{\corref}[1]         {Corollary~\ref{cor:#1}}
\newcommand{\eqref}[1]          {Equation~(\ref{eq:#1})}
\newcommand{\eqreftwo}[2]       {Equations (\ref{eq:#1}) and~(\ref{eq:#2})}
\newcommand{\ineqref}[1]        {Inequality~(\ref{ineq:#1})}
\newcommand{\ineqreftwo}[2]     {Inequalities (\ref{ineq:#1}) and~(\ref{ineq:#2})}
\newcommand{\invref}[1]         {Invariant~\ref{inv:#1}}
\newcommand{\defref}[1]         {Definition~\ref{def:#1}}
\newcommand{\propref}[1]        {Property~\ref{prop:#1}}
\newcommand{\caseref}[1]        {Case~\ref{case:#1}}
\newcommand{\casereftwo}[2]     {Cases \ref{case:#1} and~\ref{case:#2}}
\newcommand{\liref}[1]          {line~\ref{li:#1}}
\newcommand{\Liref}[1]          {Line~\ref{li:#1}}
\newcommand{\lirefs}[2]         {lines \ref{li:#1}--\ref{li:#2}}
\newcommand{\Lirefs}[2]         {Lines \ref{li:#1}--\ref{li:#2}}
\newcommand{\lireftwo}[2]       {lines \ref{li:#1} and~\ref{li:#2}}
\newcommand{\lirefthree}[3]     {lines \ref{li:#1}, \ref{li:#2}, and~\ref{li:#3}}
\newcommand{\exref}[1]          {Exercise~\ref{ex:#1}}
\newcommand{\princref}[1]       {Principle~\ref{prop:#1}}
