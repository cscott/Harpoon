% -*-latex-*-
%
% Tex Library
% Robert Blumofe
% rdbacmconf.cls
%
% $Id: rdbacmconf.cls,v 1.1 2003-11-09 21:41:33 cananian Exp $
% $Log: rdbacmconf.cls,v $
% Revision 1.1  2003-11-09 21:41:33  cananian
% Imported hacked versions of acmconf.cls from the xaction paper.
%
% Revision 1.2  2003/11/07 06:18:20  cel
% Reorg.
%
% Revision 1.1  2003/04/25 20:00:57  cel
% Created.
%
% Revision 1.6  1998/03/20 04:02:49  randall
% First pass of updating.
%
% Revision 1.5  1997/11/05 02:43:28  cel
% Changed baselinestretch to 1.2.
%
% Revision 1.4  1997/11/03 23:01:48  cel
% Minor changes.
%
% Revision 1.3  1997/10/28 00:57:54  randall
% Changed spacing and font size to PLDI requirements.
%
% Revision 1.2  1997/10/24 16:01:47  athena
% Updates spawn_sync section.  Updated rdbacmconf.cls to newer latex2e.
% (I hope this doesn't break backward compatibility. It should not)
%
% Revision 1.1  1997/10/16 22:07:57  cel
% Changed to rdbacmconf format
%
% Revision 1.9  1996/04/05 01:24:28  rdb
% Nothing, really.
%
% Revision 1.8  1996/04/03 16:01:54  rdb
% Nothing, really.
%
% Revision 1.2  1996/03/23  00:42:20  rdb
% Tighten spacing around sections.
%
% Revision 1.1  1996/03/23 00:27:08  rdb
% Create ACM Conference format class.
%
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%

%%% The main features of this class are:
%%% 1) Two columns.
%%% 2) Text is 9pt on 10pt baselines.
%%% 3) The left and right margins are .75 inches and the column gutter is
%%%    .33 inches, so the columns are 3.33 inches wide.
%%% 4) First page has title information, and an extra 4.5pc of space at the
%%%    bottom of the first column for the ACM copyright notice. (You must
%%%    use one of the commands \copyrightspace or \toappear{} to obtain this
%%%    space.)
%%%
%%% This document class supports a [preprint] class option that allows you
%%% to run off a copy for a preprint -- with page numbers, "to appear"
%%% information, and so forth. This is documented below.
%%%
%%% There are a few restrictions you must observe:
%%% 1) Must use 9pt font size.
%%% 2) Must use two columns.
%%%
%%% Copyright Space:
%%% You leave space at the bottom of page 1/column 1 with the
%%% \copyrightspace command.  Alternatively, you can use the
%%% \toappear{...} command.  Normally, this turns into an unnumbered
%%% footnote 4.5pc high.  If [preprint] is on, then this space is
%%% filled with the {...} text; otherwise, it's blank. You must put
%%% one of these commands in the text of page 1/column 1 *after* all the
%%% other footnotes that go on page1/column 1, of course.
%%%
%%% A typical usage looks like this:
%%%     \toappear{To appear in the Ninth AES Conference on Midevil Lithuanian
%%%               Embalming Technique, June 1991, Alfaretta, Georgia.
%%%               Also available as Technical Report CMU-CS-91-119,
%%%               Cranberry Melon School of Cucumber Science.}
%%% This will be included in the preprint, and left out of the conference
%%% version.
%%%
%%% WARNING:
%%% Some dvi-ps converters heuristically allow chars to drift from their
%%% true positions a few pixels. This loses noticeably with the 9pt sans-serif
%%% bold font used for section headers. You turn this hackery off in our
%%% dvi-ps converters with the -e option:
%%%     dvips -e 0 foo.dvi > foo.ps

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{rdbacmconf}[1996/03/22 rdb ACM Conference class]

% Declare preprint option.
\newif\ifpreprint
\DeclareOption{preprint}{\preprinttrue}

% Pass on other options.
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}

% Process the options.
\ProcessOptions

\newif\if@openbib\@openbibfalse

% Load article class.
\LoadClass[twocolumn]{article}

% Use 9pt fonts.
\input{rdbsize9.clo}
\renewcommand\baselinestretch{1.2}

%
% Formatting.
%

% Page format.
\setlength\textwidth{7in}
\setlength\oddsidemargin{-0.25in}
\setlength\evensidemargin{-0.25in}
\setlength\columnsep{0.33in}
\setlength\columnseprule{0\p@}
\setlength\textheight{9.0in}
\setlength\topmargin\z@
\addtolength\topmargin{-\headheight}
\addtolength\topmargin{-\headsep}

% Ignore hboxes overfull by 1 point or less.
\hfuzz 1pt

% Formatting parameters for floats.
\setcounter{topnumber}{3}
\renewcommand\topfraction{.7}
\setcounter{bottomnumber}{1}
\renewcommand\bottomfraction{.3}
\setcounter{totalnumber}{5}
\renewcommand\textfraction{.1}
\renewcommand\floatpagefraction{.7}
\setcounter{dbltopnumber}{2}
\renewcommand\dbltopfraction{.7}
\renewcommand\dblfloatpagefraction{.7}

% Spacing for floats.
\addtolength{\floatsep}{0\p@}
\addtolength{\textfloatsep}{0\p@}
\addtolength{\intextsep}{0\p@}
\addtolength{\dblfloatsep}{0\p@}
\addtolength{\dbltextfloatsep}{0\p@}

% Caption spacing.
\setlength\abovecaptionskip{10\p@}
\setlength\belowcaptionskip{0\p@}

%
% Standard article stuff.
%

% Page style: headings.
\if@twoside
  \def\ps@headings{% use sans serif in header
      \let\@oddfoot\@empty\let\@evenfoot\@empty
      \def\@evenhead{\sffamily{\bfseries\thepage}\hfil\slshape\leftmark}%
      \def\@oddhead{\sffamily{\slshape\rightmark}\hfil\bfseries\thepage}%
      \let\@mkboth\markboth
    \def\sectionmark##1{%
      \markboth {{% do not uppercase
        \ifnum \c@secnumdepth >\z@
          \thesection\quad
        \fi
        ##1}}{}}%
    \def\subsectionmark##1{%
      \markright {%
        \ifnum \c@secnumdepth >\@ne
          \thesubsection\quad
          \fi
          ##1}}}
\else
  \def\ps@headings{% use sans serif in header
    \let\@oddfoot\@empty
    \def\@oddhead{\sffamily{\slshape\rightmark}\hfil\bfseries\thepage}%
    \let\@mkboth\markboth
    \def\sectionmark##1{%
      \markright {{% do not uppercase
        \ifnum \c@secnumdepth >\m@ne
          \thesection\quad
        \fi
        ##1}}}}
\fi

% Page style: myheadings.
\def\ps@myheadings{% use sans serif in header
    \let\@oddfoot\@empty\let\@evenfoot\@empty
    \def\@evenhead{\sffamily{\bfseries\thepage}\hfil\slshape\leftmark}%
    \def\@oddhead{\sffamily{\slshape\rightmark}\hfil\bfseries\thepage}%
    \let\@mkboth\@gobbletwo
    \let\sectionmark\@gobble
    \let\subsectionmark\@gobble
    }

% Page style: plain.
\def\ps@plain{% use sans serif in foot
    \let\@oddhead\@empty\let\@evenhead\@empty
    \def\@oddfoot{\sffamily\hfil\bfseries\thepage\hfil}%
    \def\@evenfoot{\sffamily\hfil\bfseries\thepage\hfil}%
    \let\@mkboth\@gobbletwo
    \let\chaptermark\@gobble
    \let\sectionmark\@gobble
    }

% Sectioning.
\renewcommand{\section}{%
  \@startsection{section}{1}{\z@}%
  {-3.5ex \@plus -1ex \@minus -.2ex}{1.8ex \@plus.2ex}%
  {\normalfont\large\sffamily\bfseries}}% use sans serif
\renewcommand{\subsection}{%
  \@startsection{subsection}{2}{\z@}%
  {-3.25ex\@plus -1ex \@minus -.2ex}{1.3ex \@plus .2ex}%
  {\normalfont\normalsize\sffamily\bfseries}}% use sans serif
\renewcommand{\subsubsection}{%
  \@startsection{subsubsection}{3}{\z@}%
  {-3.25ex\@plus -1ex \@minus -.2ex}{1.3ex \@plus .2ex}%
  {\normalfont\normalsize\sffamily\bfseries}}% use sans serif
\renewcommand{\paragraph}{%
  \@startsection{paragraph}{4}{\z@}%
  {2.0ex \@plus1ex \@minus.2ex}{-0.5em}%
  {\normalfont\normalsize\sffamily\bfseries}}% use sans serif
\renewcommand{\subparagraph}{%
  \@startsection{subparagraph}{5}{\parindent}%
  {2.0ex \@plus1ex \@minus .2ex}{-0.5em}%
  {\normalfont\normalsize\sffamily\bfseries}}% use sans serif

% Bibliography.
\renewenvironment{thebibliography}[1]
     {\section*{\refname
        \@mkboth{\refname}{\refname}}% do not uppercase
      \small% use small type
      \list{\@biblabel{\@arabic\c@enumiv}}%
           {\settowidth\labelwidth{\@biblabel{#1}}%
            \leftmargin\labelwidth
            \advance\leftmargin\labelsep
            \if@openbib
              \advance\leftmargin\bibindent
              \itemindent -\bibindent
              \listparindent \itemindent
              \parsep \z@
            \fi
            \itemsep \z@
            \usecounter{enumiv}%
            \let\p@enumiv\@empty
            \renewcommand\theenumiv{\@arabic\c@enumiv}}%
      \if@openbib
        \renewcommand\newblock{\par}%
      \else
        \renewcommand\newblock{\hskip .11em \@plus.33em \@minus.07em}%
      \fi
      \sloppy\clubpenalty4000\widowpenalty4000%
      \sfcode`\.=\@m}
     {\def\@noitemerr
       {\@latex@warning{Empty `thebibliography' environment}}%
      \endlist}

% Tightly spaced lists.
% \newenvironment{closeitemize}
%                {\list{$\bullet$}{\setlength{\itemsep}{-0.2\baselineskip}
%                                  \setlength{\topsep}{0.2\baselineskip}
%                                  \setlength{\parskip}{0pt}}}
%                {\endlist}
% \newenvironment{closedescription}
%                {\list{}{\labelwidth\z@ \itemindent-\leftmargin
%                         \let\makelabel\descriptionlabel
%                         \setlength{\itemsep}{-0.2\baselineskip}
%                         \setlength{\topsep}{0.2\baselineskip}
%                         \setlength{\parskip}{0pt}}}
%                {\endlist}

% Figures and tables.
\def\fnum@figure{{\small\sffamily\bfseries\figurename~\thefigure}\small}
\def\fnum@table{{\small\sffamily\bfseries\tablename~\thetable}\small}

% Theorems.
\def\@begintheorem#1#2{\trivlist 
   \item[\hskip \labelsep{\sffamily\bfseries #1\ #2}]\itshape}
\def\@opargbegintheorem#1#2#3{\trivlist
      \item[\hskip \labelsep{\sffamily\bfseries #1\ #2\ (#3)}]\itshape}

% For restating theorems.
\newenvironment{restatetheorem}[1]
  {\begin{trivlist}
    \item[\hskip \labelsep{\sffamily\bfseries Theorem~\ref{thm:#1}}]\itshape}
  {\end{trivlist}}

% Figure parts.
\newcommand{\figpart}[1]{{\small\sffamily\bfseries (#1)}~\ignorespaces}
\newcommand{\figparts}[2]{{\small\sffamily\bfseries (#1)--(#2)}~\ignorespaces}

% The theorem environments.
% \newtheorem{theorem}{Theorem}
% \newtheorem{lemma}[theorem]{Lemma}
% \newtheorem{corollary}[theorem]{Corollary}
% \newtheorem{definition}[theorem]{Definition}

%
% Stuff to leave space for ACM copyright notice.
%

% Unmarked footnote.
\newcommand{\unmarkedfootnote}[1]{
  {\long\def\@makefntext##1{##1}\footnotetext{#1}}}

% Copyrightspace command.
\newcommand{\copyrightspace}{
  \unmarkedfootnote{\vrule height 4.5pc width 0in depth 0in}}

% Toappear command.
\newcommand{\toappear}[1]{
  \ifpreprint
    \unmarkedfootnote{\vrule height 2.25pc depth  2.25pc width 0in
                      \parbox{2.8in}{\small #1}}
  \else
    \copyrightspace
  \fi}

%
% Set defaults.
%

% Set pagestyle.
\ifpreprint
  \pagestyle{plain}
\else
  \pagestyle{empty}
\fi

% Ragged bottom.
\raggedbottom

\endinput
