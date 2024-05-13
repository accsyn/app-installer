package com.accsyn.installer;

import java.io.File;
import java.io.IOException;

import com.accsyn.installer.common.ASICommon;
import com.install4j.api.actions.AbstractInstallOrUninstallAction;
import com.install4j.api.context.Context;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.ProgressInterface;
import com.install4j.api.context.UninstallerContext;
import com.install4j.api.context.UserCanceledException;

public class ASIFactoryResetAction extends AbstractInstallOrUninstallAction {

	private static final long serialVersionUID = 937384765708901477L;

	void recursiveDelete(File file) throws IOException {
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					recursiveDelete(entry);
				}
			}
		}
		if (!file.delete()) {
			throw new IOException("Failed to delete " + file);
		}
	}

	private boolean execute(Context context) {
		ProgressInterface progressInterface = context.getProgressInterface();
		String data_path = ASICommon.getLegacyDataDir();
		File f = new File(data_path);
		if (f.exists()) {
			try {
				recursiveDelete(f);
				progressInterface.setStatusMessage("Removed legacy v2 config");
			} catch (IOException e) {
				e.printStackTrace();
				progressInterface.setStatusMessage("[WARNING] FAILED to remove legacy v2 config");
			}
		}
		data_path = ASICommon.getDataDir();
		f = new File(data_path);
		if (f.exists()) {
			try {
				recursiveDelete(f);
				progressInterface.setStatusMessage("Removed local accsyn config");
			} catch (IOException e) {
				e.printStackTrace();
				progressInterface.setStatusMessage("[WARNING] FAILED removing local accsyn config");
			}
		}
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

