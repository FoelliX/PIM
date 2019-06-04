package de.foellix.aql.pim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.pim.config.Config;

public class EmulatorHandler {
	private static final String DEFAULT_IP = "127.0.0.1";
	private static final int DEFAULT_PORT = 9090;

	private final String ip;
	private final int port;

	public EmulatorHandler() {
		this.ip = DEFAULT_IP;
		this.port = DEFAULT_PORT;
	}

	public EmulatorHandler(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public static boolean start() {
		return start(false);
	}

	public static boolean start(boolean restart) {
		Log.msg("--- LAUNCH ---", Log.NORMAL);
		try {
			// Start emulator
			Log.msg("Starting " + Config.getInstance().deviceName + "... ", Log.NORMAL);
			Process p;
			boolean start = true;
			String output = "";
			String[] cmd;
			while (!output.replaceAll(" ", "").replaceAll("\n", "").equals("1")) {
				cmd = new String[] { Config.getInstance().adbPath + "adb", "shell", "getprop", "sys.boot_completed" };
				Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd),
						Log.DEBUG_DETAILED);
				p = new ProcessBuilder(cmd).start();
				final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				output = "";
				String line = "";
				while ((line = br.readLine()) != null) {
					output += line;
				}
				p.waitFor();
				br.close();

				if (restart || (start && !output.replaceAll(" ", "").replaceAll("\n", "").equals("1"))) {
					restart = false;
					start = false;
					cmd = new String[] { Config.getInstance().emuPath + "emulator", "-no-window", "-avd",
							Config.getInstance().deviceName };
					Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd),
							Log.DEBUG_DETAILED);
					p = new ProcessBuilder(cmd).directory(Config.getInstance().toolFile).start();
				} else if (start) {
					break;
				}

				Thread.sleep(250);
			}
			Log.msg("done", Log.NORMAL);

			if (appStarted() && start) {
				return true;
			}
			setup();
			if (start) {
				return true;
			}
		} catch (final Exception e) {
			Log.error("Error while using the emulator (" + e.getClass().getSimpleName() + "): " + e.getMessage());
		}

		return false;
	}

	public static void setup() throws Exception {
		// Setup emulator
		Log.msg("Setting up " + Config.getInstance().deviceName + "... ", Log.NORMAL);
		String[] cmd = new String[] { Config.getInstance().adbPath + "adb", "forward", "tcp:9090", "tcp:9090" };
		Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd), Log.DEBUG_DETAILED);
		Process p = new ProcessBuilder(cmd).start();
		p.waitFor();
		cmd = new String[] { Config.getInstance().adbPath + "adb", "install", "-r", "-t", "PIMServer.apk" };
		Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd), Log.DEBUG_DETAILED);
		p = new ProcessBuilder(cmd).start();
		p.waitFor();
		cmd = new String[] { Config.getInstance().adbPath + "adb", "shell", "am", "start", "-n",
				"\"de.foellix.aql.intentmatcher.pimserver/.MainActivity\"" };
		Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd), Log.DEBUG_DETAILED);
		p = new ProcessBuilder(cmd).start();
		p.waitFor();
		while (!appStarted()) {
			Thread.sleep(250);
		}
		Thread.sleep(3000);
		Log.msg("done", Log.NORMAL);
	}

	public static boolean appStarted() throws Exception {
		// Check if the server is up and running
		final String[] cmd = new String[] { Config.getInstance().adbPath + "adb", "shell", "dumpsys", "activity",
				"services", "aql" };
		Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd), Log.DEBUG_DETAILED);
		final Process p = new ProcessBuilder(cmd).start();
		final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String output = "";
		String line = "";
		while ((line = br.readLine()) != null) {
			output += line;
		}
		p.waitFor();
		br.close();

		if (output.contains("de.foellix.aql.intentmatcher.pimserver/.PIMService")) {
			return true;
		} else {
			return false;
		}
	}

	public static void stop() {
		if (Config.getInstance().rebootAllowed) {
			try {
				// Shut down emulator
				Log.msg("Shutting down " + Config.getInstance().deviceName + "... ", Log.NORMAL);
				// Old (kill): final String[] cmd = new String[] { Config.getInstance().adbPath + "adb", "-e", "emu", "kill" };
				final String[] cmd = new String[] { Config.getInstance().adbPath + "adb", "shell", "reboot", "-p" };
				Log.msg("Launching process: " + de.foellix.aql.pim.helper.Helper.arrayToString(cmd),
						Log.DEBUG_DETAILED);
				final Process p = new ProcessBuilder(cmd).start();
				p.waitFor();
				Log.msg("done", Log.NORMAL);
			} catch (final Exception e) {
				Log.error("Error while using the emulator (" + e.getClass().getSimpleName() + "): " + e.getMessage());
			}

			try {
				Thread.sleep(10000);
				start(true);
			} catch (final InterruptedException e1) {
				Log.error("Problems while waiting for emulator to shutdown.");
			}
		} else {
			Log.msg("Setting up device (" + Config.getInstance().deviceName + ") again... ", Log.NORMAL);
			try {
				setup();
			} catch (final Exception e) {
				Log.error("Error while setting up the emulator (" + e.getClass().getSimpleName() + "): "
						+ e.getMessage());
			}
			Log.msg("done", Log.NORMAL);

			start(false);
		}
	}

	public Answer launch(List<Answer> tasks) {
		final boolean stop = !start();

		// Ask emulator
		Log.msg("Generated flows: ", Log.DEBUG);
		final Answer answer = ask(tasks);

		if (stop) {
			stop();
		}

		return answer;
	}

	private Answer ask(List<Answer> tasks) {
		final Answer answer = new Answer();
		Socket socket = null;

		int i = 0;
		int count = 0;
		int tell = 500;
		for (final Answer task : tasks) {
			int restarts = 3;
			boolean done = false;
			i++;
			while (!done && restarts > 0) {
				try {
					tell--;
					if (tasks.size() < 5000 || (tasks.size() >= 5000 && tell == 0)) {
						if (tell <= 0) {
							tell = 500;
						}
						Log.msg("Asking (" + i + "/" + tasks.size() + ")... ", Log.NORMAL);
					}
					Log.msg("\nMatching:\n"
							+ Helper.toString(task.getIntentsinks().getIntentsink().get(0).getTarget(), true) + "<->\n"
							+ Helper.toString(task.getIntentsources().getIntentsource().get(0).getTarget(), true),
							Log.DEBUG_DETAILED);
					Log.msg("Potential flow: "
							+ task.getIntentsinks().getIntentsink().get(0).getReference().getStatement()
									.getStatementfull()
							+ "\n-> " + task.getIntentsources().getIntentsource().get(0).getReference().getStatement()
									.getStatementfull()
							+ "\n", Log.DEBUG_DETAILED);
					socket = new Socket(this.ip, this.port);

					final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					out.writeObject(task);
					if (Boolean.valueOf(in.readLine()).booleanValue()) {
						Log.msg("Flow confirmed!", Log.DEBUG_DETAILED);

						final Flow flow = new Flow();
						final Reference from = task.getIntentsinks().getIntentsink().get(0).getReference();
						from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
						final Reference to = task.getIntentsources().getIntentsource().get(0).getReference();
						to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
						flow.getReference().add(from);
						flow.getReference().add(to);

						if (answer.getFlows() == null) {
							answer.setFlows(new Flows());
						}
						answer.getFlows().getFlow().add(flow);

						count++;
						Log.msg(count + ")\n" + Helper.toString(flow) + "\n", Log.DEBUG);
					}

					done = true;
					if (tasks.size() < 5000 || (tasks.size() >= 5000 && tell == 0)) {
						Log.msg("done", Log.NORMAL);
					}
				} catch (final IOException e) {
					Log.warning("Problems occured while comunicating with the server. Trying to restart the emulator. ("
							+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
					restarts--;

					stop();
				} finally {
					try {
						if (socket != null) {
							socket.close();
						}
					} catch (final IOException e) {
						// do nothing
					}
				}
			}
			if (!done) {
				Log.error(
						"Error while comunicating with the server still exists after restarting it. Could not infer links.");
			}
		}

		return answer;
	}
}
