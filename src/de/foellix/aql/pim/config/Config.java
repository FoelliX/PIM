package de.foellix.aql.pim.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import de.foellix.aql.Log;

public class Config {
	private static final String SDK_PATH = "sdkPath";
	private static final String DEVICE_NAME = "deviceName";
	private static final String REBOOT_ALLOWED = "rebootAllowed";
	private static final String APP_RELOCATION_PATH = "appRelocationPath";

	public String deviceName;
	public boolean rebootAllowed;
	public String sdkPath;
	public String adbPath;
	public String emuPath;
	public String appRelocationPath;
	public File toolFile;

	private static Config instance = new Config();

	private Config() {
		load();
	}

	public static Config getInstance() {
		return instance;
	}

	void load() {
		final Properties prop = new Properties();
		try {
			final FileInputStream in = new FileInputStream("config.properties");
			prop.load(in);
			in.close();

			this.sdkPath = prop.getProperty(SDK_PATH);
			this.adbPath = this.sdkPath + "/platform-tools/";
			this.emuPath = this.sdkPath + "/emulator/";
			this.toolFile = new File(this.sdkPath, "tools");
			this.deviceName = prop.getProperty(DEVICE_NAME);
			this.rebootAllowed = Boolean.valueOf(prop.getProperty(REBOOT_ALLOWED)).booleanValue();
			this.appRelocationPath = prop.getProperty(APP_RELOCATION_PATH);
		} catch (final Exception e0) {
			Log.msg("--- SETUP ---", Log.NORMAL);
			final Scanner sc = new Scanner(System.in);

			this.sdkPath = prop.getProperty(SDK_PATH);
			while (this.sdkPath == null || this.sdkPath.equals("")) {
				Log.msg("Please enter the abolute path to your Android SDK: ", Log.NORMAL);
				this.sdkPath = sc.next();
			}
			this.adbPath = this.sdkPath + "/platform-tools/";
			this.emuPath = this.sdkPath + "/emulator/";
			this.toolFile = new File(this.sdkPath, "tools");

			final Map<Integer, String> map = new HashMap<>();
			try {
				final Process p = new ProcessBuilder(new String[] { this.emuPath + "emulator", "-list-avds" }).start();
				final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				int i = 0;
				while ((line = br.readLine()) != null) {
					i++;
					map.put(i, line);
					Log.msg(i + ") " + line, Log.NORMAL);
				}
				p.waitFor();
				br.close();
			} catch (final Exception e) {
				Log.error("Error while getting list of AVDs (" + e.getClass().getSimpleName() + "): " + e.getMessage());
			}
			this.deviceName = prop.getProperty(DEVICE_NAME);
			while (this.deviceName == null || this.deviceName.equals("")) {
				Log.msg("Please enter the number of an AVD from above: ", Log.NORMAL);
				this.deviceName = map.get(sc.nextInt());
			}

			sc.close();

			this.rebootAllowed = false;
			prop.setProperty(DEVICE_NAME, this.deviceName);
			prop.setProperty(REBOOT_ALLOWED, Boolean.valueOf(this.rebootAllowed).toString());
			prop.setProperty(SDK_PATH, this.sdkPath);
			prop.setProperty(APP_RELOCATION_PATH, ".");
			try {
				final FileOutputStream out = new FileOutputStream("config.properties");
				prop.store(out, "PIM - Perfect Intent Matcher - Configuration file");
				out.close();
			} catch (final Exception e) {
				Log.error("Error while storing properties (" + e.getClass().getSimpleName() + "): " + e.getMessage());
			}
			Log.msg("", Log.NORMAL);
		}
	}
}
