package perfclipse.main;

import org.eclipse.core.runtime.IProgressMonitor;

import perfclipse.perforations.PerforatedLoop;
import perfclipse.perforations.PerforationException;

public class PMonitor implements IProgressMonitor {
	private long startTime;
	private long endTime;
	private long elapsedTime;
	private PerforationLaunch pl;
	private Results result;
	private PerforatedLoop loop;
	private boolean isMulti;
	
	public PMonitor(PerforationLaunch pl, boolean isMulti, Object o) {
		this.pl = pl;
		this.loop = (PerforatedLoop) o;
		this.isMulti = isMulti;
	}
	
	@Override
	public void beginTask(String name, int totalWork) {
		// TODO Auto-generated method stub
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public void done() {
		
		// TODO Auto-generated method stub
		System.out.println("process monitor has completed");
		if (!isMulti && loop != null) {
			try {
				this.loop.unperforate();
			} catch (PerforationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		endTime = System.currentTimeMillis();
		if (!isMulti) {
			this.pl.isIndvComplete(endTime - startTime, this.loop);
		} else {
			this.pl.isMultiComplete(endTime - startTime);
		}
	}

	@Override
	public void internalWorked(double work) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTaskName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subTask(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void worked(int work) {
		// TODO Auto-generated method stub
		System.out.println("got here");
	}

}
