package perfclipse.perforations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor {
	List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
	
	public boolean visit(MethodDeclaration node) {
		methods.add(node);
		return super.visit(node);
	}
	
	public List<MethodDeclaration> getMethods() {
		return methods;
	}
}
