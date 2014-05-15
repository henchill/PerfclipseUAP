package perfclipse;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PerforationInfoDialog extends TitleAreaDialog {
	private Text project;
	private Text main;
	private Text eval;
	
	private String projectName;
	private String mainClass;
	private String evalClass;
	
	public PerforationInfoDialog(Shell parentShell) {
	    super(parentShell);
	}
	
	@Override
	public void create () {
		super.create();
		setTitle("Individual Perforation Analysis");
	    setMessage("Select what project you would like to run a perforation analysis on.", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(2, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    createProject(container);
	    createMain(container);
	    createEval(container);

	    return area;
	}
	
	private void createProject(Composite container){
		Label lblProject = new Label(container, SWT.NONE);
		lblProject.setText("Project Name");

	    GridData dataProject = new GridData();
	    dataProject.grabExcessHorizontalSpace = true;
	    dataProject.horizontalAlignment = GridData.FILL;

	    project = new Text(container, SWT.BORDER);
	    project.setLayoutData(dataProject);
	}
	
	private void createMain(Composite container) {
	    Label lblMain = new Label(container, SWT.NONE);
	    lblMain.setText("Main Class");
	    
	    GridData dataMain = new GridData();
	    dataMain.grabExcessHorizontalSpace = true;
	    dataMain.horizontalAlignment = GridData.FILL;
	    
	    main = new Text(container, SWT.BORDER);
	    main.setLayoutData(dataMain);
	}
	
	private void createEval(Composite container) {
		Label lblEval = new Label(container, SWT.NONE);
		lblEval.setText("Evaluation Class");
	    
	    GridData dataEval = new GridData();
	    dataEval.grabExcessHorizontalSpace = true;
	    dataEval.horizontalAlignment = GridData.FILL;
	    
	    eval = new Text(container, SWT.BORDER);
	    eval.setLayoutData(dataEval);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	private void saveInput() {
		projectName = project.getText();
		mainClass = main.getText();
		evalClass = eval.getText();
	}
	
	@Override
	protected void okPressed() {
	  saveInput();
	  super.okPressed();
	}

	public String getProjectName() {
		return projectName;
	}

	public String getMainClass() {
		return mainClass;
	}
	
	public String getEvalClass() {
		return evalClass;
	}
	
	  // overriding this methods allows you to set the
	  // title of the custom dialog
	  @Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Loop Perforation Config");
	  }
	
	  @Override
	  protected Point getInitialSize() {
	    return new Point(450, 300);
	  }
}
