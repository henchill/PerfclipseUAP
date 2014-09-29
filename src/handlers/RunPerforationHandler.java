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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import perfclipse.main.LoopSelectionDialog;
import perfclipse.main.PerforationInfoDialog;
import perfclipse.main.PerforationLaunch;
import perfclipse.main.Results;
import perfclipse.main.ResultsJsonClass;
import perfclipse.main.ResultsViewer;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;
import perfclipse.perforations.PerforationException;
import wizards.RunPerforationWizard;


public class RunPerforationHandler extends AbstractHandler {
	
	private Shell shell;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);
		PerforationInfoDialog pid = new PerforationInfoDialog(shell);
		pid.create();
		List<PerforatedLoop> selectedLoops;
		boolean infoGiven = false;
		LoopSelectionDialog lsd = new LoopSelectionDialog(shell);
		String projectName = "";
		String eval = "";
		String pMain = "";
		String eMain = "";
		String eOut = "";
		
		IProject project = null;
		IProject evalProject = null;
		
		if (pid.open() == Window.OK) {
			infoGiven = true;
			projectName = pid.getProjectName();
			eval = pid.getEvalClass();
			pMain = pid.getProjectMain();
			eMain = pid.getEvalMain();
			eOut = pid.getEvalOut();
			project = PerforationLaunch.getProject(projectName);
			evalProject = PerforationLaunch.getProject(eval);
		}
		Results originalResult = null;
		Results perforatedResult = null;
		
		if (infoGiven) {
			lsd.setProject(project);
			lsd.create();
			if (lsd.open() == Window.OK){
				selectedLoops = lsd.getSelectedLoops();
				// switch selected loops to a map. Then use the same icu for all of them. 
				// call reparse with the new icu before doing any modifications. 
				
				PerforationLaunch pl = new PerforationLaunch(project, evalProject, pMain, eMain, this);
				
				pl.runMultiLoop(selectedLoops, eOut);			
			}
		}

		return null;
	}

	public void performCompleteAction(Results originalResult, Results perforatedResult, String originalOut, String perfOut) {
		if (originalResult != null && perforatedResult != null) {					
			ResultsViewer rv = new ResultsViewer(shell);
			rv.setResults(originalResult, perforatedResult);
			rv.setOutputFiles(originalOut, perfOut);
			rv.create();

			if (rv.open() == Window.CANCEL) {
				//do nothing?
			}
			
			// launch output viewer
		}
	}

}
