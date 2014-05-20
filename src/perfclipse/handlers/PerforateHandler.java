package perfclipse.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ltk.core.refactoring.Change;

import perfclipse.PerforationTypeDialog;
import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;
import perfclipse.perforations.PerforationException;

public class PerforateHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
	    ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
	    ITextSelection sel = getITextSelection(editor);
	    IEditorInput input= editor.getEditorInput(); 
	    IFile file = ((FileEditorInput) input).getFile();
	    if (sel == null) {
	    	MessageDialog.openError(shell, "No loops selected", "You must select a loop you want to perforate.");
	    }
	    else {
	    	PerforationTypeDialog dialog = new PerforationTypeDialog(shell);
		    dialog.create();
		    if (dialog.open() == Window.OK) {
		    	String factor = dialog.getFactor();		    	
		    	IEditorPart iep = HandlerUtil.getActiveEditor(event);
			    IProject project = file.getProject();
			    JavaPerforation jp;
				try {
					jp = JavaPerforation.getPerforation(project, shell);
					PerforatedLoop pl = jp.perforateLoop(sel, editor);
					if (pl == null) {
						return null;
					}
					ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
			        ICompilationUnit icu = (ICompilationUnit) typeRoot.getAdapter(ICompilationUnit.class);
					try {
						pl.setFactor(Integer.parseInt(factor), icu);
						
					} catch (PerforationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    }
	    }
	    return null;
	}

    private ITextSelection getITextSelection(ITextEditor textEditor) {
        return (ITextSelection) textEditor.getSelectionProvider().getSelection();
    }
}