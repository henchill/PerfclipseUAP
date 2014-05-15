package perfclipse;

import java.util.List;

import org.eclipse.swt.widgets.TreeItem;

import perfclipse.perforations.PerforatedLoop;

public class Results {
	public String RunName;
	public long ElapsedTime;
	public double Speedup;
	public Object QualityOfService;
	public List<PerforatedLoop> PerforatedLoops;
	public String toString () {
		String tmp = "{RunName: %s,\nElapsedTime: %s}";
		return String.format(tmp, RunName, String.valueOf(ElapsedTime));
				
	}
}

