package perfclipse.perforations;

import markers.MarkerFactory;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import perfclipse.main.Results;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class PerforatedLoop {
	private final static String PERFORATED_CLASS = "perfclipse.annotations.Perforated";
	private MethodDeclaration method;
	private ForStatement node;
	private int storedFactor; 
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
					NormalAnnotation na = (NormalAnnotation) modifier;
					Name annotationName = na.getTypeName();
					String name = annotationName.getFullyQualifiedName();
					
										
					if (name.equals("Perforated")) {
						List<MemberValuePair> tmpFactor = na.values();
						if (!tmpFactor.isEmpty()) {
							for (MemberValuePair pair : tmpFactor) {
								if (pair.getName().getFullyQualifiedName().equals("factor")) {									
									NumberLiteral num = (NumberLiteral) pair.getValue();																		
									break;
								}
							}
						} else {
							System.out.println("did not find factor");
							this.factor = 1;
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
        NormalAnnotation an = cu.getAST().newNormalAnnotation();
    	an.setTypeName(cu.getAST().newSimpleName("Perforated"));
    	
    	MemberValuePair mvp = cu.getAST().newMemberValuePair();
    	mvp.setName(cu.getAST().newSimpleName("factor"));
    	mvp.setValue(cu.getAST().newNumberLiteral("1"));
    	List<ASTNode> children = (List<ASTNode>) an.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
    	children.add(mvp);    	
        lr.insertFirst(an, null);

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
	
	public void addResults(double qos, double speedup, MethodDeclaration newMethod,
			CompilationUnit cu, ICompilationUnit icu) throws PerforationException {
		Document document;
        try {
			document = new Document(icu.getSource());
		} catch (JavaModelException e) {
			throw new PerforationException("Could not parse document to Java model.");
		}
        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
		ASTNode method = node.getParent().getParent();
		if (method instanceof MethodDeclaration) {
			List<ASTNode> modifiers = (List<ASTNode>)method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
			ListRewrite lr = rewrite.getListRewrite(newMethod, MethodDeclaration.MODIFIERS2_PROPERTY);
			boolean prePerforated = false;
			for (ASTNode modifier : modifiers) {
				if (modifier instanceof Annotation) {
					NormalAnnotation annotation = (NormalAnnotation) modifier;
					Name annotationName = annotation.getTypeName();
					String name = annotationName.getFullyQualifiedName();
					if (name.equals("Perforated")) {
						NormalAnnotation an = cu.getAST().newNormalAnnotation();
				    	an.setTypeName(cu.getAST().newSimpleName("Perforated"));
				    	List<ASTNode> old = (List<ASTNode>) annotation.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
				    	
				    	int curr_factor = 1;
				    	for (int i = 0; i < old.size(); i++) {
				    		MemberValuePair child = (MemberValuePair) old.get(i);
				    		if (child.getName().getFullyQualifiedName().equals("factor")) {
				    			curr_factor = Integer.parseInt(((NumberLiteral) child.getValue()).getToken());
				    		}
				    	}
				    	
				    	MemberValuePair mvp = cu.getAST().newMemberValuePair();
				    	mvp.setName(cu.getAST().newSimpleName("factor"));
				    	mvp.setValue(cu.getAST().newNumberLiteral(Integer.toString(curr_factor)));

						MemberValuePair mvp1 = cu.getAST().newMemberValuePair();
				    	mvp1.setName(cu.getAST().newSimpleName("qos"));
				    	mvp1.setValue(cu.getAST().newNumberLiteral(Double.toString(qos)));
				    	
				    	MemberValuePair mvp2 = cu.getAST().newMemberValuePair();
				    	mvp2.setName(cu.getAST().newSimpleName("speedup"));
				    	mvp2.setValue(cu.getAST().newNumberLiteral(Double.toString(speedup)));
				    	
				    	List<ASTNode> children = (List<ASTNode>) annotation.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
				    	children.add(mvp1);
				    	children.add(mvp2);
				        lr.replace(modifier, an, null);
				        
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
				}
			}
		}
	}
	
	public static MethodDeclaration findMethod(CompilationUnit cu, String methodName) {
		MethodFinder finder = new MethodFinder(methodName);
		cu.accept(finder);
		return finder.getMethod();
	}

	public void setFactor(boolean undoing) throws PerforationException {
		
		setFactor((double) this.factor, undoing);
	}
	
	public void setFactor(double factor, boolean undoing) throws PerforationException {		
		ICompilationUnit icu = this.getCompilationUnit();
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
    		if (undoing) {
	    		if (prevIncrement % prevFactor != 0) {
	    			throw new PerforationException("Illegal increment for perforation type.");
	    		}
    		}
    		int n = (int) (prevIncrement * factor);// / prevFactor;  

//    		n *= factor;    		
    		replacement.setRightHandSide(cu.getAST().newNumberLiteral(Integer.toString(Math.abs(n))));    		
    		if (n < 0) {
    			replacement.setOperator(Assignment.Operator.MINUS_ASSIGN);
    		}
    		else {
    			replacement.setOperator(Assignment.Operator.PLUS_ASSIGN);
    		}
    		rewrite.replace(updater, replacement, null);
        }
//        ListRewrite lr = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
//        ASTNode original = (ASTNode) lr.getOriginalList().get(0);
//        
//    	NormalAnnotation an = cu.getAST().newNormalAnnotation();
//    	an.setTypeName(cu.getAST().newSimpleName("Perforated"));
//    	
//    	MemberValuePair mvp = cu.getAST().newMemberValuePair();
//    	mvp.setName(cu.getAST().newSimpleName("factor"));
//    	mvp.setValue(cu.getAST().newNumberLiteral(Integer.toString(factor)));
//    	List<ASTNode> children = (List<ASTNode>) an.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
//    	children.add(mvp);
//    	lr.replace(original, an, null);
        
        
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
//        this.factor = factor;
        this.reparse(icu, method.getName().getFullyQualifiedName());
        
        CompilationUnit cu2 = (CompilationUnit) this.method.getRoot();
		IJavaElement javaElement = cu2.getJavaElement();
		
        ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
        IPath path = javaElement.getPath(); // unit: instance of CompilationUnit
        try {
        	bufferManager.connect(path, LocationKind.IFILE, null);
        	ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
        	textFileBuffer.commit(null, false );
        } catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
          	try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

	public CompilationUnit reparse(ICompilationUnit icu, String methodName) throws PerforationException {
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
	
	public void addMarker(String markerName, String annotation, Results result) throws PerforationException {
		String msg = "Perforation Results: QOS = %s; Speedup = %s";
		msg = String.format(msg, String.valueOf(result.QualityOfService), String.valueOf(result.Speedup));
		Position position = new Position(this.method.getStartPosition(),
										 this.method.getLength());
		try {
			CompilationUnit cu = (CompilationUnit) this.method.getRoot();
			IJavaElement element = cu.getJavaElement();
			if (element instanceof ICompilationUnit) {
				ICompilationUnit icu = (ICompilationUnit) element;				
				IMarker marker = MarkerFactory.createMarker(icu.getResource(), markerName, msg, position);
				
//				String newSource = MarkerFactory.addAnnotation(marker, annotation, position, cu);
//				icu.getBuffer().setContents(newSource);
//				this.reparse(icu, this.method.getName().getFullyQualifiedName());
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeMarkers(String markerName) {
	
		CompilationUnit cu = (CompilationUnit) this.method.getRoot();
		IJavaElement element = cu.getJavaElement();
		if (element instanceof ICompilationUnit) {
			ICompilationUnit icu = (ICompilationUnit) element;	
			MarkerFactory.deleteAllMarkers(icu.getResource());
		}
	
	}
	public ICompilationUnit getCompilationUnit() {
		CompilationUnit cu = (CompilationUnit) this.method.getRoot();
		
		IJavaElement element = cu.getJavaElement();
		if (element instanceof ICompilationUnit) {
			ICompilationUnit icu = (ICompilationUnit) element;
			return icu;
		}
		return null;
	}
	
	public String getName() {
		String className = method.getClass().getName();
		String name = method.getName().toString();
		return method.getName().getFullyQualifiedName();
	}
	
	public String getClassName() {
		CompilationUnit cu = (CompilationUnit) method.getRoot();
		return cu.getJavaElement().getElementName();
		
	}
	
	public void setStoredFactor(int factor) {
		this.storedFactor = factor;
	}
	
	public void resetFactor() throws PerforationException {
		ICompilationUnit icu = this.getCompilationUnit();
//		setFactor(this.storedFactor, icu);
	}
	
	public void unperforate() throws PerforationException {	
//		int curr_val = readFactor();
		setFactor( 1.0 / this.factor, true);
	}
	
	public MethodDeclaration getMethod() {
		return this.method;
	}
	
	public void perforate() throws PerforationException {
		this.factor = readFactor();		
		setFactor(this.factor, false);
	}


	public void removeAnnotation() throws PerforationException {
		CompilationUnit cu = (CompilationUnit) this.method.getRoot();
		IPath path = cu.getJavaElement().getPath();
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // (1)
      	ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
      	IDocument document = textFileBuffer.getDocument();
      	
		ICompilationUnit icu = this.getCompilationUnit();
		
		List<IMarker> markers = MarkerFactory.findAllMarkers(icu.getResource());
		System.out.println("markers found: " + Integer.toString(markers.size()));
	    for (IMarker marker : markers) {
			try {
				int start = (int) marker.getAttribute(IMarker.CHAR_START);
				int end = (int) marker.getAttribute(IMarker.CHAR_END);
				Position position = new Position(start, end - start);
				Position loopPosition = new Position(this.method.getStartPosition(),
						 this.method.getLength());
				if (!position.equals(loopPosition)) {
					System.out.println("positions are equal");
					IAnnotationModel iamf = textFileBuffer.getAnnotationModel();
					
					SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation("Perfclipse.greenAnnotation", marker);

					iamf.connect(document);
					iamf.addAnnotation(ma, position);
					iamf.disconnect(document);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
	    }
	    
		ASTRewrite rewrite = ASTRewrite.create(this.method.getAST());
        ListRewrite lr = rewrite.getListRewrite(this.method, MethodDeclaration.MODIFIERS2_PROPERTY);
        
		List<ASTNode> modifiers = (List<ASTNode>) this.method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
		for (ASTNode modifier : modifiers) {
			if (modifier instanceof Annotation) {
				NormalAnnotation na = (NormalAnnotation) modifier;
				Name annotationName = na.getTypeName();
				String name = annotationName.getFullyQualifiedName();
					
				if (name.equals("Perforated")) {
					lr.remove(na, null);
				}
			}
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
	    
	    System.out.println("remove annotation source updated");
		IJavaElement javaElement = cu.getJavaElement();
	    
        try {
        	bufferManager.connect(path, LocationKind.IFILE, null);
        	textFileBuffer.commit(null, false );
        } catch (CoreException e) {
			e.printStackTrace();
		} finally {
          	try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
        }
	}

	private int readFactor() {
		ICompilationUnit icu = this.getCompilationUnit();
		List<ASTNode> modifiers = (List<ASTNode>) this.method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);		
		for (ASTNode modifier : modifiers) {
			if (modifier instanceof Annotation) {
				NormalAnnotation na = (NormalAnnotation) modifier;
				Name annotationName = na.getTypeName();
				String name = annotationName.getFullyQualifiedName();
				
				if (name.equals("Perforated")) {
					List<MemberValuePair> tmpFactor = na.values();
					if (!tmpFactor.isEmpty()) {
						for (MemberValuePair pair : tmpFactor) {
							if (pair.getName().getFullyQualifiedName().equals("factor")) {
								NumberLiteral num = (NumberLiteral) pair.getValue();
								return Integer.parseInt(num.getToken());
							}
						}
					}
				}
			}
		}
		System.err.println("Could not read perforation factor");
		return 1;
	}
}
