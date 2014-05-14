package handlers;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import perfclipse.Results;
import wizards.RunPerforationWizard;


public class RunPerforationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		RunPerforationWizard wizard = new RunPerforationWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		List<Results> results;
		if (dialog.open() == Window.OK) {
			results = wizard.getResults();
			recordResults(results);
	    } else {
		    System.out.println("Cancel pressed");
		}
		
		return null;
	}
	
	void launch(IJavaProject proj, String main) throws CoreException {
		IVMInstall vm = JavaRuntime.getVMInstall(proj);
		if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
		IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
		String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(proj);
		VMRunnerConfiguration config = new VMRunnerConfiguration(main, cp);
		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		vmr.run(config, launch, null);
	}
	
	private void recordResults(List<Results> results) {
		String projectName = results.get(0).RunName.split("-")[1];
		String filename = "PerforationMultiLoopResults.csv";
		File resultsOut = new File(filename);
		if(!resultsOut.exists()) {
			try {
				resultsOut.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		try
		{
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmm");			
		    FileWriter writer = new FileWriter(resultsOut, true);
	 
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
