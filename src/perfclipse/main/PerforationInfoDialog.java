package perfclipse.main;

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
	private Text mainP;
	private Text eval;
	private Text mainE;
	private Text evalOut;
	
	private String projectName;
	private String pMain;
	private String evalClass;
	private String eMain;
	private String eOut;
	
	
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
	    createPMain(container);
	    createEval(container);
	    createEMain(container);
	    createEOut(container);

	    return area;
	}
	
	private void createProject(Composite container){
		Label lblProject = new Label(container, SWT.NONE);
		lblProject.setText("Project Name");

	    GridData dataProject = new GridData();
	    dataProject.grabExcessHorizontalSpace = true;
	    dataProject.horizontalAlignment = GridData.FILL;

	    project = new Text(container, SWT.BORDER);
	    project.setText("PerfclipseTest");
	    project.setLayoutData(dataProject);
	}
	
	private void createPMain(Composite container) {
	    Label lblMain = new Label(container, SWT.NONE);
	    lblMain.setText("Project Main");
	    
	    GridData dataMain = new GridData();
	    dataMain.grabExcessHorizontalSpace = true;
	    dataMain.horizontalAlignment = GridData.FILL;
	    
	    mainP = new Text(container, SWT.BORDER);
	    mainP.setText("Main");
	    mainP.setLayoutData(dataMain);
	}
	
	private void createEval(Composite container) {
		Label lblEval = new Label(container, SWT.NONE);
		lblEval.setText("Evaluation Project");
	    
	    GridData dataEval = new GridData();
	    dataEval.grabExcessHorizontalSpace = true;
	    dataEval.horizontalAlignment = GridData.FILL;
	    
	    eval = new Text(container, SWT.BORDER);
	    eval.setText("EvalTest");
	    eval.setLayoutData(dataEval);
	}
	
	private void createEMain(Composite container) {
	    Label lblMain = new Label(container, SWT.NONE);
	    lblMain.setText("Evaluation Main");
	    
	    GridData dataMain = new GridData();
	    dataMain.grabExcessHorizontalSpace = true;
	    dataMain.horizontalAlignment = GridData.FILL;
	    
	    mainE = new Text(container, SWT.BORDER);
	    mainE.setText("Main");
	    mainE.setLayoutData(dataMain);
	}
	
	private void createEOut(Composite container) {
		Label lblOut = new Label(container, SWT.NONE);
		lblOut.setText("Output File");
		
		GridData dataOut = new GridData();
		dataOut.grabExcessHorizontalSpace = true;
		dataOut.horizontalAlignment = GridData.FILL;
		
		evalOut = new Text(container, SWT.BORDER);
		evalOut.setText("C:/Users/Happy/Documents/Workspace/runtime-EclipseApplication/EvalTest/eval-test-output.txt");
		evalOut.setLayoutData(dataOut);
	}
	
	public void disableEvalOut() {
		evalOut.setText("");
		evalOut.setEnabled(false);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	private void saveInput() {
		projectName = project.getText();
		pMain = mainP.getText();
		evalClass = eval.getText();
		eMain = mainE.getText();
		eOut = evalOut.getText();
	}
	
	@Override
	protected void okPressed() {
	  saveInput();
	  super.okPressed();
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectMain() {
		return pMain;
	}
	
	public String getEvalClass() {
		return evalClass;
	}
	
	public String getEvalMain() {
		return eMain;
	}
	
	public String getEvalOut() {
		return eOut;
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
