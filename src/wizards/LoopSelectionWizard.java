package wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class LoopSelectionWizard extends WizardPage {
	
	private Tree tree;
	private Composite container;

	protected LoopSelectionWizard() {
		super("Loop Selection");
		setTitle("Loop Selection");
		setDescription("Which marked loops should be perforated");
	}
	
	private void checkPath(TreeItem item, boolean checked, boolean grayed) {
	    if (item == null) return;
	    if (grayed) {
	        checked = true;
	    } else {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}
	
	static void checkItems(TreeItem item, boolean checked) {
	    item.setGrayed(false);
	    item.setChecked(checked);
	    TreeItem[] items = item.getItems();
	    for (int i = 0; i < items.length; i++) {
	        checkItems(items[i], checked);
	    }
	}
	
	

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
	    layout.numColumns = 1;
	    
	    tree = new Tree(container, SWT.BORDER | SWT.CHECK);
	    tree.addListener(SWT.Selection, new Listener() {
	        @Override
			public void handleEvent(Event event) {
	            if (event.detail == SWT.CHECK) {
	                TreeItem item = (TreeItem) event.item;
	                boolean checked = item.getChecked();
	                checkItems(item, checked);
	                checkPath(item.getParentItem(), checked, false);
	            }
	        }
	    });
	    // Modify to add class and methods. 
	    for (int i = 0; i < 4; i++) {
	        TreeItem itemI = new TreeItem(tree, SWT.NONE);
	        itemI.setText("Item " + i);
	        for (int j = 0; j < 4; j++) {
	            TreeItem itemJ = new TreeItem(itemI, SWT.NONE);
	            itemJ.setText("Item " + i + " " + j);
	            for (int k = 0; k < 4; k++) {
	                TreeItem itemK = new TreeItem(itemJ, SWT.NONE);
	                itemK.setText("Item " + i + " " + j + " " + k);
	            }
	        }
	    }
	    
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    tree.setLayoutData(gd);

	    setControl(container);
	    setPageComplete(true);
	}

	public TreeItem[] getSelectedLoops() {
	    return tree.getSelection(); 
	}


}
