package com.accsyn.installer.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.install4j.api.Util;

public class ASICommon {

	public static final String VERSION = "1.3-3";

	public static final String OS_WINDOWS = "windows";
	public static final String OS_MAC = "mac";
	public static final String OS_LINUX = "linux";
	public static final String OS_SOLARIS = "solaris";
	
	public static int DEFAULT_REST_PORT = 443;

	public static final int REST_GET = 0;
	public static final int REST_PUT = 1;
	public static final int REST_POST = 2;
	public static final int REST_DELETE = 3;
	
	public ASICommon() {
		// TODO Auto-generated constructor stub
	}


	public static boolean isDEV() {
		return System.getenv("AS_DEV") != null;
	}

	// Registry REST calls

	public static final String info(String s) {
		Util.logInfo("Accsyn-installer", "(ASI, " + (new Date()) + ") " + s);
		System.out.println("(ASI, " + (new Date()) + ") " + s);
		return s;
	}
	
	public static final String warning(String s) {
		return info("[WARNING] "+s);
	}

	public static final String warning(Exception e) {
		return warning(strStackTrace(e));
	}
	
	public static final String error(String s) {
		info("[ERROR] " + s);
		Util.logError("Accsyn-installer", "(ASI, " + (new Date()) + ") [WARNING] " + s);
		System.err.println("(ASI, " + (new Date()) + ") [WARNING] " + s);
		return s;
	}

	public static String strStackTrace(Exception e) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.getBuffer().toString();
		} finally {
			if (pw != null)
				pw.close();
			if (pw != null)
				pw.close();
		}
	}

	public static String error(Exception e) {
		String s = strStackTrace(e);
		Util.log(e);
		error(s);
		return s;
	}

	public static String getOS() {

		String s = System.getProperty("os.name");
		if (s == null) {
			System.err.println("[Startup] ERROR! Could not determine operating system name!");
		} else if (s.indexOf("Linux") > -1) {
			return OS_LINUX;
		} else if (s.indexOf("Windows") > -1) {
			return OS_WINDOWS;
		} else if (s.indexOf("Mac OS X") > -1) {
			return OS_MAC;
		} else if (s.indexOf("SunOS") > -1) {
			return OS_SOLARIS;
		}
		return null;
	}


	public static boolean isMac() {
		return getOS().equalsIgnoreCase(OS_MAC);
	}

	public static boolean isWindows() {
		return getOS().equalsIgnoreCase(OS_WINDOWS);
	}
	
	public static boolean isLinux() {
		return getOS().equalsIgnoreCase(OS_LINUX);
	}
	
	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			error(e);
			return "?hostname?";
		}
	}
	
	boolean checkExistingConfiguration() {
		return false;
	}

	public static String readFile(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					if (0 < sb.length())
						sb.append("\n");
					sb.append(line);
					line = br.readLine();
				}
				return sb.toString();
			} finally {
				br.close();
			}
		} catch (Exception e) {
			info("[WARNING] " + e.toString());
		}
		return null;
	}
	
	public static void writeFile(String path, String write_text) {
		PrintWriter writer;
		try {
			File f = new File(path);
			if (!f.getParentFile().exists())
				ASICommon.info("Creating '" + f.getParentFile() + "'");
			f.getParentFile().mkdirs();
			writer = new PrintWriter(path, "UTF-8");
			writer.println(write_text);
			writer.close();
		} catch (FileNotFoundException e) {
			warning(e);
		} catch (UnsupportedEncodingException e) {
			warning(e);
		}
	}
	public static String[] generateCommandLine(String executable, String[] args) {
		String cmd[];
		if (getOS().equals(OS_WINDOWS)) {
			if (!executable.equalsIgnoreCase("cmd.exe")) {
				cmd = new String[3 + (args != null ? args.length : 0)];
				cmd[0] = "cmd.exe";
				cmd[1] = "/C";
				cmd[2] = executable;
				if (args != null) {
					for (int idx = 0; idx < args.length; idx++) {
						cmd[3 + idx] = args[idx];
					}
				}
			} else {
				cmd = new String[1+(args != null ? args.length : 0)];
				cmd[0] = executable;
				if (args != null) {
					for (int idx = 0; idx < args.length; idx++) {
						cmd[1 + idx] = args[idx];
					}
				}
			}
		} else {
			if (!executable.equalsIgnoreCase("/bin/bash")) {
				cmd = new String[3];
				cmd[0] = "/bin/bash";
				cmd[1] = "-c";
				cmd[2] = executable;
				if (args != null) {
					for (int idx = 0; idx < args.length; idx++) {
						cmd[2] = cmd[2] + (0 < cmd[2].length() ? " " : "") + args[idx];
					}
				}
			} else {
				cmd = new String[1+(args != null ? args.length : 0)];
				cmd[0] = executable;
				if (args != null) {
					for (int idx = 0; idx < args.length; idx++) {
						cmd[1 + idx] = args[idx];
					}
				}
			}
		}
		return cmd;
	}
	

	public static Object[] executeCatchOutput(String executable, String[] args) {
		Object[] retval = { -1, null };
		try {
			String[] cmd = generateCommandLine(executable, args);
			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec(cmd);
			// any error message?
			ASStreamGobbler errorGobbler = new ASStreamGobbler(proc.getErrorStream(), "STDERR", true);

			// any output?
			ASStreamGobbler outputGobbler = new ASStreamGobbler(proc.getInputStream(), "STDOUT", true);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error??
			retval[0] = proc.waitFor();
			Thread.sleep(25); // Let streams finish read
			String output_merged = errorGobbler.output.trim();
			if (0 < output_merged.length())
				output_merged += "\n";
			output_merged += outputGobbler.output.trim();
			retval[1] = output_merged;
		} catch (Throwable t) {
			retval[1] = t.toString();
			info("[WARNING] "+t.toString());
		}
		return retval;
	}
	
	
	private static class ASStreamGobbler extends Thread {
		InputStream is;
		String type;
		String output;
		boolean silent;

		public ASStreamGobbler(InputStream is, String type, boolean store_output) {
			this.is = is;
			this.type = type;
			if (store_output)
				this.output = "";
			else
				this.output = null;
			this.silent = silent;
		}

		@Override
		public void run() {
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (!this.silent)
						ASICommon.info(type + ">" + line);
					if (this.output != null)
						this.output += line + "\n";
				}
			} catch (IOException e) {
				info("[WARNING] ASStreamGobbler; "+e.toString());
			} finally {
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
					}
				if (isr != null)
					try {
						isr.close();
					} catch (IOException e) {
					}
			}
		}
	}
	
	// Config

	public static File ensureDirExists(File f) {
		if (!f.exists()) {
			ASICommon.info("Creating: '" + f.getAbsolutePath() + "'...");
			f.mkdirs();
		}
		return f;
	}
	
	private static String getRootPathEnv() {
		return System.getenv("AS_ROOT_PATH");
	}	
	
	public static String getRootDir() {
		/*Return the accsyn root dir, can be transposed by environment variables */
		if (getRootPathEnv() != null) {
			return getRootPathEnv();
		} else {
			if (getOS().equals(OS_WINDOWS))
				return System.getenv("SystemDrive");
			else
				return "/";
		}
	}

	public static String getHomeDir() {
		String result;
		if (getOS().equals(OS_WINDOWS))
			result = System.getenv("USERPROFILE");
		else if (getOS().equals(OS_MAC))
			result = System.getenv("HOME");
		else if (getOS().equals(OS_LINUX))
			result = System.getenv("HOME");
		else {
			System.err.println("Can't evauluate home dir - unsupported OS!");
			return null;
		}
		if (getRootPathEnv() != null) {
			if (getOS().equals(OS_WINDOWS)) {
				// TODO: support home dir on network drive
				result = getRootDir()+result.substring(2);
			} else
				result = getRootDir()+result;
		}
		return result;
	}

	public static String getConfDir() {
		String retval = null;
		// Central settings, only one daemon allowed per machine
		if (getOS().equals(OS_WINDOWS))
			retval = getRootDir() + File.separator + "ProgramData" + File.separator  + "accsyn";
		else if (getOS().equals(OS_MAC))
			retval = getRootDir()+  File.separator + "Library" +  File.separator + "Preferences" + File.separator  +  "com.accsyn";
		else
			retval = getRootDir() +  File.separator + "etc" + File.separator  +  "accsyn";

		ASICommon.info("[" + (new SimpleDateFormat("yyMMdd_HHmmss.SSS")).format(new Date()) + "] Using configuration dir: '" + retval + "'");
		return retval;
	}


	public static String getDataDir() {
		String retval = null;
		// Central data, only one daemon allowed per machine
		if (getOS().equals(OS_WINDOWS))
			// return "C:\\ProgramData\\Accsyn";
			retval = getRootDir() + File.separator + "ProgramData"+ File.separator + "accsyn" + File.separator + "data";
		else if (getOS().equals(OS_MAC))
			retval = getRootDir() + File.separator + "Library" + File.separator + "Application Support" + File.separator + "com.accsyn" + File.separator + "data";
		else
			retval = getRootDir() + File.separator + "var" + File.separator + "lib" + File.separator +  "accsyn";
		
		File f = ensureDirExists(new File(retval));

		ASICommon.info("[" + (new SimpleDateFormat("yyMMdd_HHmmss.SSS")).format(new Date()) + "] Using data dir: '" + f.getAbsolutePath() + "'");
		return f.getAbsolutePath();
	}

	public static boolean hasData() {
		File data_dir = new File(getDataDir());
		if (!data_dir.exists())
			return false;
		File users_dir = new File(data_dir.getAbsolutePath(), "users");
		if (!users_dir.exists())
			return false;
		return users_dir.listFiles().length>0;
	}
	
	public static String getLegacyDataDir() {
		String retval = null;
		// Central data, only one daemon allowed per machine
		if (getOS().equals(OS_WINDOWS))
			// return "C:\\ProgramData\\Accsyn";
			retval = getRootDir() + File.separator + "ProgramData"+ File.separator + "accsyn";
		else if (getOS().equals(OS_MAC))
			retval = getRootDir()+  File.separator + "Library" +  File.separator + "Preferences" + File.separator  +  "com.accsyn";
		else
			retval = getRootDir() + File.separator + "etc" + File.separator + "accsyn";
		
		File f = ensureDirExists(new File(retval));

		ASICommon.info("[" + (new SimpleDateFormat("yyMMdd_HHmmss.SSS")).format(new Date()) + "] Using legacy data dir: '" + f.getAbsolutePath() + "'");
		return f.getAbsolutePath();
	}

	public static boolean hasLegacyData() {
		File data_dir = new File(getLegacyDataDir());
		if (!data_dir.exists())
			return false;
		File domain_txt = new File(data_dir.getAbsolutePath(), "domain.txt");
		return domain_txt.exists();
	}
	
	
	public static String getLogDir() {
		return getLogDir(null);
	}

	public static String getLogDir(String subdir) {
		String retval;
		// Central log, only one daemon allowed per machine
		if (getOS().equals(OS_WINDOWS))
			// return "C:\\ProgramData\\Accsyn";
			retval = getRootDir() + File.separator + "ProgramData" + File.separator +  "accsyn" + File.separator + "log";
		else
			retval = getRootDir() + File.separator  + "var" + File.separator  + "log" + File.separator + "accsyn";

		File result = new File(retval + (subdir != null ? File.separator + subdir : ""));
		ensureDirExists(result);
		ASICommon.info("[" + (new SimpleDateFormat("yyMMdd_HHmmss.SSS")).format(new Date()) + "] Using log dir: '" + result.getAbsolutePath() + "'");
		return result.getAbsolutePath();
	}
	
	public static String getTempDir() {
		String sys_tmp_dir = null;
		sys_tmp_dir = System.getProperty("java.io.tmpdir");
		if (sys_tmp_dir == null) {
			if (isWindows())
				sys_tmp_dir = System.getenv("TMPDIR") != null ? System.getenv("TMPDIR") : System.getenv("TMPPATH");
			else
				sys_tmp_dir = System.getenv("TEMP") != null ? System.getenv("TEMP") : System.getenv("TMP");
		}
		if (sys_tmp_dir == null || !(new File(sys_tmp_dir)).exists()) {
			warning("Could not evaulate system temporary directory!");
		}
		if (getRootPathEnv() != null) {
			if (isWindows())
				sys_tmp_dir = getRootDir()+sys_tmp_dir.substring(2);
			else
				sys_tmp_dir = getRootDir()+sys_tmp_dir;
		}
		File result = new File(sys_tmp_dir + File.separator + ".accsyn");
		ensureDirExists(result);
		info("[" + (new SimpleDateFormat("yyMMdd_HHmmss.SSS")).format(new Date()) + "] Using temp dir: '" + result.getAbsolutePath() + "'");
		return result.getAbsolutePath();
	}
	// Registry REST
	
	/*
	 * private static final File getDownloadAccSynCert(boolean download) throws
	 * Exception { File result = new File(getConfDir() + File.separator +
	 * "accsyn.keystore"); if (!result.getParentFile().exists()) {
	 * result.getParentFile().mkdirs(); if (result.getParentFile().exists())
	 * info("Created '" + result.getParent() + "'.."); else throw new
	 * Exception("Failed to create AccSyn conf dir: "+result.getParentFile()+"!");
	 * download = true; } else if (!result.exists()) download = true; if (download)
	 * try { String url = "http://accsyn.com/cert/accsyn.keystore";
	 * info("Downloading cert store from '" + url + "'"); URL website = new
	 * URL(url); URLConnection con = website.openConnection();
	 * con.setConnectTimeout(5 * 1024); con.setReadTimeout(5 * 1024);
	 * ReadableByteChannel rbc = Channels.newChannel(website.openStream());
	 * FileOutputStream fos = new FileOutputStream(result.getAbsolutePath()); long
	 * size = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	 * info("Downloaded cert '" + result.getName() + "'(" + size + "b)"); } catch
	 * (IOException e) { error(e); } return result; }
	 */

	public static JSONObject serializeRESTData(JSONObject d) {
		for (Iterator<String> iterator = d.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Object o = d.get(key);
			if (o instanceof JSONObject) {
				serializeRESTData((JSONObject) o);
			} else if (o instanceof ArrayList) {
				ArrayList<Object> serialized_list = new ArrayList<Object>();
				for (int idx = 0; idx < ((ArrayList<Object>) o).size(); idx++) {
					Object o2 = ((ArrayList<Object>) o).get(idx);
					if (o2 instanceof JSONObject)
						serialized_list.add(serializeRESTData((JSONObject) o2));
					else
						serialized_list.add(o2);
				}
				d.put(key, serialized_list);
			} else if (o instanceof Date) {
				d.put(key, (new SimpleDateFormat("yy-MM-dd'T'HH:mm:ss.SSS")).format((Date) o));
			}
		}
		return d;
	}

	public static Object[] deSerializeRESTResponse(Object o) {
		if (o instanceof JSONObject) {
			// Dig down into JSON
			JSONObject d = (JSONObject) o;
			for (Iterator<String> iterator = d.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				Object[] sub_retval = deSerializeRESTResponse(d.get(key));
				if (sub_retval[1].equals(Boolean.TRUE))
					d.put(key, sub_retval[0]);
			}
		} else if (o instanceof ArrayList) {
			// Replace with a new list
			ArrayList<Object>l = new ArrayList<>();
			boolean did_modify = false;
			for (int idx = 0; idx < ((ArrayList<Object>) o).size(); idx++) {
				Object o2 = ((ArrayList<Object>) o).get(idx);
				Object[] sub_retval = deSerializeRESTResponse(o2);
				if (sub_retval[1].equals(Boolean.TRUE)) {
					l.add(sub_retval[0]);
					did_modify = true;
				} else
					l.add(o2);
			}
			if (did_modify)
				return new Object[] { l, Boolean.TRUE };
		} else if (o instanceof String) {
			Pattern p = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}[T]{1}[0-9]{2}:[0-9]{2}:[0-9]{2}$");
			if (p.matcher((String) o).find()) {
				DateFormat format = new SimpleDateFormat("yy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
				try {
					return new Object[] { format.parse((String) o), Boolean.TRUE };
				} catch (Exception e) {
					error(e);
				}
			} 
		}
		return new Object[] { o, Boolean.FALSE };
	}

	public static HashMap<String, String> createHeaders(String name, String value, String key) {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(key, name + " " + new String(Base64.encodeBase64(value.getBytes())));
		return headers;
	}

	public static JSONObject rest(final int method, String uri, JSONObject data) {
		// Do a registry REST call
		return rest(null, method, uri, data, null);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject rest(String workspace, final int method, String uri, JSONObject data, HashMap<String, String> headers) {
		info("rest(" + workspace + "," + method + ",'" + uri + "'," + data + ")");
		String hostname;
		int port = DEFAULT_REST_PORT;
		if (isDEV()) {
			hostname = "http://localhost";
			port = 8180;
		} else {
			if (workspace == null)
				hostname = "https://api.master.accsyn.com";
			else
				hostname = "https://" + workspace + ".accsyn.com";
		}

		String url = null, text = null, str_method = "?";
		int retries = 2, timeout = 60 * 1000;
		try {
			if (retries <= 0)
				retries = 2;
			int iterations = 0;
			while (0 < retries) {
				CloseableHttpClient httpClient = null;
				try {
					boolean ssl = false;
					httpClient = HttpClients.createDefault();

					// Proxy applied?
					String proxy_hostname_port = System.getenv("ACCSYN_PROXY");
					if (proxy_hostname_port == null)
						proxy_hostname_port = System.getenv("FILMHUB_PROXY");
					if (proxy_hostname_port != null) {
						String[] parts = proxy_hostname_port.split(":");
						String _hostname;
						int _port;
						try {
							if (2 == parts.length) {
								_hostname = parts[0];
								_port = Integer.parseInt(parts[1]);
							} else {
								_hostname = proxy_hostname_port;
								_port = 80;
							}
							info("Using proxy @ " + _hostname + ":" + _port + " instead of " + hostname + ":" + port + "..");
							hostname = "http://" + _hostname;
							port = _port;
						} catch (Exception e) {
							error(e);
							throw new Exception("ACCSYN_PROXY environment '" + proxy_hostname_port + "' has wrong syntax! Should be on the form '<hostname or IP>:<port>'. Details: " + e.toString());
						}
					}
					url = hostname;
					if (uri == null)
						uri = "";
					url += ":" + port + "/api/v3/" + uri;

					if (data != null) {
						try {
							text = serializeRESTData(data).toString();
						} catch (Exception e) {
							error("Could not serialize: " + data + "! Details: " + e);
							throw e;
						}
					}
					info("~" + uri + (text != null ? "(" + text.length() + "b):" : ""));
					long millis_start = System.currentTimeMillis();

					HttpRequestBase req = null;
					String url_with_query;
					switch (method) {
					case REST_GET:
						str_method = "GET";
						url_with_query = url + (data != null && 0 < data.size() ? "?" + URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20") : "");
						info("Rest " + str_method + " > " + url_with_query + (text != null ? " (raw JSON: " + text + ")" : "") + " (port: " + port + ", ssl: " + ssl + ")");
						req = new HttpGet(url_with_query);
						break;
					case REST_PUT:
						str_method = "PUT";
						req = new HttpPut(url);
						info("Rest " + str_method + " > " + url + ", payload: " + text + " (port: " + port + ", ssl: " + ssl + ")");
						break;
					case REST_POST:
						str_method = "POST";
						req = new HttpPost(url);
						info("Rest " + str_method + " > " + url + ", payload: " + text + " (port: " + port + ", ssl: " + ssl + ")");
						break;
					case REST_DELETE:
						str_method = "DELETE";
						url_with_query = url + (data != null && 0 < data.size() ? "?" + URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20") : "");
						info("Rest " + str_method + " > " + url_with_query + " (raw: " + text + ")" + " (port: " + port + ", ssl: " + ssl + ")");
						req = new HttpDelete(url_with_query);
						break;
					default:
						throw new Exception("Unsupported REST method - '" + method + "'!");
					}
					// HttpPut httpPut = new HttpPut(url);
					req.setConfig(RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(5 * 60 * 1000).setConnectionRequestTimeout(60 * 1000).setStaleConnectionCheckEnabled(true).build());
					// if (AccSyn._client_api_key != null)
					// data.put("api_key", AccSyn._client_api_key);
					if (headers == null)
						headers = new HashMap<String, String>();
					headers.put("X-Accsyn-Device", "Installer " + VERSION + " @ " + getOS());
					for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext();) {
						String key = iterator.next();
						req.setHeader(key, headers.get(key));
					}
					// f (auth != null)
					// req.setHeader(HttpHeaders.AUTHORIZATION, auth);
					req.addHeader("Accept", "application/json");
					req.addHeader("Content-Type", "application/json");
					StringEntity entity;
					switch (method) {
					case REST_GET:
						break;
					case REST_PUT:
						HttpPut httpPut = (HttpPut) req;
						entity = new StringEntity(text, "UTF-8");
						entity.setContentType("application/json");
						httpPut.setEntity(entity);
						break;
					case REST_POST:
						HttpPost httpPost = (HttpPost) req;
						entity = new StringEntity(text, "UTF-8");
						entity.setContentType("application/json");
						httpPost.setEntity(entity);
						break;
					case REST_DELETE:
						break;
					}
					ResponseHandler handler = new BasicResponseHandler();
					String response;

					try {
						iterations += 1;
						response = httpClient.execute(req, handler);
						info("Rest response: " + response);
						if (isDEV())
							System.out.print(response.length() + "b:" + (System.currentTimeMillis() - millis_start) + "ms~");
						JSONObject retval = null;
						try {
							retval = (JSONObject) (new JSONParser()).parse(response);
						} catch (org.json.simple.parser.ParseException e1) {
							retval = new JSONObject();
							retval.put("message", "Could not parse JSON! Details: " + e1);
							if (isDEV())
								info("Could not parse REST response JSON: '" + response + "'");
							return retval;
						}
						if (retval.containsKey("exception")) {
							if (!isDEV()) {
								retval.put("message", retval.get("exception"));
							} else {
								info((String) retval.get("exception"));
								retval.put("message", retval.get("message"));
							}
						}
						// Deserialize objects that cannot be serialized and return
						if (!headers.containsKey("ASProxy")) {
							JSONObject result = (JSONObject) deSerializeRESTResponse(retval)[0];
							return result;
						} else
							return retval;
					} finally {
					}
				} catch (Exception e1) {
					error("Could not fulfill REST " + str_method + " call to " + hostname + ":" + port + "/" + uri + "! Details:");
					error(e1);
					retries -= 1;
					if (retries == 0) {
						error("Giving up REST " + str_method + "!");
						throw e1;
					} else {
						if (iterations == 1 && false) {
							// Possible cert expired, download
							error("Possible expired or invalid cert causing exception (downloading and retrying in 2s)!");
						} else {
							error("Rest " + str_method + " failed (retrying in 2s)!");
						}
						Thread.sleep(2000);
					}
				} finally {
					if (httpClient != null)
						httpClient.close();
				}
			}
		} catch (Exception e) {
			error("An error occured during REST " + str_method + " " + text + " to " + url + "! Details: ");
			error(e);
			JSONObject retval = new JSONObject();
			retval.put("message", "An error occured when talking to accsyn backen server! Details: " + e);
			return retval;
		}
		return null;
	}

	// Daemon 
	
	public static String detectDaemonUser() {
		try {
			if (isWindows()) {
				Object[] retval = executeCatchOutput("sc", new String[] {"qc", "AccsynDaemon"});
				String output = (String)retval[1];
				if ((Integer)retval[0] == 0) {
					/* Expect:
					[SC] QueryServiceConfig SUCCESS

					SERVICE_NAME: accsyndaemon
					        TYPE               : 10  WIN32_OWN_PROCESS
					        START_TYPE         : 2   AUTO_START
					        ERROR_CONTROL      : 1   NORMAL
					        BINARY_PATH_NAME   : "C:\Program Files\Accsyn\accsyndaemon.exe"
					        LOAD_ORDER_GROUP   :
					        TAG                : 0
					        DISPLAY_NAME       : AccsynDaemon
					        DEPENDENCIES       :
					        SERVICE_START_NAME : .\Filmgate
					*/
					int idx = output.indexOf("SERVICE_START_NAME");
					if (0<idx) {
						int idx_value = output.indexOf(":", idx);
						if (0<idx_value) {
							String result = output.substring(idx_value+1, Math.max(output.indexOf("\n", idx_value), output.length()));
							if (result.startsWith(".\\"))
								result = result.substring(2);
							result = result.trim().replace("\n", "").replace("\t", "");
							if (!result.equals("LocalSystem"))
								return result;
						} else
							warning("detectDaemonUser; No : after 'SERVICE_START_NAME' entry! Output: "+output);
					} else
						warning("detectDaemonUser; No 'SERVICE_START_NAME' entry! Output: "+output);
				} else {
					warning("detectDaemonUser; Could not query Windows service! Details: "+output);
				}
			} else if (isMac()) {
				String p = "/Library/LaunchDaemons/com.accsyn.daemon.plist";
				File f = new File(p);
				if (f.exists() && f.canRead()) {
					String contents = readFile(p);
					int idx = contents.toLowerCase().indexOf("<key>username</key>");
					if (0<idx) {
						int idx_value = contents.indexOf("<string>", idx);
						if (0<idx_value) {
							return contents.substring(idx_value+8, contents.indexOf("</string>", idx_value));
						} else
							warning("detectDaemonUser; No <string> after Username entry! Output: "+contents);
					} else
						warning("detectDaemonUser; No Username entry! Output: "+contents);
				} 
			} else {
				// Assume Linux/*NIX
				String p = "/etc/init.d/accsyndaemon";
				File f = new File(p);
				if (f.exists() && f.canRead()) {
					String contents = readFile(p);
					int idx = contents.indexOf("USERNAME");
					if (0<idx) {
						int idx_value = contents.indexOf("=", idx);
						if (0<idx_value) {
							return contents.substring(idx_value+1, contents.indexOf("\n", idx_value));
						} else
							warning("detectDaemonUser; No = after USERNAME entry @ "+p+"!");
					} else
						warning("detectDaemonUser; No USERNAME entry @ "+p+"!");
				}
			}
		} catch (Exception e) {
			warning("Could not detect daemon user, details: "+e.toString());
			warning(e);
		}
		return null;
	}
	
	public static String detectDaemonUmask() {
		try {
			if (isWindows()) {
				return null;
			} else if (isMac()) {
				String p = "/Library/LaunchDaemons/com.accsyn.daemon.plist";
				File f = new File(p);
				if (f.exists() && f.canRead()) {
					String contents = readFile(p);
					int idx = contents.toLowerCase().indexOf("<key>umask</key>");
					if (0<idx) {
						int idx_value = contents.indexOf("<integer>", idx);
						if (0<idx_value) {
							return contents.substring(idx_value+8, contents.indexOf("</integer>", idx_value));
						} else
							warning("detectDaemonUmask; No <integer> after Umask entry! Output: "+contents);
					} else
						warning("detectDaemonUmask; No Umask entry! Output: "+contents);
				} 
			} else {
				// Assume Linux/*NIX
				String p = "/etc/init.d/accsyndaemon";
				File f = new File(p);
				if (f.exists() && f.canRead()) {
					String contents = readFile(p);
					int idx = contents.indexOf("UMASK");
					if (0<idx) {
						int idx_value = contents.indexOf("=", idx);
						if (0<idx_value) {
							return contents.substring(idx_value+1, contents.indexOf("\n", idx_value));
						} else
							warning("detectDaemonUmask; No = after UMASK entry @ "+p+"!");
					} else
						warning("detectDaemonUmask; No UMASK entry @ "+p+"!");
				}
			}
		} catch (Exception e) {
			warning("Could not detect daemon umask, details: "+e.toString());
			warning(e);
		}
		return null;
	}
	
	public static boolean validateSystemUser(String username) {
		if (isWindows()) {
			Object[] retval = executeCatchOutput("net", new String[] {"user", username});
			return (Integer)retval[0] == 0;
		} else {
			Object[] retval = executeCatchOutput("id", new String[] {username});
			return (Integer)retval[0] == 0;
		}
	}

	public static boolean updateDaemon(String username, String password, String umask) {
		try {
			if (isWindows()) {
				if (username == null || username.length()==0)
					return true;
				if (username.indexOf("\\") == -1)
					username = ".\\"+username;
				Object[] retval = executeCatchOutput("sc", new String[] {"config", "AccsynDaemon", "obj=", username, "password=", password, "type=", "own"});
				//else
				//retval = executeCatchOutput("sc", new String[] {"config", "AccsynDaemon", "obj=", ".\\LocalSystem", "type=", "own"});
				info("Output from configuring Windows service user: "+(String)retval[1]);
				if ((Integer)retval[0] == 0) {
					info("writeDaemonUser; Updated Windows service with username/umask");
					return true;
				} else {
					warning("writeDaemonUser; Could not update windows service, details: "+(String)retval[1]+"!");
					return false;
				}
			} else if (isMac()) {
				String p = "/Library/LaunchDaemons/com.accsyn.daemon.plist";
				info("writeDaemonUser; Updating Mac OS launchd with username/umask @ '"+p+"'");
				// install4j overwrites plist, append user
				
				File f = new File(p);
				if (f.exists() && f.canRead() && f.canWrite()) {
					String result = "";
					String[] parts = readFile(p).split("\n");
					for (int idx = 0; idx < parts.length; idx++) {
						String line = parts[idx];
						if (-1<line.indexOf("</dict>")) {
							if (username != null && 0<username.length()) {
								result += "    <key>UserName</key>\n";
								result += "    <string>"+username+"</string>\n";
							}
							if (umask != null && 0<umask.length()) {
								result += "    <key>Umask</key>\n";
								result += "    <integer>"+Integer.parseInt(umask,8)+"</integer>\n";
							}
						}
						result += line + "\n";
					}
					writeFile(p, result);
					info("writeDaemonUser; Successfully updated Mac OS launchd configuration");
				} else
					warning("writeDaemonUser; Cannot access: "+p+"!");
				
			} else {
				String p = "/etc/init.d/accsyndaemon";
				File f = new File(p);
				if (f.exists() && f.canRead()) {
					String[] parts = readFile(p).split("\n");
					String result = "";
					boolean within_case_entry = false;
					for (int idx = 0; idx < parts.length; idx++) {
						String line = parts[idx];
						String post_content = null;
						if (-1<line.indexOf("INSTALL4J_JAVA_PREFIX=\"")) {
							post_content = "";
							if (username != null && 0<username.length())
								post_content += "USERNAME="+username+"\n";
							if (umask != null && 0<umask.length())
								post_content += "UMASK="+umask+"\n";
						} else if (-1<line.indexOf("start)") || -1<line.indexOf("start-launchd)") || -1<line.indexOf("echo \"Restarting accsyndaemon\"") || -1<line.indexOf("run)") || -1<line.indexOf("run-redirect)")) {
							within_case_entry = true;
						} else if (within_case_entry) {
							String p_tmp = "/tmp";
							if (System.getenv("TMPDIR") != null)
								p_tmp = System.getenv("TMPDIR");
							else if (System.getenv("TMPPATH") != null)
								p_tmp = System.getenv("TMPPATH");
							String p_wrapper_script_parent = p_tmp+"/.accsyn";
							String p_wrapper_script = p_wrapper_script_parent  + "/daemon-wrapper.sh";
							if (-1<line.indexOf("$INSTALL4J_JAVA_PREFIX")) {
								if (username != null && 0<username.length()) {
									result += "        if [ ! -e "+p_wrapper_script_parent+" ]; then mkdir -p "+p_wrapper_script_parent+"; fi; \n";
									if (umask != null && 0<umask.length())
										result += "        echo \"umask ${UMASK}\" >  "+p_wrapper_script+"\n";
									line = "        echo \""+line.replace("\"", "\\\"")+"\" >> "+p_wrapper_script;
									post_content = 
											"        chmod 755 "+p_wrapper_script+"\n"+
											"        su - ${USERNAME} -c "+p_wrapper_script+" > /dev/null 2>&1 \n";
								} else if (umask != null && 0<umask.length()) {
									result += "        umask ${UMASK}\n";
								}
							} else if (-1<line.indexOf(";;")) {
								within_case_entry = false;
							}
						}
						result += line + "\n";
						if (post_content != null)
							result += post_content;
					}
					writeFile(p, result);
					info("writeDaemonUser; Updated Linux init script with username/umask");
				} else {
					warning("writeDaemonUser; Cannot access init script: "+p+", looking for systemd!"); 
					// Systemd?
					p = "/etc/systemd/system/accsyndaemon.service";
					f = new File(p);
					if (f.exists() && f.canRead()) {
						String[] parts = readFile(p).split("\n");
						String result = "";
						for (int idx = 0; idx < parts.length; idx++) {
							String line = parts[idx];
							if (-1<line.indexOf("ExecStart=")) {
								if (username != null && 0<username.length())
									line += "\nUser="+username;
								if (umask != null && 0<umask.length())
									line += "\nUmask="+umask;
							}
							result += line + "\n";
						}
						writeFile(p, result);
						executeCatchOutput("systemctl", new String[] {"daemon-reload"});
						info("writeDaemonUser; Updated Linux systemd script with username/umask");
					} else
						warning("writeDaemonUser; Cannot access systemd script: "+p+"!"); 
				}
			}
			/*
			ArrayList<String> chown_directories = new ArrayList<>();
			if (username != null && 0<username.length()) {
				chown_directories.add(getConfDir());
				chown_directories.add(getLogDir());
				if (!ASICommon.isWindows()) {
					chown_directories.add("/var/lib/accsyn");
					chown_directories.add("/tmp/.accsyn");
				}
			}
			if (0<chown_directories.size()) {
				for (int idx = 0; idx < chown_directories.size(); idx++) {
					String path = chown_directories.get(idx);
					File f_dir = new File(path);
					if (!f_dir.exists())
						info("Result of creating directory '"+path+"': "+f_dir.mkdirs());
					if (isWindows()) {
						// >CACLS C:\Users\Filmgate\Music /e /p henrik.norin:f
						Object[] retval = executeCatchOutput("cacls", new String[] {path, "/e", "/p", username+":f"});
						String output = (String)retval[1];
						if ((Integer)retval[0] == 0) {
							info("Granted windows full access permission for "+username+" @ '"+path+"'");
						} else
							warning("Could not grant access on '"+path+"'! Details: "+output);
					} else {
						Object[] retval = executeCatchOutput("sudo", new String[] {"chown", "-Rv", username, path});
						String output = (String)retval[1];
						if ((Integer)retval[0] == 0) {
							info("Changed *NIX  owner to "+username+" @ '"+path+"'");
						} else
							warning("Could not change ownership on '"+path+"'! Details: "+output);
					}
				}
			}*/
		} catch (Exception e) {
			warning("writeDaemonUser; Could not write daemon user/umask, details: "+e.toString());
			warning(e);
		}
		return false;
	}
}
