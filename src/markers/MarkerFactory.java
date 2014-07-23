package markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class MarkerFactory {
	
	public static final Map<String, String> markerMap;// = new HashMap<String, String>();
	static {
		HashMap<String, String> tmp = new HashMap<String, String>();
		tmp.put("GREENMARKER", "Perfclipse.greenMarker");
		tmp.put("GREENANNOTATION", "Perfclipse.greenAnnotation");
		markerMap = Collections.unmodifiableMap(tmp);
	}
	
	public static IMarker createMarker(IResource res, String markerName, String msg, Position position) throws CoreException {
		IMarker marker = null;		
		marker = res.createMarker(markerMap.get(markerName));
		marker.setAttribute(IMarker.MESSAGE, msg);
		int start = position.getOffset();
		int end = position.getOffset() + position.getLength();
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, end);
		return marker;
	}
	
	public static List<IMarker> findAllMarkers(IResource  resource) {
        try {
            return Arrays.asList(resource.findMarkers(markerMap.get("GREENMARKER"), true, IResource.DEPTH_INFINITE));
        } catch (CoreException e) {
            return new ArrayList<IMarker>();
        }
    }
	
	public static void deleteAllMarkers(IResource resource) {
		try {
			List<IMarker> markers = Arrays.asList(resource.findMarkers(markerMap.get("GREENMARKER"), true, IResource.DEPTH_INFINITE));
			for (IMarker marker : markers) {
				marker.delete();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	public static String addAnnotation(IMarker marker, String annotation, Position position, CompilationUnit cu) throws CoreException {		
		IPath path = cu.getJavaElement().getPath();
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		bufferManager.connect(path, LocationKind.IFILE, null); // (1)
      	ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
      	IDocument document = textFileBuffer.getDocument();
      	IAnnotationModel iamf = textFileBuffer.getAnnotationModel();
		
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(markerMap.get(annotation), marker);

		iamf.connect(document);
		iamf.addAnnotation(ma, position);
		iamf.disconnect(document);
		
		String newSource = document.get();
		//bufferManager.disconnect(path, LocationKind.IFILE, null);
		return newSource;
		
		
		
	}

}
