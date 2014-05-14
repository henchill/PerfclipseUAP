package wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ClassSelection extends WizardPage {

	private Text proj;
	private Text main;
	private Text eval;
	private Composite container;

	public ClassSelection() {
		super("Class Selection");
		setTitle("Class Selection");
		setDescription("Specify full qualified name of project, main class and evaluation class");		
	}
	
	@Override
	public void createControl(Composite parent) {
	    container = new Composite(parent, SWT.NONE);
	    GridLayout layout = new GridLayout();
	    container.setLayout(layout);
	    layout.numColumns = 2;
	    
	    Label projLabel = new Label(container, SWT.NONE);
	    projLabel.setText("Project Name");
	    proj = new Text(container, SWT.BORDER | SWT.SINGLE);
	    proj.setText("");
	    
	    Label mainLabel = new Label(container, SWT.NONE);
	    mainLabel.setText("Main Function");
	    main = new Text(container, SWT.BORDER | SWT.SINGLE);
	    main.setText("");
	    
	    Label evalLabel = new Label(container, SWT.NONE);
	    evalLabel.setText("Evaluation Class");
	    eval = new Text(container, SWT.BORDER | SWT.SINGLE);
	    eval.setText("");
	    
	    KeyListener isComplete = new KeyListener() {
	    	@Override
	    	public void keyPressed(KeyEvent e) {
	    		
	    	}
	    	
	    	@Override
	    	public void keyReleased(KeyEvent e) {
	    		if (!proj.getText().isEmpty() &&
	    				!main.getText().isEmpty() &&
	    				!eval.getText().isEmpty()) {
	  	          setPageComplete(true);
	  	        }
	    	}
	    };

	    proj.addKeyListener(isComplete);
	    main.addKeyListener(isComplete);
	    eval.addKeyListener(isComplete);
	    
	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	    proj.setLayoutData(gd);
	    main.setLayoutData(gd);
	    eval.setLayoutData(gd);
	    // required to avoid an error in the system
	    setControl(container);
	    setPageComplete(false);

	}

	public String getProjectText() {
	    return proj.getText();
	}

	public String getMainText() {
		return main.getText();
	}
	
	public String getEvalText() {
		return eval.getText();
	}
}
