package handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import perfclipse.PerforationEvaluation;
import perfclipse.PerforationInfoDialog;
import perfclipse.PerforationLaunch;
import perfclipse.PerforationTypeDialog;
import perfclipse.Results;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;

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
	    	IProject iProject = PerforationLaunch.getProject(project);
	    	PerforationEvaluation evalObj = PerforationLaunch.getEvalObject(eval);
	    	List<Results> results = new ArrayList<Results>();
	    	
	    	if (iProject != null) { // && eval class is correct
	    		PerforationLaunch pl = new PerforationLaunch();
	    		try {
					results.add(pl.runUnperforated(project, main, eval));
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		JavaPerforation jp;
		    	try {
		    		jp = JavaPerforation.getPerforation(iProject, shell);
					List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(iProject);
					if (loops != null) {
						for (PerforatedLoop loop : loops) {
							loop.setFactor(0);
						}
						for (PerforatedLoop loop : loops) {
							loop.setFactor();
							List<PerforatedLoop> tmp = new ArrayList<PerforatedLoop>();
							tmp.add(loop);
							Results res = pl.runPerforated(iProject, main, eval, tmp);
							results.add(res);
							loop.setFactor(0);
							// TODO Add if statement for marker
							loop.addMarker("GREENMARKER", "GREENANNOTATION", res);
							// add anotation with qos and time performance.
						}
					}
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	recordResults(results);
	    	} else {
	    		MessageDialog.openError(shell, "Class Selection Error", "Could not find the specified project or evaluation class");
	    	}
	    	
	    }

		return null;
	}

	private void recordResults(List<Results> results) {
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
		    	writer.append(result.PerforatedLoops.get(0).getName());
		    	writer.append(',');
		    	writer.append((String) result.QualityOfService);
		    	writer.append(',');
		    	writer.append(String.valueOf(result.ElapsedTime)); //df.format(new Date(result.ElapsedTime)));
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
