package com.accsyn.installer;

import com.accsyn.installer.common.ASICommon;
import com.install4j.api.actions.AbstractInstallOrUninstallAction;
import com.install4j.api.context.Context;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.UninstallerContext;
import com.install4j.api.context.UserCanceledException;

public class ASIReadDaemonStatusAction extends AbstractInstallOrUninstallAction {

	private static final long serialVersionUID = 927384765708101477L;

	private boolean execute(Context context) {
		String daemon_user = ASICommon.detectDaemonUser();
		ASICommon.info("Current daemon user(accsyn_daemon_user): "+daemon_user);
		context.setVariable("accsyn_daemon_user", daemon_user != null ? daemon_user : "");
		
		String daemon_umask = ASICommon.detectDaemonUmask();
		ASICommon.info("Current daemon umask(accsyn_daemon_umask): "+daemon_umask);
		context.setVariable("accsyn_daemon_umask", daemon_umask != null ? daemon_umask : "");
		
		String conf_dir = ASICommon.getConfDir();
		ASICommon.info("Conf dir(accsyn_conf_dir): "+conf_dir);
		context.setVariable("accsyn_conf_dir", conf_dir);

		String data_dir = ASICommon.getDataDir();
		ASICommon.info("Data dir(accsyn_data_dir): "+data_dir);
		context.setVariable("accsyn_data_dir", data_dir);

		String log_dir = ASICommon.getLogDir();
		ASICommon.info("Conf dir(accsyn_log_dir): "+log_dir);
		context.setVariable("accsyn_log_dir", log_dir);
		
		String temp_dir = ASICommon.getTempDir();
		ASICommon.info("Temp dir(accsyn_temp_dir): "+temp_dir);
		context.setVariable("accsyn_temp_dir", temp_dir);

		return true;
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

