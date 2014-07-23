package wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.TreeItem;

import perfclipse.PerforationEvaluation;
import perfclipse.main.PerforationLaunch;
import perfclipse.main.Results;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;

public class RunPerforationWizard extends Wizard {

	protected ClassSelection classSel;
	protected LoopSelectionWizard loopSel;
	private String sharedProject;

	private HashMap<String, Results[]> output;
	private List<Results> results;
	public RunPerforationWizard() {
	    super();
	    setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		classSel = new ClassSelection();
		loopSel = new LoopSelectionWizard();
		addPage(classSel);
		addPage(loopSel);
	}
	
	@Override
	public boolean performFinish() {
		boolean valid = validateInputs();
		if (valid) {
			String main = classSel.getMainText();
			String project = classSel.getProjectText();
			String eval = classSel.getEvalText();		
			try {
				PerforationLaunch pl = new PerforationLaunch();
				results = new ArrayList<Results>(); 
//				results.add(pl.runUnperforated(project, main, eval));
				results.add(runPerforated(project, main, eval));				
				
			}
			catch (CoreException e) {
				e.printStackTrace();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private boolean validateInputs() {
		IProject project = PerforationLaunch.getProject(classSel.getProjectText());
		PerforationEvaluation eval = PerforationLaunch.getEvalObject(classSel.getEvalText());
		
		if (project == null && eval == null) {
			MessageDialog.openError(getShell(), "Class Selection Error", "Could not find the specified project and evaluation class");
			return false;
		} else if (project == null && eval != null) {
			MessageDialog.openError(getShell(), "Project Name Error", "Could not find the specified project name");
			return false;
		} else if (eval == null && project != null) {
			MessageDialog.openError(getShell(), "Evaluation Class Error", "Could not find the specified evaluation class");
			return false;
		} else {
			return true;
		}		
	}

	private boolean isProjectInputValid() {
		IProject project = PerforationLaunch.getProject(classSel.getProjectText());
		if (project != null) {
			return true;
		}
		return false;
	}
	
	private boolean isEvaluationInputValid() {
		PerforationEvaluation eval = PerforationLaunch.getEvalObject(classSel.getEvalText());
		if (eval != null) {
			return true;
		}
		return false;
	}
	
	private Results runPerforated(String proj, String main, String evalClass) throws CoreException {
		TreeItem[] selectedLoops = loopSel.getSelectedLoops();
		IProject project = PerforationLaunch.getProject(proj);
		List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(project);
		List<PerforatedLoop> sLoops = new ArrayList<PerforatedLoop>();
		for (TreeItem item : selectedLoops){
			String parent = item.getParentItem().getText();
			String name = item.getText();
			String loopName = parent + "-" + name;
			for (PerforatedLoop loop : loops) {
				if (!loop.getName().equals(loopName)) {
//					loop.setFactor(0);
				} else {
//					loop.setFactor();
					sLoops.add(loop);
				}
			}
		}
		PerforationLaunch pl = new PerforationLaunch();
		return pl.runPerforated(project, main, evalClass, sLoops);
	}

	public HashMap<String, Results[]> getOutput() {
		return output;
	}
	
	public List<Results> getResults() {
		return results;
	}
}
