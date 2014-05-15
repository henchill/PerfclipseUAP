package markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class GreenMarkerUpdater implements IMarkerUpdater {

	@Override
	public String getMarkerType() {
		// TODO Auto-generated method stub
		return MarkerFactory.markerMap.get("GREENMARKER");
	}

	@Override
	public String[] getAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument document,
			Position position) {
		try {
			int start = position.getOffset();
			int end = position.getOffset() + position.getLength();
			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);
			return true;
		} catch (CoreException e) {
			return false;
		}
	}

}
