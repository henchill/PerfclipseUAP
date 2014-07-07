package wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
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

import perfclipse.PerforationLaunch;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;

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
	
	private void checkItems(TreeItem item, boolean checked) {
	    item.setGrayed(false);
	    item.setChecked(checked);
	    TreeItem[] items = item.getItems();
	    for (int i = 0; i < items.length; i++) {
	        checkItems(items[i], checked);
	    }
	}
	
	@Override
	public void createControl(Composite parent) {
		System.out.println("executed create control");
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
	    
	    buildTree();
	    System.out.println("tree built");
	    
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    tree.setLayoutData(gd);

	    setControl(container);
	    setPageComplete(true);
	}
	
	private void buildTree() {
		System.out.println("Building Tree");
		ClassSelection page = (ClassSelection) this.getWizard().getPage("Class Selection");
		String projectName = page.getProjectText();
		System.out.println("ProjectName: " + projectName);
		IProject iProject = PerforationLaunch.getProject(projectName);
		System.out.println(iProject == null);
		List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(iProject);
		HashMap<String, List<PerforatedLoop>> classMap = new HashMap<String, List<PerforatedLoop>>();
		for (PerforatedLoop loop : loops) {
			System.out.println("Executing: " + loop.getName());
			String className = loop.getName().split("-")[0];
			classMap.get(className).add(loop); 
		}
		
		Iterator it = classMap.entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry pairs = (HashMap.Entry)it.next();
	        TreeItem classNode = new TreeItem(tree, SWT.NONE);
	        classNode.setText((String) pairs.getKey());
	        for (PerforatedLoop loop : (ArrayList<PerforatedLoop>) pairs.getValue()) {
	        	TreeItem loopNode = new TreeItem(classNode, SWT.NONE);
	        	loopNode.setText(loop.getName());
	        }
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		
	}
	public TreeItem[] getSelectedLoops() {
	    return tree.getSelection(); 
	}


}
