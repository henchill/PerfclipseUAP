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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
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
		        IJavaElement element = JavaCore.create((IFile)file);
		        if (element instanceof ICompilationUnit) {
		            ICompilationUnit icu = (ICompilationUnit)element;
		            CompilationUnit cu = parse(icu);
		            JavaPerforationVisitor visitor = new JavaPerforationVisitor();
		            cu.accept(visitor);
		            for (PerforatedLoop loop : visitor.getLoops()) {
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
	
	public static List<PerforatedLoop> getPerforatedLoops(IProject project) {
		if (perforations.containsKey(project)) {
			return perforations.get(project).loops;
		}
		return new ArrayList<PerforatedLoop>();
	}

	public void perforateLoop(ITextSelection sel, ITextEditor editor) {
		// TODO Auto-generated method stub
		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
        ICompilationUnit icu = (ICompilationUnit) typeRoot.getAdapter(ICompilationUnit.class);
        CompilationUnit cu = parse(icu);
        NodeFinder finder = new NodeFinder(cu, sel.getOffset(), sel.getLength());
        ASTNode node = finder.getCoveringNode();
        try {
			findLoops(shell, cu, icu, node);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 * 
	 * @param unit
	 * @return
	 */
	private CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	private void findLoops(Shell shell, CompilationUnit cu, ICompilationUnit icu, ASTNode node) throws JavaModelException {
		LoopVisitor visitor = new LoopVisitor();
		node.accept(visitor);

		boolean found = false;
		for (ForStatement stmt : visitor.getLoops()) {
			if (found/* && !MessageDialog.openConfirm(shell, "More than one loop found", "More than one loop was selected. Continue perforating the next loop?")*/) {
				MessageDialog.openError(shell, "Multiple loops found", "Only the first loop was perforated.");
				break;
			}
			PerforatedLoop loop = new PerforatedLoop(stmt, 2);
			String methodName = "extractedPerforation" + this.loops.size();
			ICompilationUnit new_icu = extractFor(stmt, cu, icu);
			CompilationUnit new_cu = parse(new_icu);
			MethodVisitor methodVisitor = new MethodVisitor();
			new_cu.accept(methodVisitor);
			for (MethodDeclaration md : methodVisitor.getMethods()) {
				if (md.getName().toString().equals(methodName)) {
					loop.setExtractedMethod(md);
					break;
				}
			}
			
			loops.add(loop);
			found = true;
		}
		if (!found) {
			MessageDialog.openError(shell, "No loops within selection", "The selected text must contain at least one for loop.");
		}
	}

	@SuppressWarnings("restriction")
	public ICompilationUnit extractFor(ForStatement stmt, CompilationUnit cu, ICompilationUnit icu) {
		ASTNode parentMethod = stmt.getParent();
		int start = stmt.getStartPosition();
		int length = stmt.getLength();
		int dlength = 0;		
	    try {
	        // creation of ASTRewrite
	        Document document= new Document(icu.getSource());
	        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());

	        // description of the change
	        List<ASTNode> updaters = (List<ASTNode>) stmt.getStructuralProperty(ForStatement.UPDATERS_PROPERTY);
	        for (ASTNode updater : updaters) {
	        	if (updater instanceof PostfixExpression) {
	        		PostfixExpression pfe = (PostfixExpression)updater;
	        		Assignment replacement = cu.getAST().newAssignment();
	        		replacement.setLeftHandSide(cu.getAST().newSimpleName(((SimpleName)pfe.getStructuralProperty(PostfixExpression.OPERAND_PROPERTY)).getIdentifier()));
	        		if (pfe.getOperator().equals(PostfixExpression.Operator.INCREMENT)) {
	        			replacement.setOperator(Assignment.Operator.PLUS_ASSIGN);
	        		}
	        		else {
	        			replacement.setOperator(Assignment.Operator.MINUS_ASSIGN);
	        		}
	        		replacement.setRightHandSide(cu.getAST().newNumberLiteral("2"));
	        		rewrite.replace(updater, replacement, null);
	        		dlength += updater.getLength() - replacement.getLength();
	        	}
	        	// @TODO
	        }
	        // computation of the text edits
	        TextEdit edits = rewrite.rewriteAST(document, icu.getJavaProject().getOptions(true));

	        // computation of the new source code
	        edits.apply(document);
	        String newSource = document.get();

	        // update of the compilation unit
	        icu.getBuffer().setContents(newSource);
	        //icu.becomeWorkingCopy(new NullProgressMonitor());

			ExtractMethodRefactoring emRefactor = new ExtractMethodRefactoring(icu, start, length + dlength);
			emRefactor.setMethodName("extractedPerforation" + this.loops.size());
			
			NullProgressMonitor pm = new NullProgressMonitor();
	        emRefactor.checkAllConditions(pm);
	        Change change = emRefactor.createChange(pm);
	        change.perform(pm);
	        return icu;
	    } catch (Exception e) {e.printStackTrace();}
	    return null;
	}

	private class JavaPerforationVisitor extends ASTVisitor {

		public List<PerforatedLoop> getLoops() {
			// TODO Auto-generated method stub
			return new ArrayList<PerforatedLoop>();
		}
		
	}
}
