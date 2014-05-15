package perfclipse;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ResultsViewer extends Dialog{
	private Shell shell;
	private Display display;
	private TableViewer viewer;
	public ResultsViewer(Shell parent) {
		super(parent);
	}
	
//	@Override
//	protected void setShellStyle(int arg0) 
//	{
//		//Use the following so that no close X button will be created in the dialog
//		super.setShellStyle(SWT.TITLE);
//	}
	
	@Override
	protected Control createDialogArea(Composite composite) 
	{
		Composite area = (Composite) super.createDialogArea(composite);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(4, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    
		createResultsList(container);
		createComparisonView(container);
		getShell().setText("Results Viewer");
		/*
		 * Create your custome UI.
		 * Place all your UI components here
		 */
		return composite;
	}
	
	private void tmp() {
		display = new Display();
		this.shell = new Shell(display);
		
		GridLayout layout = new GridLayout(4, false);
		shell.setLayout(layout);
		
		
		
	}
	
	private void createResultsView(Composite container) {
		TableViewer sidebyside = new TableViewer(container);        
        
		TableViewerColumn left = new TableViewerColumn(sidebyside, SWT.NONE);
		TableViewerColumn right = new TableViewerColumn(sidebyside, SWT.NONE);
		final Table table = sidebyside.getTable();
		
		table.setHeaderVisible(false);
		table.setLinesVisible(true); 
		GridData dataList = new GridData();
		dataList.horizontalAlignment = GridData.FILL;
		dataList.horizontalSpan = 3;
		table.setLayoutData(dataList);		  

	}
	
	private Control createListView() {
		
		return null;
	}

	private Control createComparisonView(Composite container) {
//		?Image image = new Image(shell.getDisplay(), "/Perfclipse/icons/sample.gif");
		Label leftImgLabel = new Label(container, SWT.BORDER);
		leftImgLabel.setText("Image goes here");
		
		GridData leftImgData = new GridData(SWT.FILL, 
                SWT.FILL, true, true,
                1, 1);
		leftImgLabel.setLayoutData(leftImgData);
		
//		Image r_image = new Image(shell.getDisplay(), "/Perfclipse/icons/sample.gif");
		Label rightImgLabel = new Label(container, SWT.BORDER);
		rightImgLabel.setText("Image goes here");
		
		GridData rightImgData = new GridData(SWT.FILL, 
                SWT.FILL, true, true,
                1, 1);
		rightImgLabel.setLayoutData(rightImgData);
		
		return null;
	}

	private void createResultsList(Composite container) {
		viewer = new TableViewer(container);
		TableViewerColumn results = new TableViewerColumn(viewer, SWT.NONE);
		final Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(true); 
		GridData dataList = new GridData();
		dataList.horizontalAlignment = GridData.FILL;
		dataList.horizontalSpan = 1;
		table.setLayoutData(dataList);
	}
	
	@Override
	protected Button createButton(Composite arg0, int arg1, String arg2, boolean arg3) 
	{
		//Returns null so that no default buttons like "OK","Cancel" will be created
		return null;
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
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
