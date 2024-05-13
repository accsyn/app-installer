package com.accsyn.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.accsyn.installer.common.ASICommon;
import com.install4j.api.actions.AbstractInstallOrUninstallAction;
import com.install4j.api.context.Context;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.UninstallerContext;
import com.install4j.api.context.UserCanceledException;

public class ASIWriteCredentialsAction extends AbstractInstallOrUninstallAction {

	private static final long serialVersionUID = 937384765708901477L;

	private boolean execute(Context context) {
		String client_id = (String)context.getVariable("accsyn_client_id");
		if (client_id == null || client_id.length()==0) {
			ASICommon.error("Cannot write server credentials to temp file - variable not set!");
		}
		String conf_dir = ASICommon.getConfDir();
		File f_credentials = new File(conf_dir, ".accsyn_client_id");
		
		PrintWriter writer;
		try {
			if (!f_credentials.getParentFile().exists())
				ASICommon.info("Creating '" + f_credentials.getParentFile() + "'");
			ASICommon.info("Writing credentials to disk @ '" + f_credentials.getName() + "'");
			f_credentials.getParentFile().mkdirs();
			writer = new PrintWriter(f_credentials.getAbsolutePath(), "UTF-8");
			writer.println(client_id);
			writer.close();
			ASICommon.info("Successfully wrote credentials.");
			// Make sure user running daemon can remove it afterwards
			f_credentials.setWritable(true); 
		} catch (FileNotFoundException e) {
			ASICommon.warning(e);
		} catch (UnsupportedEncodingException e) {
			ASICommon.warning(e);
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

