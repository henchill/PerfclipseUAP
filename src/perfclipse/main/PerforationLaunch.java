package perfclipse.main;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import perfclipse.PerforationEvaluation;
import perfclipse.perforations.PerforatedLoop;

public class PerforationLaunch {
	
	public Results runUnperforated(IProject project, String main, String evalClass) throws CoreException{		
		Results result = new Results();
		long elapsedTime = launch(JavaCore.create(project), main);		
		
		result.RunName = String.format("UnPerforatedRun-%s", project.getName());
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = new ArrayList<PerforatedLoop>();
		return result;
	}
	
	public Results runPerforated(IProject project, String main, String evalClass, List<PerforatedLoop> loops) throws CoreException {		
		Results result = new Results();
		long elapsedTime = launch(JavaCore.create(project), main);
//		PerforationEvaluation pe = getEvalObject(evalClass);
//		qos = PerforationLaunch.getEvalObject(evalClass).evaluate();
//		getEvaluationClass(project, evalClass);
//		launch(JavaCore.create(project), evalClass);
		result.QualityOfService = 0.15; //pe.evaluate();
		result.RunName = String.format("PerforatedRun-%s", project.getName());
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = loops;
		return result;		
	}
	
	private long launchProgram (IJavaProject proj, String main) throws CoreException {
		IVMInstall vm = JavaRuntime.getVMInstall(proj);
		System.out.println(vm.toString());
		if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
		IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
		String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(proj);
		System.out.println(cp);
		VMRunnerConfiguration config = new VMRunnerConfiguration(main, cp);
		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		long startTime = System.currentTimeMillis();
		vmr.run(config, launch, new NullProgressMonitor());
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}
	
	private long launch(IJavaProject proj, String main) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = 
				manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.getName().equals("Start Perforation")) {
				configuration.delete();
				break;
			}
		}
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "Start Perforation");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
				proj.getElementName());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, main);
		ILaunchConfiguration config = wc.doSave();
		long startTime = System.currentTimeMillis();
//		DebugUITools.launch(config, ILaunchManager.RUN_MODE);
		config.launch(ILaunchManager.RUN_MODE,  null);
		long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}
	
	public static PerforationEvaluation getEvalObject(String evalClass) {
		PerforationEvaluation obj;
		try {
			Class<?> eval = Class.forName(evalClass);
			obj = (PerforationEvaluation) eval.newInstance();
			System.out.println(eval.getMethods());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		return obj;
		
		
	}
	
	public static void getEvaluationClass(IProject project, String evalName) {
		IJavaProject jp = JavaCore.create(project);
		try {
			URL classUrl = new URL("file:C:\\Users\\Happy\\Documents\\Workspace\\runtime-EclipseApplication\\Test1\\bin");
			URL[] urls = new URL[]{classUrl};

		    // Create a new class loader with the directory
		    ClassLoader cl = new URLClassLoader(urls);
		    cl.loadClass("main");
			System.out.println(cl.toString());
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    // Get all projects in the workspace
	    IProject[] projects = root.getProjects();
	    for (IProject project : projects) {	    	
	    	if (project.getName().equals(projectName)) {
	    		return project;
	    	}
	    }
	    return null;
	}
}
