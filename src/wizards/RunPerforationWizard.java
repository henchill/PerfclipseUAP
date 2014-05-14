package wizards;

import java.util.HashMap;

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

import perfclipse.IPerforationEvaluation;
import perfclipse.Results;

public class RunPerforationWizard extends Wizard {

	protected ClassSelection classSel;
	protected LoopSelectionWizard loopSel;

	private HashMap<String, Results[]> output;
	
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
		String main = classSel.getMainText();
		String project = classSel.getProjectText();
		String eval = classSel.getEvalText();		
		try {
			Results[] results = {runUnperforated(project, main, eval),
								runPerforated(project, main, eval)};
			System.out.println(results);
			output = new HashMap<String, Results[]>();
			output.put("Run1", results);
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException f){
			f.printStackTrace();
		}
		catch (InstantiationException g){
			g.printStackTrace();
		}
		catch (IllegalAccessException h){
			h.printStackTrace();
		}
		return true;
	}
	
	private Results runUnperforated(String proj, String main, String evalFunction) throws CoreException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		IJavaProject project = getJavaProject(proj);
		if (project != null) {
			long elapsedTime = launch(project, main);
			Results result = new Results();
			result.RunName = "UnPerforatedRun";
			result.ElapsedTime = elapsedTime;
//			result.QualityOfService = evaluateOutput(evalFunction);
			result.PerforatedLoops = null;
			return result;
		} else {
			MessageDialog.openError(getShell(), "Error", "Could not find the project specified.");
			return null;
		}
	}
	
	private Results runPerforated(String proj, String main, String evalFunction) throws CoreException, ClassNotFoundException, InstantiationException, IllegalAccessException{
		IJavaProject project = getJavaProject(proj);
		long elapsedTime = launch(project, main);
		Results result = new Results();
		result.RunName = "PerforatedRun";
		result.ElapsedTime = elapsedTime;
//		result.QualityOfService = evaluateOutput(evalFunction);
		result.PerforatedLoops = loopSel.getSelectedLoops();
		return result;
	}
	
	private Object evaluateOutput(String evalFunction) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class eval = Class.forName(evalFunction);
		IPerforationEvaluation obj = (IPerforationEvaluation) eval.newInstance();
		
		return obj.evaluate();
	}
	
	private IJavaProject getJavaProject(String proj) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    // Get all projects in the workspace
	    IProject[] projects = root.getProjects();
	    for (IProject project : projects) {	    	
	    	if (project.getName().equals(proj)) {
	    		return JavaCore.create(project);
	    	}
	    }
		return null;
	}
	
	private long launch(IJavaProject proj, String main) throws CoreException {
		IVMInstall vm = JavaRuntime.getVMInstall(proj);
		if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
		IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
		String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(proj);
		VMRunnerConfiguration config = new VMRunnerConfiguration(main, cp);
		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		long startTime = System.currentTimeMillis();
		vmr.run(config, launch, null);
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}
	
	public HashMap<String, Results[]> getOutput() {
		return output;
	}
}
