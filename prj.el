;; emacs hacking! (thanks to darko for the ideas & initial implementation)
;;;; extract the package name from the buffer-file-name
(defun harpoon-subdir-name (basename-fragment)
  "Returns the subdirectory of the current buffer, after stripping everything up to and including the basename-fragment."
  (let ((directory (file-name-directory buffer-file-name)))
    (string-match (concat "\\(.*" basename-fragment "\\)*\\(.*\\)/$")
		  directory)
    (substring directory (match-end 1) (match-end 2))))
;;;; extract a dot-delimited package name from the path to the current buffer
(defun harpoon-package-name (basename-fragment)
  "Returns the package name of the current buffer, after stripping the directory up to and including the basename-fragment"
  (let ((pkgname (harpoon-subdir-name basename-fragment)))
    (while (string-match "\\(.*\\)/\\(.*\\)" pkgname)
      (setq pkgname (concat 
		     (substring pkgname (match-beginning 1) (match-end 1)) "."
		     (substring pkgname (match-beginning 2) (match-end 2)))))
    pkgname))
;;;; extract the work directory name from the buffer-file-name
(defun harpoon-basepath-name (basename-fragment)
  "Returns the subdirectory of the current buffer, after stripping everything after (but not including) the basename-fragment."
  (let ((directory (file-name-directory buffer-file-name)))
    (string-match (concat "\\(.*" basename-fragment "\\)*\\(.*\\)$")
		  directory)
    (substring directory (match-beginning 1) (match-end 1))))
;;;; extract the root project dir
(defun user-specific-project-dir ()
  "Returns the pathname to the root project directory."
  (harpoon-basepath-name "/Code/"))
;;;; changed class-buffer template so that extra space is not generated
(defun harpoon-gen-get-super-class ()
  "Concatenates a space to the result of jde-gen-get-super-class if it is not empty."
  (let ((super-class (jde-gen-get-super-class)))
    (if (not (eq super-class ()))
      (concat " " super-class))))
;;;; template for generating visitor classes
(defun harpoon-list-of-quads ()
  "Returns list of all quadruple type files defined in IR/Quads."
  (directory-files (concat (user-specific-project-dir) "IR/Quads/") nil "^[A-Z]+.java"))
(defun harpoon-list-of-visit-quads (file-names)
  "Returns string which lists empty methods for visiting quad types in file-names."
  (let ((r '(l))
	(n (length file-names))
	(i 0))
    (while (< i n)
      (setq r (append r (list 'n>) (list (concat "public void visit (" 
						 (substring (nth i file-names) 0 -5) " q) { }"))))
      (setq i (1+ i)))
    r))
(tempo-define-template 
 "harpoon-visitor-class" 
 '('& '> "class " (P "Visitor class name: " class) " extends QuadVisitor {" 
     'n> (s class) "() { }"
     (harpoon-list-of-visit-quads (harpoon-list-of-quads))
     'n "}" '> 'n>)
 nil 
 "Insert a class which extends QuadVisitor class.")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; jde properties & settings
(jde-set-project-name "harpoon")
(jde-set-variables 
 '(jde-run-option-properties nil)
 '(jde-run-option-stack-size (quote ((128 . "kilobytes") (400 . "kilobytes"))))
 '(jde-gen-buffer-templates (quote (("Class" . jde-gen-class) ("Console" . jde-gen-console) ("Swing App" . jde-gen-jfc-app))))
 '(jde-compile-option-command-line-args "")
 '(jde-gen-action-listener-template (quote ("'& (P \"Component name: \")" "\".addActionListener(new ActionListener() {\" 'n>" "\"public void actionPerformed(ActionEvent e) {\" 'n>" "\"}});\" 'n>")))
 '(jde-compile-option-depend t t)
 '(jde-compile-option-optimize nil)
 '(jde-run-option-verify (quote (nil t)))
 '(jde-gen-inner-class-template (quote ("'& \"class \" (P \"Class name: \" class)" "(P \"Superclass: \" super t)" "(let ((parent (jde-gen-lookup-named 'super)))" "(if (not (string= parent \"\"))" "(concat \" extends \" parent))) \" {\" 'n>" "\"public \" (s class) \"() {\" 'n> \"}\" 'n> \"}\" 'n>")))
 '(jde-run-read-vm-args nil)
 '(jde-entering-java-buffer-hooks (quote (jde-reload-project-file)))
 '(jde-run-applet-viewer "appletviewer")
 '(jde-compile-option-debug t t)
 '(jde-project-file-name "prj.el")
 '(jde-run-option-verbose (quote (nil nil nil)))
 '(jde-run-application-class "")
 '(jde-db-option-vm-args nil)
 '(jde-run-option-heap-size (quote ((1 . "megabytes") (16 . "megabytes"))))
 '(jde-db-read-vm-args nil)
 '(jde-db-option-heap-profile (quote (nil "./java.hprof" 5 20 "Allocation objects")))
 '(jde-db-mode-hook nil)
 '(jde-run-option-garbage-collection (quote (t t)))
 '(jde-compile-option-vm-args nil)
 '(jde-run-applet-doc "index.html")
 '(jde-db-option-java-profile (quote (nil . "./java.prof")))
 '(jde-gen-get-set-var-template (quote ("'n>" "(P \"Variable type: \" type) \" \"" "(P \"Variable name: \" name) \";\" 'n> 'n>" "\"/**\" 'n>" "\"* Get the value of \" (s name) \".\" 'n>" "\"* @return Value of \" (s name) \".\" 'n>" "\"*/\" 'n>" "\"public \" (s type) \" get\" (jde-gen-init-cap (jde-gen-lookup-named 'name))" "\"() {return \" (s name) \";}\" 'n> 'n>" "\"/**\" 'n>" "\"* Set the value of \" (s name) \".\" 'n>" "\"* @param v  Value to assign to \" (s name) \".\" 'n>" "\"*/\" 'n>" "\"public void set\" (jde-gen-init-cap (jde-gen-lookup-named 'name))" "\"(\" (s type) \"  v) {this.\" (s name) \" = v;}\" 'n>")))
 '(jde-db-option-verify (quote (nil t)))
 '(jde-run-mode-hook nil)
 '(jde-db-option-classpath nil)
 '(jde-compile-option-deprecation nil)
 '(jde-db-startup-commands nil)
 '(jde-gen-boilerplate-function (quote jde-gen-create-buffer-boilerplate))
 '(jde-compile-option-nodebug nil)
 '(jde-compile-option-classpath nil t)
 '(jde-build-use-make t)
 '(jde-quote-classpath nil t)
 '(jde-gen-to-string-method-template (quote ("'&" "\"public String toString() {\" 'n>" "\"return super.toString();\" 'n>" "\"}\" 'n>")))
 '(jde-run-read-app-args nil)
 '(jde-db-source-directories (list (concat (user-specific-project-dir) "jdb/")))
 '(jde-db-option-properties nil)
 '(jde-db-option-stack-size (quote ((128 . "kilobytes") (400 . "kilobytes"))))
 '(jde-db-set-initial-breakpoint t)
 '(jde-run-option-application-args nil)
 '(jde-gen-mouse-listener-template (quote ("'& (P \"Component name: \")" "\".addMouseListener(new MouseAdapter() {\" 'n>" "\"public void mouseClicked(MouseEvent e) {}\" 'n>" "\"public void mouseEntered(MouseEvent e) {}\" 'n>" "\"public void mouseExited(MouseEvent e) {}\" 'n>" "\"public void mousePressed(MouseEvent e) {}\" 'n>" "\"public void mouseReleased(MouseEvent e) {}});\" 'n>")))
 '(jde-gen-console-buffer-template (quote ("(funcall jde-gen-boilerplate-function) 'n" "\"/**\" 'n" "\" * \"" "(file-name-nondirectory buffer-file-name) 'n" "\" *\" 'n" "\" *\" 'n" "\" * Created: \" (current-time-string) 'n" "\" *\" 'n" "\" * @author \" (user-full-name) 'n" "\" * @version\" 'n" "\" */\" 'n>" "'n>" "\"public class \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\" {\" 'n> 'n>" "\"public \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\"() {\" 'n>" "'n>" "\"}\" 'n>" "'n>" "\"public static void main(String[] args) {\" 'n>" "'p 'n>" "\"}\" 'n> 'n>" "\"} // \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "'n>")))
 '(jde-compile-option-directory (user-specific-project-dir) t)
 '(jde-run-option-vm-args nil)
 '(jde-make-program "make")
 '(jde-use-font-lock t)
 '(jde-db-option-garbage-collection (quote (t t)))
 '(jde-gen-class-buffer-template (quote 
    ("\"// \" (file-name-nondirectory buffer-file-name)"
     "\", created \" (current-time-string)" "\" by \" (user-login-name) 'n"
     "(funcall jde-gen-boilerplate-function)"
     "\"// Copyright (C) 2000 \" (user-full-name) \" <\" user-mail-address \">\" 'n" 
     "\"// Licensed under the terms of the GNU GPL; see COPYING for details.\" 'n" 
     "\"package harpoon\" (harpoon-package-name \"/Code\") \";\" 'n 'n" 
     "\"/**\" 'n" 
     "\" * <code>\"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\"</code>\" 'n" 
     "\" * \" 'n" 
     "\" * @author  \" (user-full-name)" "\" <\" user-mail-address \">\" 'n" 
     "\" * @version $I\" \"d$\" 'n" 
     "\" */\" 'n>" 
     "\"public class \"" 
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
     "(harpoon-gen-get-super-class) \" {\" 'n> 'n>"
     "\"/** Creates a <code>\" " 
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
     "\"</code>. */\" 'n" 
     "\"public \"" 
     "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
     "\"() {\" 'n>" 
     "\"    \" 'p 'n>" 
     "\"}\" 'n>" 
     "'n>" "\"}\"")) t)
 '(jde-compiler "javac")
 '(jde-jdk-doc-url "http://www.javasoft.com/products/jdk/1.1/docs/api/index.html")
 '(jde-db-debugger (quote ("jdb" . "Executable")))
 '(jde-compile-option-optimize-interclass nil)
 '(jde-run-option-classpath nil)
 '(jde-gen-mouse-motion-listener-template (quote ("'& (P \"Component name: \")" "\".addMouseMotionListener(new MouseMotionAdapter() {\" 'n>" "\"public void mouseDragged(MouseEvent e) {}\" 'n>" "\"public void mouseMoved(MouseEvent e) {}});\" 'n>")))
 '(jde-db-marker-regexp "^Breakpoint hit: .*(\\([^$]*\\).*:\\([0-9]*\\))")
 '(jde-gen-window-listener-template (quote ("'& (P \"Window name: \")" "\".addWindowListener(new WindowAdapter() {\" 'n>" "\"public void windowActivated(WindowEvent e) {}\" 'n>" "\"public void windowClosed(WindowEvent e) {}\" 'n>" "\"public void windowClosing(WindowEvent e) {System.exit(0);}\" 'n>" "\"public void windowDeactivated(WindowEvent e) {}\" 'n>" "\"public void windowDeiconified(WindowEvent e) {}\" 'n>" "\"public void windowIconified(WindowEvent e) {}\" 'n>" "\"public void windowOpened(WindowEvent e) {}});\" 'n>")))
 '(jde-global-classpath (list (user-specific-project-dir) "/usr/local/jdk/lib/classes.zip") t)
 '(jde-enable-abbrev-mode nil)
 '(jde-run-option-heap-profile (quote (nil "./java.hprof" 5 20 "Allocation objects")))
 '(jde-db-read-app-args nil)
 '(jde-db-option-verbose (quote (nil nil nil)))
 '(jde-run-java-vm "java")
 '(jde-read-compile-args nil)
 '(jde-run-option-java-profile (quote (nil . "./java.prof")))
 '(jde-compile-option-encoding nil)
 '(jde-run-java-vm-w "javaw")
 '(jde-compile-option-nowarn nil)
 '(jde-gen-jfc-app-buffer-template (quote ("(funcall jde-gen-boilerplate-function) 'n" "\"import java.awt.*;\" 'n" "\"import java.awt.event.*;\" 'n" "\"import com.sun.java.swing.*;\" 'n 'n" "\"/**\" 'n" "\" * \"" "(file-name-nondirectory buffer-file-name) 'n" "\" *\" 'n" "\" *\" 'n" "\" * Created: \" (current-time-string) 'n" "\" *\" 'n" "\" * @author \" (user-full-name) 'n" "\" * @version\" 'n" "\" */\" 'n>" "'n>" "\"public class \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\" extends JFrame {\" 'n> 'n>" "\"public \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\"() {\" 'n>" "\"super(\\\"\" (P \"Enter app title: \") \"\\\");\" 'n>" "\"setSize(600, 400);\" 'n>" "\"addWindowListener(new WindowAdapter() {\" 'n>" "\"public void windowClosing(WindowEvent e) {System.exit(0);}\" 'n>" "\"public void windowOpened(WindowEvent e) {}});\" 'n>" "\"}\" 'n>" "'n>" "\"public static void main(String[] args) {\" 'n>" "'n>" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\" f = new \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "\"();\" 'n>" "\"f.show();\" 'n>" "'p 'n>" "\"}\" 'n> 'n>" "\"} // \"" "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" "'n>")))
 '(jde-db-option-application-args nil)
 '(jde-gen-buffer-boilerplate nil t)
 '(jde-db-option-heap-size (quote ((1 . "megabytes") (16 . "megabytes"))))
 '(jde-compile-option-verbose nil)
 '(jde-mode-abbreviations (quote (("ab" . "abstract") ("bo" . "boolean") ("br" . "break") ("by" . "byte") ("byv" . "byvalue") ("cas" . "cast") ("ca" . "catch") ("ch" . "char") ("cl" . "class") ("co" . "const") ("con" . "continue") ("de" . "default") ("dou" . "double") ("el" . "else") ("ex" . "extends") ("fa" . "false") ("fi" . "final") ("fin" . "finally") ("fl" . "float") ("fo" . "for") ("fu" . "future") ("ge" . "generic") ("go" . "goto") ("impl" . "implements") ("impo" . "import") ("ins" . "instanceof") ("in" . "int") ("inte" . "interface") ("lo" . "long") ("na" . "native") ("ne" . "new") ("nu" . "null") ("pa" . "package") ("pri" . "private") ("pro" . "protected") ("pu" . "public") ("re" . "return") ("sh" . "short") ("st" . "static") ("su" . "super") ("sw" . "switch") ("sy" . "synchronized") ("th" . "this") ("thr" . "throw") ("throw" . "throws") ("tra" . "transient") ("tr" . "true") ("vo" . "void") ("vol" . "volatile") ("wh" . "while"))))
 '(jde-make-args (concat "-C " (user-specific-project-dir) " java") t)
 '(jde-gen-code-templates (quote (("Get Set Pair" . jde-gen-get-set) ("toString method" . jde-gen-to-string-method) ("Action Listener" . jde-gen-action-listener) ("Window Listener" . jde-gen-window-listener) ("Mouse Listener" . jde-gen-mouse-listener) ("Mouse Motion Listener" . jde-gen-mouse-motion-listener) ("Inner Class" . jde-gen-inner-class)  ("Visitor Class" . tempo-template-harpoon-visitor-class)))))
