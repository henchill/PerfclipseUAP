package perfclipse.perforations;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class PerforatedLoop {
	private final static String PERFORATED_CLASS = "perfclipse.annotations.Perforated";
	private MethodDeclaration method;
	private ForStatement node;
	private int factor;
	public PerforatedLoop(ForStatement node) throws PerforationException {
		this.node = node;
		this.factor = 1;
		ASTNode method = node.getParent().getParent();
		if (method instanceof MethodDeclaration) {
			this.method = (MethodDeclaration)method;
			List<ASTNode> modifiers = (List<ASTNode>)method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
			boolean prePerforated = false;
			for (ASTNode modifier : modifiers) {
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					Name annotationName = annotation.getTypeName();
					String name = annotationName.getFullyQualifiedName();
					if (name.equals("Perforated")) {
						if (annotation instanceof MarkerAnnotation) {
							this.factor = 1;
						}
						else if (annotation instanceof SingleMemberAnnotation) {
							this.factor = (int)((SingleMemberAnnotation) annotation).getStructuralProperty(SingleMemberAnnotation.VALUE_PROPERTY);
						}
						else {
							throw new PerforationException("Unsupported @Perforated annotation type.");
						}
						prePerforated = true;
						break;
					}
				}
			}
			if (!prePerforated) {
				throw new PerforationException("Perforated loop missing @Perforated annotation.");
			}
		}
		else {
			throw new PerforationException("Loop not top-level within perforated method.");
		}
	}

	/**
	 * Perforate a new loop.
	 * @param node
	 * @param cu
	 * @param icu
	 * @return
	 * @throws PerforationException 
	 */
	public static PerforatedLoop perforate(ForStatement node, CompilationUnit cu, ICompilationUnit icu) throws PerforationException {
		ASTNode method = node.getParent().getParent();
		if (method instanceof MethodDeclaration) {
			List<ASTNode> modifiers = (List<ASTNode>)method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
			boolean prePerforated = false;
			for (ASTNode modifier : modifiers) {
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					Name annotationName = annotation.getTypeName();
					String name = annotationName.getFullyQualifiedName();
					if (name.equals("Perforated")) {
						throw new PerforationException("Loop already perforated.");
					}
				}
			}
		}
		int start = node.getStartPosition();
		int length = node.getLength();
		int dlength = 0;

	    try {
	        // creation of ASTRewrite
			ExtractMethodRefactoring emRefactor = new ExtractMethodRefactoring(icu, start, length + dlength);
			String methodNameBase = ((Name)method.getStructuralProperty(MethodDeclaration.NAME_PROPERTY)).getFullyQualifiedName() + "InnerLoop";
			String methodName = methodNameBase;
			emRefactor.setMethodName(methodName);
			NullProgressMonitor pm = new NullProgressMonitor();
			RefactoringStatus rs = emRefactor.checkAllConditions(pm);
			int i = 0;
			while (!rs.isOK() && i < 20) {
				methodName = methodNameBase + i;
				emRefactor.setMethodName(methodName);
				rs = emRefactor.checkAllConditions(pm);
				i++;
			}

			try {
		        Change change = emRefactor.createChange(pm);
		        change.perform(pm);
			} catch (NullPointerException e) {
				throw new PerforationException("Multiple output variables from this for loop.");
			}
	        cu = JavaPerforation.parse(icu);
	        MethodDeclaration newMethod = findMethod(cu, methodName);
	        addAnnotation(newMethod, cu, icu);
	        cu = JavaPerforation.parse(icu);
	        newMethod = findMethod(cu, methodName);
	        Block block = (Block)newMethod.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
	        int j = 0;
	        while (j < block.statements().size()) {
		        ASTNode topLevel = (ASTNode)block.statements().get(j);
		        if (topLevel instanceof ForStatement) {
		        	return new PerforatedLoop((ForStatement)topLevel);
		        }
		        j++;
	        }
        	throw new PerforationException("Cannot find extracted for statement.");
	    } catch (Exception e) { throw new PerforationException(e); }
	}

	private static void addAnnotation(MethodDeclaration newMethod, CompilationUnit cu, ICompilationUnit icu) throws PerforationException {
		Document document;
        try {
			document = new Document(icu.getSource());
		} catch (JavaModelException e) {
			throw new PerforationException("Could not parse document to Java model.");
		}
        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        ImportFinder importFinder = new ImportFinder();
        cu.accept(importFinder);
        if (!importFinder.found()) {
        	ListRewrite importLR = rewrite.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);
        	ImportDeclaration id = cu.getAST().newImportDeclaration();
        	id.setName(cu.getAST().newName(PERFORATED_CLASS));
        	importLR.insertLast(id, null);
        }
        ListRewrite lr = rewrite.getListRewrite(newMethod, MethodDeclaration.MODIFIERS2_PROPERTY);
        MarkerAnnotation ma = cu.getAST().newMarkerAnnotation();
        ma.setStructuralProperty(MarkerAnnotation.TYPE_NAME_PROPERTY, cu.getAST().newSimpleName("Perforated"));
        lr.insertFirst(ma, null);

	    // computation of the text edits
	    TextEdit edits = rewrite.rewriteAST(document, icu.getJavaProject().getOptions(true));
	
	    // computation of the new source code
	    try {
			edits.apply(document);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	    String newSource = document.get();
	
	    // update of the compilation unit
	    try {
			icu.getBuffer().setContents(newSource);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private static MethodDeclaration findMethod(CompilationUnit cu, String methodName) {
		MethodFinder finder = new MethodFinder(methodName);
		cu.accept(finder);
		return finder.getMethod();
	}

	public void setFactor(int factor, ICompilationUnit icu) throws PerforationException {
		ASTNode cu = node.getRoot();
		int prevFactor = this.factor;
		// creation of ASTRewrite
        Document document;
		try {
			document = new Document(icu.getSource());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
			throw new PerforationException("Could not parse document to Java model.");
		}
        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());

        List<ASTNode> updaters = (List<ASTNode>) node.getStructuralProperty(ForStatement.UPDATERS_PROPERTY);
        for (ASTNode updater : updaters) {
        	int prevIncrement;
        	String varName;
        	if (updater instanceof PostfixExpression) {
        		PostfixExpression pfe = (PostfixExpression)updater;
        		varName = ((SimpleName)pfe.getStructuralProperty(PostfixExpression.OPERAND_PROPERTY)).getIdentifier();
        		if (pfe.getOperator().equals(PostfixExpression.Operator.INCREMENT)) {
        			prevIncrement = 1;
        		}
        		else {
        			prevIncrement = -1;
        		}
        	}
        	else if (updater instanceof Assignment) {
        		Assignment assign = (Assignment)updater;
        		varName = ((SimpleName)assign.getStructuralProperty(Assignment.LEFT_HAND_SIDE_PROPERTY)).getIdentifier();
        		Expression rhs = assign.getRightHandSide();
        		int rhsVal = (int)rhs.resolveConstantExpressionValue();
        		if (assign.getOperator().equals(Assignment.Operator.PLUS_ASSIGN)) {
        			prevIncrement = rhsVal;
        		}
        		else if (assign.getOperator().equals(Assignment.Operator.MINUS_ASSIGN)) {
        			prevIncrement = -rhsVal;
        		}
        		else {
        			throw new PerforationException("Only simple-type expressions are currently supported.");
        		}
        	}
        	else {
        		throw new PerforationException("Only simple-type for loops are currently supported.");
        	}
        	Assignment replacement = cu.getAST().newAssignment();
    		replacement.setLeftHandSide(cu.getAST().newSimpleName(varName));
    		if (prevIncrement % prevFactor != 0) {
    			throw new PerforationException("Illegal increment for perforation type.");
    		}
    		int n = prevIncrement / prevFactor;
    		n *= factor;
    		replacement.setRightHandSide(cu.getAST().newNumberLiteral(Integer.toString(Math.abs(n))));
    		if (n < 0) {
    			replacement.setOperator(Assignment.Operator.MINUS_ASSIGN);
    		}
    		else {
    			replacement.setOperator(Assignment.Operator.PLUS_ASSIGN);
    		}
    		rewrite.replace(updater, replacement, null);
        }
        ListRewrite lr = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
        ASTNode original = (ASTNode) lr.getOriginalList().get(0);
        if (factor == 1) {
        	MarkerAnnotation ma = cu.getAST().newMarkerAnnotation();
        	ma.setTypeName(cu.getAST().newSimpleName("Perforated"));
        	lr.replace(original, ma, null);
        }
        else {
        	NormalAnnotation an = cu.getAST().newNormalAnnotation();
        	an.setTypeName(cu.getAST().newSimpleName("Perforated"));
        	MemberValuePair mvp = cu.getAST().newMemberValuePair();
        	mvp.setName(cu.getAST().newSimpleName("factor"));
        	mvp.setValue(cu.getAST().newNumberLiteral(Integer.toString(factor)));
        	List<ASTNode> children = (List<ASTNode>) an.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
        	children.add(mvp);
        	lr.replace(original, an, null);
        }
        // computation of the text edits
        TextEdit edits = rewrite.rewriteAST(document, icu.getJavaProject().getOptions(true));

        // computation of the new source code
        try {
			edits.apply(document);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
        String newSource = document.get();

        // update of the compilation unit
        try {
			icu.getBuffer().setContents(newSource);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

        this.factor = factor;
        this.reparse(icu, method.getName().getFullyQualifiedName());
	}

	private CompilationUnit reparse(ICompilationUnit icu, String methodName) throws PerforationException {
		CompilationUnit cu = JavaPerforation.parse(icu);
        this.method = findMethod(cu, methodName);
        Block block = (Block)this.method.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        int j = 0;
        while (j < block.statements().size()) {
	        ASTNode topLevel = (ASTNode)block.statements().get(j);
	        if (topLevel instanceof ForStatement) {
	        	this.node = (ForStatement)topLevel;
	        	return cu;
	        }
	        j++;
        }
    	throw new PerforationException("Cannot find extracted for statement."); 
	}

	private static class MethodFinder extends ASTVisitor {
		private String search;
		private MethodDeclaration node;
		public MethodFinder(String search) {
			this.search = search;
		}
		@Override
		public boolean visit(MethodDeclaration node) {
			if (((Name)node.getStructuralProperty(MethodDeclaration.NAME_PROPERTY)).getFullyQualifiedName().equals(search)) {
				this.node = node;
			}
			return super.visit(node);
		}
		public MethodDeclaration getMethod() {
			return node;
		}
	}

	private static class ImportFinder extends ASTVisitor {
		private boolean found = false;
		@Override
		public boolean visit(ImportDeclaration node) {
			if (node.getName().getFullyQualifiedName().equals(PERFORATED_CLASS)) {
				found = true;
			}
			return super.visit(node);
		}
		public boolean found() {
			return found;
		}
	}
}
