package handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.ITextEditor;

import perfclipse.PerforationInfoDialog;
import perfclipse.PerforationTypeDialog;
import perfclipse.Results;

public class RunIndividualAnalysis extends AbstractHandler {
    
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		
		Shell shell = HandlerUtil.getActiveShell(event);
	    
	    
	    PerforationInfoDialog dialog = new PerforationInfoDialog(shell);
	    dialog.create();
	    if (dialog.open() == Window.OK) {
	    	String project = dialog.getProjectName();
	    	String main = dialog.getMainClass();
	    	String eval = dialog.getEvalClass();
	    	IJavaProject jProject = getJavaProject(project);
	    	if (jProject != null) { // && eval class is correct
		    	// runOriginal();
		    	// get all perforations
		    	// for each perforation:
		    	// 		runPerforated(loop);
	    		//		evaluate result
	    		// 		add annotation.
	    		//  
	    	}
	    }

		return null;
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

	private Results runUnperforated(IJavaProject project, String main) throws CoreException {		
		long elapsedTime = launch(project, main);
		Results result = new Results();
		result.RunName = "UnPerforatedRun";
		result.ElapsedTime = elapsedTime;
		result.PerforatedLoops = null;
		return result;
	}
	
	private Results runPerforated(IJavaProject project, String main) throws CoreException {		
		long elapsedTime = launch(project, main);
		Results result = new Results();
		result.RunName = "PerforatedRun";
		result.ElapsedTime = elapsedTime;
//		result.PerforatedLoops = loopSel.getSelectedLoops();
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

	private void generateOutput(Results[] results) {
		try
		{
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmm");
			String sdt = df.format(new Date(System.currentTimeMillis()));
		    FileWriter writer = new FileWriter("RunIndividualAnalysis-%s.csv".format(sdt));
	 
		    writer.append("Loop");
		    writer.append(',');
		    writer.append("Quality Of Service");
		    writer.append(',');
		    writer.append("Performance");
		    writer.append('\n');
		    
		    for (Results result : results) {
		    	writer.append(result.PerforatedLoops[0].getText());
		    	writer.append(',');
		    	writer.append((String) result.QualityOfService);
		    	writer.append(',');
		    	writer.append(df.format(new Date(result.ElapsedTime)));
		    	writer.append('\n');
		    }
	 
		    //generate whatever data you want
	 
		    writer.flush();
		    writer.close();
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
	}
}
