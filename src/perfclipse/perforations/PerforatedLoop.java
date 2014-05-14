package perfclipse.perforations;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class PerforatedLoop {
	private ASTNode node;
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

	    MethodDeclaration md = (MethodDeclaration) parentNode;
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
}
