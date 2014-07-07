package perfclipse.perforations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;


public class JavaPerforation {
	private static Map<IProject, JavaPerforation> perforations = new HashMap<IProject, JavaPerforation>();

	private IProject project;
	private List<PerforatedLoop> loops;
	private Shell shell;

	private JavaPerforation(IProject project, Shell shell) throws CoreException {
		this.project = project;
		this.shell = shell;
		this.loops = new ArrayList<PerforatedLoop>();
		IResource[] resources = project.members();
		this.scanPerforations(resources);
	}

	private void scanPerforations(IResource[] resources) throws CoreException {
		for (IResource file : resources) {
			if (file instanceof IFile && file.isAccessible()) {
//				System.out.println("first if");
		        IJavaElement element = JavaCore.create((IFile)file);
		        if (element instanceof ICompilationUnit) {
//		        	System.out.println("second if");
		            ICompilationUnit icu = (ICompilationUnit)element;
		            CompilationUnit cu = parse(icu);
		            JavaPerforationVisitor visitor = new JavaPerforationVisitor();
		            cu.accept(visitor);
		            for (PerforatedLoop loop : visitor.getLoops()) {
		            	System.out.println("Found loop" + loop.getName());
		            	loops.add(loop);
		            }
		        }
			}
			else if (file instanceof IFolder && file.isAccessible()) {
				scanPerforations(((IFolder)file).members());
			}
		}
	}

	public static JavaPerforation getPerforation(IProject project, Shell shell) throws CoreException {
		if (!perforations.containsKey(project)) {
			perforations.put(project, new JavaPerforation(project, shell));
		}
		return perforations.get(project);
	}
	
	public static void removePerforation(IProject project) {
		if (perforations.containsKey(project)) {
			perforations.remove(project);
		}
	}
	
	public static List<PerforatedLoop> getPerforatedLoops(IProject project) {
		if (perforations.containsKey(project)) {
			return perforations.get(project).loops;
		}
		return new ArrayList<PerforatedLoop>();
	}

	public PerforatedLoop perforateLoop(ITextSelection sel, ITextEditor editor) {
		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
        ICompilationUnit icu = (ICompilationUnit) typeRoot.getAdapter(ICompilationUnit.class);
        CompilationUnit cu = parse(icu);
        NodeFinder finder = new NodeFinder(cu, sel.getOffset(), sel.getLength());
        ASTNode node = finder.getCoveringNode();
        try {
        	LoopVisitor visitor = new LoopVisitor();
    		node.accept(visitor);
    		boolean found = false;
    		for (ForStatement stmt : visitor.getLoops()) {
    			if (found/* && !MessageDialog.openConfirm(shell, "More than one loop found", "More than one loop was selected. Continue perforating the next loop?")*/) {
    				MessageDialog.openError(shell, "Multiple loops found", "Only the first loop was perforated.");
    				break;
    			}
    			return new PerforatedLoop(stmt);
    		}
		} catch (PerforationException e) {
			MessageDialog.openError(shell, "Perforation failed", e.getMessage());
		}
        return null;
	}
	
	public PerforatedLoop markLoopForPerforation (ITextSelection sel, ITextEditor editor) {
		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
        ICompilationUnit icu = (ICompilationUnit) typeRoot.getAdapter(ICompilationUnit.class);
        CompilationUnit cu = parse(icu);
        NodeFinder finder = new NodeFinder(cu, sel.getOffset(), sel.getLength());
        ASTNode node = finder.getCoveringNode();
        try {
			return findLoop(shell, cu, icu, node);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (PerforationException e) {
			MessageDialog.openError(shell, "Perforation failed", e.getMessage());
		}
        return null;
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 * 
	 * @param unit
	 * @return
	 */
	public static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	private PerforatedLoop findLoop(Shell shell, CompilationUnit cu, ICompilationUnit icu, ASTNode node) throws JavaModelException, PerforationException {
		LoopVisitor visitor = new LoopVisitor();
		node.accept(visitor);

		boolean found = false;
		for (ForStatement stmt : visitor.getLoops()) {
			if (found/* && !MessageDialog.openConfirm(shell, "More than one loop found", "More than one loop was selected. Continue perforating the next loop?")*/) {
				MessageDialog.openError(shell, "Multiple loops found", "Only the first loop was perforated.");
				break;
			}
			return extractFor(stmt, cu, icu);
		}
		MessageDialog.openError(shell, "No loops within selection", "The selected text must contain at least one for loop.");
		return null;
	}

	@SuppressWarnings("restriction")
	public PerforatedLoop extractFor(ForStatement stmt, CompilationUnit cu, ICompilationUnit icu) throws PerforationException {
		PerforatedLoop loop = PerforatedLoop.perforate(stmt, cu, icu);
		loops.add(loop);
		return loop;
	}

	private class JavaPerforationVisitor extends ASTVisitor {
		List<PerforatedLoop> loops = new ArrayList<PerforatedLoop>();

		public List<PerforatedLoop> getLoops() {
			return loops;
		}

		// TODO: This works but is suboptimal - should just check methods with the annotation.
		public boolean visit(ForStatement node) {
			System.out.println("for loop execution");
			try {
				PerforatedLoop loop = new PerforatedLoop(node);
				System.out.println("Got Here: " + loop.getName());
				loops.add(loop);
			} catch (PerforationException e) {
				// Not a perforated loop, move along.
				
				System.err.println(e.getMessage());
			}
			return super.visit(node);
		}
	}
}
