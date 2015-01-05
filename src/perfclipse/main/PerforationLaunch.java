package perfclipse.main;

import handlers.RunIndividualAnalysis;
import handlers.RunPerforationHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import perfclipse.perforations.PerforationException;

public class PerforationLaunch {
	private IProject project;
	private IProject evalProject;
	private String projMain;
	private String evalMain;
	private boolean processComplete;
	private List<PerforatedLoop> loopsToPerf;
	private Results originalResult;
	private Object origin;
	private List<PerforatedLoop> selectedLoops;
	private HashMap<String, Results> perforatedResults;
	private boolean originalRun;
	private String evalOut;
	
	public PerforationLaunch(IProject project, IProject eval, String projMain, String evalMain, Object origin) {
		this.project = project;
		this.evalProject = eval; 
		this.projMain = projMain;
		this.evalMain = evalMain;
		this.processComplete = true;
		this.origin = origin;
		this.perforatedResults = new HashMap<String, Results>();
		this.originalResult = null;
	}
	
	public void runIndLoopAnalysis(List<PerforatedLoop> loops, String eOut) {
		System.out.println("run indv loop analysis executed");
		this.processComplete = false;
		this.loopsToPerf = loops;
		this.originalRun = true;
		this.evalOut = eOut;
		this.runUnperforated(false);
		
	}
	
	public void runMultiLoop(List<PerforatedLoop> loops, String eOut) {
		System.out.println("run multi loop analysis executed");
		this.processComplete = false;
		this.selectedLoops = loops;
		this.originalRun = true;
		this.evalOut = eOut;
		this.runUnperforated(true);
	}
	
	public void runUnperforated(boolean isMulti) {
		System.out.println("running unperforated");
		PMonitor pm = new PMonitor(this, isMulti, null);
		try {
			launch(JavaCore.create(this.project), pm);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void runPerforated() {
		System.out.println("running perforated");
		PerforatedLoop currLoop = this.loopsToPerf.get(0);
		try {
			currLoop.perforate();
			List<PerforatedLoop> tmp = new ArrayList<PerforatedLoop>();
			tmp.add(currLoop);
			PMonitor pm = new PMonitor(this, false, currLoop);
			launch(JavaCore.create(project), pm);
		} catch (PerforationException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void isIndvComplete(long elapsedTime, PerforatedLoop loop) {
		Results result = new Results();
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = new ArrayList<PerforatedLoop>();
		
		if (originalRun) {
			originalRun = false;
			System.out.println("Unperforated run completed");
			result.RunName = String.format("UnPerforatedRun-%s", project.getName());
			this.originalResult = result;
			if (this.loopsToPerf.size() >= 1) { // still something to perforate and run
				this.runPerforated();
			} else { // finished
				RunIndividualAnalysis ria = (RunIndividualAnalysis) this.origin;
				ria.performCompleteAction(this.perforatedResults, this.project);
			}
		} else {
			result.RunName = String.format("PerforatedRun-%s", project.getName());
			
			result.Speedup = (int) (((float) this.originalResult.ElapsedTime / result.ElapsedTime) * 100) / 100.0;
			System.out.println("original elapsed time: " + Long.toString(this.originalResult.ElapsedTime));
			System.out.println("perforated elapsed time: " + Long.toString(result.ElapsedTime));
			System.out.println("speedup: " + Double.toString(result.Speedup));
			
			result.PerforatedLoops.add(loop);
			this.perforatedResults.put(loop.getName(), result);
			this.loopsToPerf.remove(0);
			
			EMonitor em = new EMonitor(this, loop, false);
			try {
				launch(JavaCore.create(evalProject), em);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void isMultiComplete(long elapsedTime) {
		System.out.println("Is multi complete executed");
		Results result = new Results();
		result.ElapsedTime = elapsedTime;
		
		if (originalRun) {
			originalRun = false;
			result.RunName = String.format("UnPerforatedRun-%s", project.getName());
			result.PerforatedLoops = new ArrayList<PerforatedLoop>();
			this.originalResult = result;
			for (PerforatedLoop pl: selectedLoops) {
				try {
					pl.perforate();
				} catch (PerforationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for (PerforatedLoop pl2 : selectedLoops) {
					try {
						pl2.reparse(pl.getCompilationUnit(), pl.getName());
					} catch (PerforationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			PMonitor pm = new PMonitor(this, true, null);
			try {
				launch(JavaCore.create(this.project), pm);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			result.RunName = String.format("PerforatedRun-%s", project.getName());
			result.Speedup = this.originalResult.ElapsedTime / result.ElapsedTime;
			result.PerforatedLoops = selectedLoops;
			
			for (PerforatedLoop pl: selectedLoops) {
				try {
					pl.unperforate();
				} catch (PerforationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for (PerforatedLoop pl2 : selectedLoops) {
					try {
						pl2.reparse(pl.getCompilationUnit(), pl.getName());
					} catch (PerforationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			this.perforatedResults.put("multiResult", result);
			EMonitor em = new EMonitor(this, null, true);
			try {
				launch(JavaCore.create(evalProject), em);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void evalIndvComplete(PerforatedLoop loop) {
		System.out.println("Eval completion started");
		Results currRes = perforatedResults.get(loop.getName());
		// calculate/extract quality of service
		try {
			BufferedReader br = new BufferedReader(new FileReader(evalOut));
			String currLine;
			while((currLine = br.readLine()) != null) {
				String[] tmp = currLine.split("=");
				if (tmp[0].trim().equals("QOS")) {
					currRes.QualityOfService = Double.parseDouble(tmp[1].trim());
				}
			}

			perforatedResults.put(loop.getName(), currRes);
			
			if (this.loopsToPerf.size() >= 1) { // still something to perforate and run
				this.runPerforated();
			} else { // finished
				RunIndividualAnalysis ria = (RunIndividualAnalysis) this.origin;
				ria.performCompleteAction(this.perforatedResults, this.project);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void evalMultiComplete() {
		System.out.println("Eval completion started");
		// calculate/extract quality of service
		Results currRes = perforatedResults.get("multiResult");
		try {
			BufferedReader br = new BufferedReader(new FileReader(evalOut));
			String currLine;
			String originalOut = "";
			String perfOut = "";
			while((currLine = br.readLine()) != null) {
//				System.out.println(currLine);
				String[] tmp = currLine.split(Pattern.quote("="));
//				System.out.println(tmp.toString());
				if (tmp[0].trim().equals("QOS")) {
//					System.out.println("found qos: " + tmp[1]);
					currRes.QualityOfService = Double.parseDouble(tmp[1].trim());
				} else if (tmp[0].trim().equals("ORIGINAL_OUTPUT")) {
//					System.out.println("found original: " + tmp[1]);
					originalOut = tmp[1].trim();
				} else if (tmp[0].trim().equals("PERFORATED_OUTPUT")) {
//					System.out.println("found perforated: " + tmp[1]);
					perfOut = tmp[1].trim();
				}
			}

			RunPerforationHandler rph = (RunPerforationHandler) this.origin;

			rph.performCompleteAction(this.originalResult, currRes, originalOut, perfOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private long launch(IJavaProject proj, IProgressMonitor pm) throws CoreException {
		System.out.println("launching project: " + proj.getElementName());
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
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, this.projMain);
		ILaunchConfiguration config = wc.doSave();
		long startTime = System.currentTimeMillis();
//		DebugUITools.launch(config, ILaunchManager.RUN_MODE);
		
		config.launch(ILaunchManager.RUN_MODE,  pm);
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
