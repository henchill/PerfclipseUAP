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

import perfclipse.Results;

public class PerforatedLoop {
	private ASTNode node;
	private ASTNode extractedMethod;
	private int pt;
	private String name;
	private int current_pt;
	
	PerforatedLoop(ASTNode node, int pt) {
		this.node = node;
		this.pt = pt;
		this.current_pt = pt;
		createName();
	}
	
	private void createName() {
		ASTNode parentNode = node.getParent();
	    while (parentNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
	        parentNode = parentNode.getParent();
	    }

	    MethodDeclaration md = (MethodDeclaration) this.extractedMethod;
	    name = md.getClass().getName() + "-" + md.getName().toString();
	}
	
	public String getName() {
		return name;
	}

	public void setFactor() {
		this.current_pt = this.pt;
	}
	
	public void setFactor(int pt) {
		this.current_pt = pt; 
	}
	
	public void setExtractedMethod(ASTNode node) {
		this.extractedMethod = node;
	}
	
	public ASTNode getExtractedMethod() {
		return this.extractedMethod;
	}
	
	public void addMarker(String markerName, String annotation, Results result) {
		String msg = "Perforation Results: QOS = %s; Speedup = %s";
		msg = String.format(msg, String.valueOf(result.QualityOfService), String.valueOf(result.ElapsedTime));
		Position position = new Position(this.extractedMethod.getStartPosition(),
										 this.extractedMethod.getLength());
		System.out.println(this.node.getParent().getStartPosition() + "---" + node.getStartPosition());
		try {
			CompilationUnit cu = (CompilationUnit) this.node.getRoot();
			IJavaElement element = cu.getJavaElement();
			if (element instanceof ICompilationUnit) {
				ICompilationUnit icu = (ICompilationUnit) element;
				IMarker marker = MarkerFactory.createMarker(icu.getResource(), markerName, msg, position);
				MarkerFactory.addAnnotation(marker, annotation, position, cu);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
