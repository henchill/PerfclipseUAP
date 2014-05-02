package handlers;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PerforationTypeDialog extends TitleAreaDialog {
	private Text iterToKeepText;
	private Text txtFactor;
	
	private String iterNum;
	private String perfFactor;
	
	public PerforationTypeDialog(Shell parentShell) {
	    super(parentShell);
	}
	
	@Override
	public void create () {
		super.create();
		setTitle("Perforation Factor");
	    setMessage("Select a perforation factor and iterations to keep", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(2, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    createIterToKeep(container);
	    createFactor(container);

	    return area;
	}
	
	private void createFactor(Composite container){
		Label lblFactor = new Label(container, SWT.NONE);
	    lblFactor.setText("Factor");

	    GridData dataFactor = new GridData();
	    dataFactor.grabExcessHorizontalSpace = true;
	    dataFactor.horizontalAlignment = GridData.FILL;

	    txtFactor = new Text(container, SWT.BORDER);
	    txtFactor.setLayoutData(dataFactor);
	}
	
	private void createIterToKeep(Composite container) {
	    Label lblIterToKeep = new Label(container, SWT.NONE);
	    lblIterToKeep.setText("Iterations to Keep");
	    
	    GridData dataIterToKeep = new GridData();
	    dataIterToKeep.grabExcessHorizontalSpace = true;
	    dataIterToKeep.horizontalAlignment = GridData.FILL;
	    iterToKeepText = new Text(container, SWT.BORDER);
	    iterToKeepText.setLayoutData(dataIterToKeep);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	private void saveInput() {
		iterNum = iterToKeepText.getText();
		perfFactor = txtFactor.getText();
	}
	
	@Override
	protected void okPressed() {
	  saveInput();
	  super.okPressed();
	}

	public String getIterationNumber() {
		return iterNum;
	}

	public String getFactor() {
		return perfFactor;
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
