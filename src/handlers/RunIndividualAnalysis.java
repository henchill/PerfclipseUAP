package handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import markers.MarkerFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.ITextEditor;

import perfclipse.PerforationEvaluation;
import perfclipse.main.MethodVisitor;
import perfclipse.main.PerforationInfoDialog;
import perfclipse.main.PerforationLaunch;
import perfclipse.main.PerforationTypeDialog;
import perfclipse.main.Results;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;
import perfclipse.perforations.PerforationException;

public class RunIndividualAnalysis extends AbstractHandler {
    
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		
		Shell shell = HandlerUtil.getActiveShell(event);
	    
	    
	    PerforationInfoDialog dialog = new PerforationInfoDialog(shell);
	    dialog.create();
//	    dialog.disableEvalOut();
	    if (dialog.open() == Window.OK) {
	    	String project = dialog.getProjectName();
	    	String pMain = dialog.getProjectMain();
	 
	    	String eval = dialog.getEvalClass();
	    	String eMain = dialog.getEvalMain();
	    	String eOut = dialog.getEvalOut();
	    	
	    	IProject iProject = PerforationLaunch.getProject(project);
	    	IProject eProject = PerforationLaunch.getProject(eval);
	  
	    	
//	    	PerforationEvaluation evalObj = PerforationLaunch.getEvalObject(eval);
	    	
	    	if (iProject != null && eProject != null) { // && eval class is correct
	    		PerforationLaunch pl = new PerforationLaunch(iProject, eProject, pMain, eMain, this);
	    		
	    		
	    		
//	    		Results originalResult = null;
//	    		HashMap<String, Results> perforatedResults = new HashMap<String, Results>();
				
	    		JavaPerforation.removePerforation(iProject);
	    		JavaPerforation jp;
	    		try {
					jp = JavaPerforation.getPerforation(iProject, shell);
					
					removeMarkers(iProject);
					
					List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(iProject);
					pl.runIndLoopAnalysis(loops, eOut);
					
//					// Run unperforated version of the project
//					try {
//						originalResult = pl.runUnperforated(iProject, main, eval);
//					} catch (CoreException e) {
//						e.printStackTrace();
//					}
//					
//		    		// Run each individual perforated loop
//					List<PerforatedLoop> loops = JavaPerforation.getPerforatedLoops(iProject);
//					for (PerforatedLoop loop : loops) {
//						try {
//							loop.perforate();
//							List<PerforatedLoop> tmp = new ArrayList<PerforatedLoop>();
//							tmp.add(loop);
//							Results res = pl.runPerforated(iProject, main, eval, tmp);
//							if (originalResult != null) {
//								res.Speedup = (double) originalResult.ElapsedTime / res.ElapsedTime;								
//							}													
//							perforatedResults.put(loop.getName(), res);	
//							loop.unperforate();								
//						} catch (PerforationException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
					
//					// add marker annotation
//					iProject.getWorkspace().save(true, null);
//					JavaPerforation.removePerforation(iProject);
////					jp = JavaPerforation.getPerforation(iProject, shell);
//					addMarkersToSource(iProject, perforatedResults); 
					
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
	    	} else {
	    		MessageDialog.openError(shell, "Class Selection Error", "Could not find the specified project or evaluation class");
	    	}
	    	
	    }

		return null;
	}
	
	public void performCompleteAction(HashMap<String, Results> perforatedResults, IProject iProject) {
//		try {
//			iProject.getWorkspace().save(true, null);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		JavaPerforation.removePerforation(iProject);
//		jp = JavaPerforation.getPerforation(iProject, shell);
		addMarkersToSource(iProject, perforatedResults); 
		System.out.println("addMarkersToSource executed");
	}

	private void removeMarkers(IProject project) {
		try {
			IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		
		    // parse(JavaCore.create(project));
		    for (IPackageFragment mypackage : packages) {
		    	if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
		    		for (ICompilationUnit icu : mypackage.getCompilationUnits()) {		    			
		    			MarkerFactory.deleteAllMarkers(icu.getResource());
		    		}
		    	}
		    }
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void addMarkersToSource(IProject project, HashMap<String, Results> perforatedResults) {		
		try {
			IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		
		    // parse(JavaCore.create(project));
		    for (IPackageFragment mypackage : packages) {
		    	if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
		    		for (ICompilationUnit icu : mypackage.getCompilationUnits()) {
		    			CompilationUnit cu = JavaPerforation.parse(icu);
		    			MethodVisitor visitor = new MethodVisitor();
		    			cu.accept(visitor);
		    			for (MethodDeclaration method : visitor.getMethods()) {
		    				List<ASTNode> modifiers = (List<ASTNode>) method.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);		    						
		    				for (ASTNode modifier : modifiers) {
		    					if (modifier instanceof Annotation) {
		    						NormalAnnotation na = (NormalAnnotation) modifier;
		    						String annotationName = na.getTypeName().getFullyQualifiedName();		    						
		    						if (annotationName.equals("Perforated")) {					    				
					    				String methodName = method.getName().getFullyQualifiedName();					    									    				
					    				Results result = perforatedResults.get(methodName);
					    				
					    				if (result == null) {
					    					System.out.println("result was null");
					    					break;
					    				} else {
					    					System.out.println(result.QualityOfService);
					    					if (result.QualityOfService < 0.25 && result.QualityOfService >= 0) {
					    						String msg = "Perforation Results: QOS = %s; Speedup = %s";
						  		      			msg = String.format(msg, String.valueOf(result.QualityOfService), String.valueOf(result.Speedup));
						  		      			
							    				Position position = new Position(method.getStartPosition(),
					  		      						method.getLength());
					  		      				IMarker marker = MarkerFactory.createMarker(icu.getResource(), "GREENMARKER",
					  		      						msg, position);
					  		      				
					  		      				String newSource = MarkerFactory.addAnnotation(marker, "GREENANNOTATION", position, cu);
					  		      				icu.getBuffer().setContents(newSource);
					    					}
					    				}
					    				
		    						}
		    					}
		    				}
		    			}
		            }
		    	}
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
