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

public class OutputFilenameDialog extends TitleAreaDialog {

	private Text originalField;
	private Text perforatedField;
	
	private String originalOutput;
	private String perforatedOutput;	
	
	
	public OutputFilenameDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Enter Output Filenames");
	    setMessage("Please enter the names of the output image files of the perforation run.", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(2, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    createOriginal(container);
	    createPerforated(container);		    

	    return area;
	}
		
	private void createOriginal(Composite container){
		Label lblOriginal = new Label(container, SWT.NONE);
		lblOriginal.setText("Original Output");

	    GridData dataOriginal = new GridData();
	    dataOriginal.grabExcessHorizontalSpace = true;
	    dataOriginal.horizontalAlignment = GridData.FILL;

	    originalField = new Text(container, SWT.BORDER);
	    originalField.setLayoutData(dataOriginal);
	}
		
	private void createPerforated(Composite container) {
	    Label lblPerforated = new Label(container, SWT.NONE);
	    lblPerforated.setText("Perforated Output");
	    
	    GridData dataPerforated = new GridData();
	    dataPerforated.grabExcessHorizontalSpace = true;
	    dataPerforated.horizontalAlignment = GridData.FILL;
	    
	    perforatedField = new Text(container, SWT.BORDER);
	    perforatedField.setLayoutData(dataPerforated);
	}
		
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	private void saveInput() {
		originalOutput = originalField.getText();
		perforatedOutput = perforatedField.getText();			
	}
		
	@Override
	protected void okPressed() {
	  saveInput();
	  super.okPressed();
	}

	public String getOriginalOutput() {
		return originalOutput;
	}

	public String getPerforatedOutput() {
		return perforatedOutput;
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
