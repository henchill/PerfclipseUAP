package perfclipse;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ResultsViewer {
	private Shell shell;
	private TableViewer viewer;
	public ResultsViewer(Shell shell) {
		this.shell = shell;
		
		GridLayout layout = new GridLayout(4, false);
		shell.setLayout(layout);
		
		createResultsView();
		createResultsList();
	}
	
	private void createResultsView() {
		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        GridData data = new GridData(SWT.FILL, 
                SWT.FILL, true, true,
                3, 1);
        folder.setLayoutData(data);
        CTabItem ComparisonView = new CTabItem(folder, SWT.NONE);
        ComparisonView.setText("Compare Output");
        CTabItem ListView = new CTabItem(folder, SWT.NONE);
        ListView.setText("List Results");
        
        ComparisonView.setControl(createComparisonView());
        ListView.setControl(createListView());

	}
	
	private Control createListView() {
		// TODO Auto-generated method stub
		return null;
	}

	private Control createComparisonView() {
		// TODO Auto-generated method stub
		return null;
	}

	private void createResultsList() {
		viewer = new TableViewer(shell);
		TableViewerColumn results = new TableViewerColumn(viewer, SWT.NONE);
		final Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(true); 
		GridData dataList = new GridData();
		dataList.horizontalAlignment = GridData.FILL;
		dataList.horizontalSpan = 1;
		table.setLayoutData(dataList);
	}

}
