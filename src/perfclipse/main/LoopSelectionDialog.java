package perfclipse.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;

public class LoopSelectionDialog extends TitleAreaDialog {
	private IProject iProject;
	private Tree tree;
	private Shell shell;
	private List<PerforatedLoop> selectedLoops;
	private HashMap<String, List<PerforatedLoop>> classMap;
	
	public LoopSelectionDialog(Shell parentShell) {
		super(parentShell);
//		iProject = project;	
		shell = parentShell;
	}
	
	public void setProject(IProject project) {
		System.out.println("set project called");
		System.out.println(project == null);
		iProject = project;
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Loop Selection");
		setMessage("Which marked loops should be perforated");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(2, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    buildTree(container);
	    return area;
	}

	private void buildTree(Composite container) {
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
	    
	    JavaPerforation.removePerforation(iProject);
		try {
			JavaPerforation jp = JavaPerforation.getPerforation(iProject, shell);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(iProject);		
		classMap = new HashMap<String, List<PerforatedLoop>>();
		for (PerforatedLoop loop : loops) {			
			String className = loop.getClassName();
			if (classMap.containsKey(className)) {
				classMap.get(className).add(loop);
			} else {
				classMap.put(className, new ArrayList<PerforatedLoop>());
				classMap.get(className).add(loop);
			}
			 
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
	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    GridData data = new GridData(GridData.FILL_BOTH);
	    tree.setLayoutData(data);
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
	
	public List<PerforatedLoop> getSelectedLoops() {
	    return selectedLoops; 
	}

	private void saveInput() {
		selectedLoops = new ArrayList<PerforatedLoop>();
		TreeItem[] selected = tree.getSelection();
		for (TreeItem item : tree.getSelection()) {
			System.out.println(item.getText());
			TreeItem[] subItems = item.getItems();
			System.out.println(classMap.toString());
			List<PerforatedLoop> subLoops = classMap.get(item.getText());
			System.out.println(subLoops.toString());
			if (subItems.length > 0) { 
				for (TreeItem subItem : subItems) {
					for (PerforatedLoop loop: subLoops) {
						if (loop.getName().equals(subItem.getText())) {
							selectedLoops.add(loop);
						}
					}
				}
			}			
		}		
	}
	
	@Override
	protected void okPressed() {
	  saveInput();
	  super.okPressed();
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
}
