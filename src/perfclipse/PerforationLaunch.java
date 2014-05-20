package perfclipse;

import java.util.ArrayList;
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

import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;

public class PerforationLaunch {
	
	public Results runUnperforated(String proj, String main, String evalClass) throws CoreException{
		IProject project = PerforationLaunch.getProject(proj);
		List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(project);
		for (PerforatedLoop loop : loops){
			// loop.setfactor(0);
		}
		Results result = new Results();
		long elapsedTime = launch(JavaCore.create(project), main);

		result.QualityOfService = (double) .99;
		
		result.RunName = String.format("UnPerforatedRun-%s", project.getName());
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = new ArrayList<PerforatedLoop>();
		return result;
	}
	
	public Results runPerforated(IProject project, String main, String evalClass, List<PerforatedLoop> loops) throws CoreException {		
		Results result = new Results();
		long elapsedTime = launch(JavaCore.create(project), main);
//		Object qos;

//		qos = PerforationLaunch.getEvalObject(evalClass).evaluate();
		result.QualityOfService = (double) .99;
		result.RunName = "PerforatedRun-%s".format(project.getName());
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = loops;
		return result;		
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
	
	public static PerforationEvaluation getEvalObject(String evalClass) {
		PerforationEvaluation obj;
		try {
			Class<?> eval = Class.forName(evalClass);
			obj = (PerforationEvaluation) eval.newInstance();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		return obj;
	}
	
	public static IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    // Get all projects in the workspace
	    IProject[] projects = root.getProjects();
	    for (IProject project : projects) {	    	
	    	if (project.getName().equals(projectName)) {
	    		System.out.println(project.getName() + "---" + projectName);
	    		return project;
	    	}
	    }
	    return null;
	}
}
