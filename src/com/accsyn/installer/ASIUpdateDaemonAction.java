package com.accsyn.installer;

import com.accsyn.installer.common.ASICommon;
import com.install4j.api.actions.AbstractInstallOrUninstallAction;
import com.install4j.api.context.Context;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.UninstallerContext;
import com.install4j.api.context.UserCanceledException;

public class ASIUpdateDaemonAction extends AbstractInstallOrUninstallAction {

	private static final long serialVersionUID = 927384765708101478L;

	private String parseVar(Object obj) {
		String s = (String)obj;
		if (s != null && s.equals("$?"))
			s = null;
		return s;
	}
	
	private boolean execute(Context context) {
		String daemon_user = parseVar(context.getVariable("accsyn_daemon_user"));
		String daemon_password =  parseVar(context.getVariable("accsyn_daemon_password"));
		String daemon_umask = parseVar(context.getVariable("accsyn_daemon_umask"));
		if ((daemon_user != null && 0<daemon_user.length()) || (daemon_umask != null && 0<daemon_umask.length())) {
			ASICommon.info("Configuring daemon with user: "+daemon_user+", password: *, umask: "+daemon_umask+ " (OS: "+System.getProperty("os.name")+", invoking user: "+System.getProperty("user.name")+")");
			return ASICommon.updateDaemon(daemon_user, daemon_password, daemon_umask);
		} else {
			ASICommon.info("Not updating daemon config - no username or umask defined!");
			return true;
		}
	}

	@Override
	public boolean install(InstallerContext context) throws UserCanceledException {
		return execute(context);
	}

	@Override
	public boolean uninstall(UninstallerContext arg0) throws UserCanceledException {
		return false;
	}

}

