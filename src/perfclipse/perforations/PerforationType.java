package perfclipse.perforations;

import org.eclipse.jdt.core.dom.ASTNode;

public interface PerforationType {
	public String getName();
	public String getArg();
	public void setArg(String arg);
	public void perforate(ASTNode node, String arg);
	public void revert(ASTNode node, String arg);
}
