package de.foellix.aql.pim.cli;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.AnsiConsole;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.pim.EmulatorHandler;
import de.foellix.aql.pim.PIM;

public class PIMClient {
	public static void main(String[] args) throws IOException {
		AnsiConsole.systemInstall();
		final String authorStr1 = "Author: " + Properties.info().AUTHOR;
		final String authorStr2 = "(" + Properties.info().AUTHOR_EMAIL + ")";
		final String centerspace0 = "           ".substring(Math.min(11, (Properties.info().VERSION.length() + 3) / 2));
		final String centerspace1 = "           ".substring(Math.min(11, authorStr1.length() / 2));
		final String centerspace2 = "           ".substring(Math.min(11, authorStr2.length() / 2));
		Log.msg(ansi().bold().fg(GREEN)
				.a("  _____ _____ __  __ \r\n" + " |  __ \\_   _|  \\/  |\r\n" + " | |__) || | | \\  / |\r\n"
						+ " |  ___/ | | | |\\/| |\r\n" + " | |    _| |_| |  | |\r\n" + " |_|   |_____|_|  |_|\r\n")
				.reset().a(centerspace0 + "v. " + Properties.info().VERSION + "\r\n\r\n" + centerspace1 + authorStr1
						+ "\r\n" + centerspace2 + authorStr2 + "\r\n\r\n"),
				Log.NORMAL);

		// Set Log-Level
		Log.setLogLevel(Log.NORMAL);

		// Read input
		if (args[0].equals("ask")) {
			final List<Answer> input = new ArrayList<>();
			if (args.length >= 2) {
				args = args[1].split(", ");
			}
			for (final String fileStr : args) {
				final File file = new File(fileStr.replaceAll(",", ""));
				if (file.exists()) {
					input.add(AnswerHandler.parseXML(file));
				}
			}

			// Execute
			final PIM pim = new PIM();
			input.add(pim.start(input));

			// Build output
			final Answer result = pim.buildOutput(input);

			final List<File> files = new ArrayList<>();
			for (final String arg : args) {
				files.add(new File(arg));
			}
			final File storedAnswer = new File(
					"result_" + HashHelper.sha256Hash(Helper.answerFilesAsString(files)) + ".xml");
			AnswerHandler.createXML(result, storedAnswer);
			Log.msg("--- STORING ---", Log.NORMAL);
			Log.msg("Answer stored in: " + storedAnswer.getAbsolutePath(), Log.NORMAL);
		} else if (args[0].equals("start")) {
			EmulatorHandler.start();
		} else if (args[0].equals("stop")) {
			EmulatorHandler.stop();
		}
	}
}
