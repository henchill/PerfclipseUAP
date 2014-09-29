package perfclipse.main;

import org.eclipse.core.runtime.IProgressMonitor;

import perfclipse.perforations.PerforatedLoop;

public class EMonitor implements IProgressMonitor {
	private PerforationLaunch pl;
	private PerforatedLoop loop;
	private boolean isMulti;
	
	public EMonitor(PerforationLaunch pl, PerforatedLoop loop, boolean multi) {
		this.pl = pl;
		this.loop = loop;
		this.isMulti = multi;
	}
	
	@Override
	public void beginTask(String name, int totalWork) {
		// TODO Auto-generated method stub

	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		if (isMulti) {
			this.pl.evalMultiComplete();
		} else {
			this.pl.evalIndvComplete(this.loop);
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

	}

}
