package perfclipse.perforations;

import org.eclipse.jdt.core.dom.ASTNode;

public class PerforatedLoop {
	private ASTNode node;
	private int pt;
	PerforatedLoop(ASTNode node, int pt) {
		this.node = node;
		this.pt = pt;
	}
}
