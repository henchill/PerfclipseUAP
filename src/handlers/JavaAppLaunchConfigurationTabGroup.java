package handlers;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

public class JavaAppLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public JavaAppLaunchConfigurationTabGroup() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		 ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
              new JavaMainTab(),              
              new JavaArgumentsTab(), 
              new JavaJRETab(),
              new JavaClasspathTab(),            
              // new SourceLookupTab(),
              new EnvironmentTab(), 
              new CommonTab() 
      };
      setTabs(tabs);

	}

}
