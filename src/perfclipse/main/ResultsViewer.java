package perfclipse.main;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import perfclipse.perforations.PerforatedLoop;

public class ResultsViewer extends Dialog{
	private Shell shell;
	private Display display;
	private TableViewer viewer;
	private Results original;
	private Results perforated;
	private GridData openPerfData;
	private String perfOut;
	private String originalOut;
	private Composite imgContainer;
	private Label origImg;
	private Label perfImg;
	
	public ResultsViewer(Shell parent) {
		super(parent);
	}
	
	@Override
	protected Control createDialogArea(Composite composite) 
	{
		Composite area = (Composite) super.createDialogArea(composite);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(1, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);
	    
	    createResultsView(container);
		createComparisonView(container);
		getShell().setText("Results Viewer");
//		setImages();
		return composite;
	}
	
	@Override
	public void create() {
		super.create();
		setOriginalImage();
		setPerfImage();
	}
	
	public void setImages() {
		setOriginalImage();
		setPerfImage();
	}
	
	private void createResultsView(Composite container) {
		Label origInfo = new Label(container, SWT.SINGLE | SWT.WRAP | SWT.BORDER | SWT.LEFT);
		origInfo.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		origInfo.setText(buildStringOutput(perforated));
		
		GridData origInfoData = new GridData();
		origInfoData.horizontalAlignment = SWT.FILL;				
		origInfoData.grabExcessHorizontalSpace = true;		
		origInfo.setLayoutData(origInfoData);
	}
	
	public void setResults (Results orig, Results perf){
		original = orig;
		perforated = perf;
	}
	
	public void setOutputFiles(String orig, String perf) {
		perfOut = perf;
		originalOut = orig;
	}
	
	private void setOriginalImage() {
		Shell shell = imgContainer.getShell();
//		imgContainer.pack();
		if (originalOut != null) {
			System.out.println(originalOut);
			Image originalImage = new Image(shell.getDisplay(), originalOut);
			
			Rectangle imgBounds = originalImage.getBounds();
			Rectangle labelBounds = origImg.getBounds();
			Rectangle labelBounds2 = perfImg.getBounds();

			System.out.println(labelBounds);
			System.out.println(imgBounds);
			
			double widthScale = ((double) labelBounds.width) / imgBounds.width;
			double heightScale = ((double) labelBounds.height) / imgBounds.height;
			double factor = 1.0;
			
			System.out.println(widthScale);
			System.out.println(heightScale);
			System.out.println(factor);
			
			if (widthScale < 1 && heightScale < 1) {
				factor = widthScale > heightScale ? heightScale : widthScale;
			} else if (widthScale < 1) {
				factor = widthScale;
			} else if (heightScale < 1) {
				factor = heightScale;
			}
			
			
			Image finalImg = new Image(shell.getDisplay(),
					originalImage.getImageData().scaledTo((int) (imgBounds.width * factor), 
							(int) (imgBounds.height * factor)));
			origImg.setImage(finalImg);
		}
		
		
	}
	
	private void setPerfImage() {
		Shell shell = imgContainer.getShell();
		if (perfOut != null) {					
			Image originalImage = new Image(shell.getDisplay(), perfOut);
			
			Rectangle imgBounds = originalImage.getBounds();
			Rectangle labelBounds = perfImg.getBounds();
			double widthScale = ((double) labelBounds.width) / imgBounds.width;
			double heightScale = ((double) labelBounds.height) / imgBounds.height;
			double factor = 1.0;
			if (widthScale < 1 && heightScale < 1) {
				factor = widthScale > heightScale ? heightScale : widthScale;
			} else if (widthScale < 1) {
				factor = widthScale;
			} else if (heightScale < 1) {
				factor = heightScale;
			}
			Image finalImg = new Image(shell.getDisplay(),
					originalImage.getImageData().scaledTo((int) (imgBounds.width * factor), 
							(int) (imgBounds.height * factor)));
			perfImg.setImage(finalImg);
		}
	}
	private Control createListView() {
		return null;
	}

	private String openDialog(Shell shell) {
		// File standard dialog
	    FileDialog fileDialog = new FileDialog(shell);
	    // Set the text
	    fileDialog.setText("Select File");
	    // Set filter on .txt files
	    fileDialog.setFilterExtensions(new String[] { "*.jpg", "*.jpeg", "*.png" });
	    // Put in a readable name for the filter
//	    fileDialog.setFilterNames(new String[] { "Textfiles(*.txt)" });
	    // Open Dialog and save result of selection
	    String selected = fileDialog.open();
	    return selected;
	}
	private Control createComparisonView(Composite container) {
		final Composite comp = new Composite(container, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH | SWT.CENTER));
		imgContainer = comp;
		GridLayout layout = new GridLayout(2, false);
//		layout.marginLeft = 40;
//		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(layout);
		
		Button openOrig = new Button(comp, SWT.PUSH | SWT.CENTER);
		openOrig.setText("Add Original Output");
		openOrig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = comp.getShell();
				openDialog(shell);
			}
		});
		
		GridData openOrigData = new GridData(SWT.CENTER);
		openOrigData.horizontalAlignment = SWT.FILL;		
		openOrigData.horizontalSpan = 1;
		openOrigData.grabExcessHorizontalSpace = true;
		openOrig.setLayoutData(openOrigData);

		final Button openPerf = new Button(comp, SWT.PUSH | SWT.CENTER);
		openPerf.setText("Add Perforated Output");
		openPerfData = new GridData(SWT.CENTER);
		openPerfData.horizontalAlignment = SWT.FILL;
		openPerfData.horizontalSpan = 1;
		openPerfData.grabExcessHorizontalSpace = true;
		openPerf.setLayoutData(openPerfData);
		
		origImg = new Label(comp, SWT.CENTER);
//		origImg.setText("something");
		GridData origImgData = new GridData(SWT.CENTER);
		origImgData.horizontalSpan = 1;
		origImgData.horizontalAlignment = SWT.FILL;
		origImgData.verticalAlignment = SWT.FILL;
		origImgData.grabExcessHorizontalSpace = true;
		origImgData.grabExcessVerticalSpace = true;
		origImg.setLayoutData(origImgData);
			
		perfImg = new Label(comp, SWT.CENTER);
//		perfImg.setText("test");
		GridData perfImgData = new GridData(SWT.CENTER);
		perfImgData.horizontalSpan = 1;
		perfImgData.horizontalAlignment = SWT.FILL;
		perfImgData.verticalAlignment = SWT.FILL;
		perfImgData.grabExcessHorizontalSpace = true;
		perfImgData.grabExcessVerticalSpace = true;
		perfImg.setLayoutData(perfImgData);
		
//		comp.pack();
		
//		shell.pack();
		
		openPerf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = comp.getShell();
				if (perfOut == null) {
					perfOut = openDialog(shell);
				}
				
//				createImgLabel(selected);
				if (perfOut != null) {					
					Image originalImage = new Image(shell.getDisplay(), perfOut);
					
					Rectangle imgBounds = originalImage.getBounds();
					Rectangle labelBounds = perfImg.getBounds();
					double widthScale = ((double) labelBounds.width) / imgBounds.width;
					double heightScale = ((double) labelBounds.height) / imgBounds.height;
					double factor = 1.0;
					if (widthScale < 1 && heightScale < 1) {
						factor = widthScale > heightScale ? heightScale : widthScale;
					} else if (widthScale < 1) {
						factor = widthScale;
					} else if (heightScale < 1) {
						factor = heightScale;
					}
					Image finalImg = new Image(shell.getDisplay(),
							originalImage.getImageData().scaledTo((int) (imgBounds.width * factor), 
									(int) (imgBounds.height * factor)));
					perfImg.setImage(finalImg);
				}
//				openPerf.dispose(); 
			}
		});
		
		openOrig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = comp.getShell();
				if (originalOut == null) {
					originalOut = openDialog(shell);
				}
//				String selected = openDialog(shell);
//				createImgLabel(selected);
				if (originalOut != null) {					
					Image originalImage = new Image(shell.getDisplay(), originalOut);
					
					Rectangle imgBounds = originalImage.getBounds();
					Rectangle labelBounds = perfImg.getBounds();
					double widthScale = labelBounds.width / imgBounds.width;
					double heightScale = labelBounds.height / imgBounds.height;
					double factor = 1.0;
					if (widthScale < 1 && heightScale < 1) {
						factor = widthScale > heightScale ? heightScale : widthScale;
					} else if (widthScale < 1) {
						factor = widthScale;
					} else if (heightScale < 1) {
						factor = heightScale;
					}
					Image finalImg = new Image(shell.getDisplay(),
							originalImage.getImageData().scaledTo((int) (imgBounds.width * factor), 
									(int) (imgBounds.height * factor)));
					origImg.setImage(finalImg);
				}
//				openPerf.dispose(); 
			}
		});

		return null;
	}

	private String buildStringOutput(Results result) {
		StringBuilder sb = new StringBuilder();
		sb.append("Perforated Results\n");
		sb.append("Quality of Service: ");
		sb.append(result.QualityOfService);
		sb.append("\n");
		sb.append("SpeedUp: ");
		sb.append(result.Speedup);
		sb.append("\n");
		sb.append("Perforated Loops: [");
		for (PerforatedLoop loop: result.PerforatedLoops) {			
			sb.append(loop.getClassName());
			sb.append("/");
			sb.append(loop.getName());
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	private void createResultsList(Composite container) {
		viewer = new TableViewer(container);
		TableViewerColumn results = new TableViewerColumn(viewer, SWT.NONE);
		final Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(true); 
		GridData dataList = new GridData();
		dataList.horizontalAlignment = GridData.FILL;
		dataList.horizontalSpan = 1;
		table.setLayoutData(dataList);
	}
	
	@Override
	protected Button createButton(Composite arg0, int arg1, String arg2, boolean arg3) 
	{
		//Returns null so that no default buttons like "OK","Cancel" will be created
		return null;
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Loop Perforation Config");
	}
	
	@Override  
	protected Point getInitialSize() {
	    return new Point(500, 400);
	}

}
