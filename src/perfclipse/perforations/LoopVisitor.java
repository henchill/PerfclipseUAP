package perfclipse.perforations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class LoopVisitor extends ASTVisitor {
	List<ForStatement> statements = new ArrayList<ForStatement>();

	public boolean visit(ForStatement node) {
		statements.add(node);
		return super.visit(node);
	}

	public List<ForStatement> getLoops() {
		return statements;
	}
}