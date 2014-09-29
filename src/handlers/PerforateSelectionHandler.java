package handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import perfclipse.perforations.JavaPerforation;
import perfclipse.perforations.PerforatedLoop;
import perfclipse.perforations.PerforationException;


public class PerforateSelectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("perforate selection handler");
		Shell shell = HandlerUtil.getActiveShell(event);
	    ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
	    ITextSelection sel = getITextSelection(editor);
	    IEditorInput input= editor.getEditorInput();
	    IFile file = ((FileEditorInput) input).getFile();
	    if (sel == null) {
	    	MessageDialog.openError(shell, "No loops selected", "You must select a loop you want to perforate.");
	    } else {		    		    	    	
		    IProject project = file.getProject();
		    JavaPerforation jp;
			try {
				jp = JavaPerforation.getPerforation(project, shell);
				PerforatedLoop pl = jp.perforateLoop(sel, editor);
				
				if (pl == null) {
					return null;
				} else {
					pl.perforate();
					pl.removeAnnotation();
				}
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (PerforationException pe) {
				pe.printStackTrace();
			}
	    }
	    return null;
	}
	
    private ITextSelection getITextSelection(ITextEditor textEditor) {
        return (ITextSelection) textEditor.getSelectionProvider().getSelection();
    }

}
