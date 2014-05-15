package perfclipse;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

public class ResultsViewer {
	private Shell shell;
	public ResultsViewer(Shell shell) {
		this.shell = shell;
		
		GridLayout layout = new GridLayout(4, false);
		shell.setLayout(layout);
		
		createResultsView();
		createResultsList();
	}
	private void createResultsView() {
		// TODO Auto-generated method stub
		
	}
	private void createResultsList() {
		
		GridData dataList = new GridData();
		dataList.horizontalAlignment = GridData.FILL;
		dataList.horizontalSpan = 2;
//		button2.setLayoutData(gridData);
		
	}
}
