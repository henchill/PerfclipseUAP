package handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;


public class PerforateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Shell shell = HandlerUtil.getActiveShell(event);
	    ITextEditor editor = (ITextEditor) HandlerUtil.getActiveEditor(event);
	    ITextSelection sel = getITextSelection(editor);
	    if (sel == null) {
	    	MessageDialog.openError(shell, "No loops selected", "You must select a loop you want to perforate.");
	    	return null;
	    }
	    
	    PerforationTypeDialog dialog = new PerforationTypeDialog(shell);
	    dialog.create();
	    if (dialog.open() == Window.OK) {
	    	System.out.println(dialog.getIterationNumber());
	    	System.out.println(dialog.getFactor());
	    }
	    System.out.println("Execute Perforate Loop");
	    return null;
	}
	
    private ITextSelection getITextSelection(ITextEditor textEditor) {
        return (ITextSelection) textEditor.getSelectionProvider().getSelection();
    }

}
