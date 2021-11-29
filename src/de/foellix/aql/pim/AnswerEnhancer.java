package de.foellix.aql.pim;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.FileRelocator;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.pim.config.Config;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.Options;
import de.foellix.aql.system.defaulttools.operators.DefaultConnectOperator;

public class AnswerEnhancer {
	private static final String NEEDLE_SET_RESULT = "void setResult(int,android.content.Intent)";
	private static final String NEEDLE_GET_RESULT = "void onActivityResult(int,int,android.content.Intent)";

	private final List<Answer> input;
	private final Scanner sc;
	private final AQLSystem aqlSystem;
	private List<File> relocationFolder;

	public AnswerEnhancer(List<Answer> input) {
		if (Config.getInstance().appRelocationPath != null && !Config.getInstance().appRelocationPath.isEmpty()) {
			this.relocationFolder = new ArrayList<>();
			for (final String folder : Config.getInstance().appRelocationPath.split(",")) {
				this.relocationFolder.add(new File(folder));
			}
		}

		this.input = input;

		this.sc = new Scanner(java.lang.System.in);
		if (java.lang.System.getProperty("os.name").toLowerCase().contains("win")) {
			CLIHelper.evaluateConfig("windows.xml");
		} else {
			CLIHelper.evaluateConfig("linux.xml");
		}

		final Options options = new Options().setTimeout(CLIHelper.evaluateTimeout("5m")); // 5 Minutes
		this.aqlSystem = new AQLSystem(options);
	}

	public void enhance() {
		Log.msg("--- ENHANCING I ---", Log.NORMAL);
		addIntentSources();
	}

	public Answer enhance(Answer answer, List<Answer> explicitCases) {
		Log.msg("--- ENHANCING II ---", Log.NORMAL);
		handleExplicit(answer, explicitCases);

		return answer;
	}

	public Answer enhance(Answer answer) {
		Log.msg("--- ENHANCING III ---", Log.NORMAL);
		addFlows(answer);

		return answer;
	}

	private void addIntentSources() {
		Log.msg("Adding new IntentSources...", Log.NORMAL);
		String lastquery = null;
		Object fa = null;
		int isc = 0;
		final Map<Answer, List<Intentsource>> map = new HashMap<>();
		for (final Answer ia : this.input) {
			map.put(ia, new ArrayList<>());
			if (ia.getIntentsources() != null) {
				for (final Intentsource i : ia.getIntentsources().getIntentsource()) {
					File iFile = new File(i.getReference().getApp().getFile());
					if (!iFile.exists()) {
						if (this.relocationFolder.isEmpty()) {
							this.relocationFolder.add(relocateFile(iFile));
						}
						File foundFile = null;
						for (final File folder : this.relocationFolder) {
							foundFile = FileRelocator.recursivelySearchFile(iFile, folder, true);
							if (foundFile != null) {
								break;
							}
						}
						iFile = foundFile;
					}
					if (iFile == null) {
						Log.error("App (" + i.getReference().getApp().getFile() + ") not found on any relocation path: "
								+ Config.getInstance().appRelocationPath);
						return;
					}
					// Compute connecting flows
					final int logLevelBackup = Log.getLogLevel();
					if (!Config.getInstance().debug) {
						Log.setLogLevel(Log.IMPORTANT);
					}
					final String query = "Flows IN App('" + iFile.getAbsolutePath() + "') ?";
					if (!query.equals(lastquery)) {
						lastquery = query;
						final Collection<Object> collection = this.aqlSystem.queryAndWait(query);
						if (collection != null && collection.iterator().hasNext()) {
							fa = collection.iterator().next();
						} else {
							fa = null;
						}
					}
					Log.setLogLevel(logLevelBackup);

					if (fa instanceof Answer) {
						final Answer fac = (Answer) fa;
						if (fac != null && fac.getFlows() != null && !fac.getFlows().getFlow().isEmpty()) {
							for (final Flow ff : fac.getFlows().getFlow()) {
								final Reference f = Helper.getFrom(ff.getReference());
								if (EqualsHelper.equals(i.getReference().getApp(), f.getApp())
										&& i.getReference().getClassname().equals(f.getClassname())) {
									final Intentsource newIS = new Intentsource();
									newIS.setTarget(i.getTarget());
									newIS.setReference(f);
									map.get(ia).add(newIS);
									Log.msg(++isc + ")\n" + Helper.toString(newIS) + "\n", Log.DEBUG);
								}
							}
						}
					}
				}
			}
		}

		for (final Answer ia : this.input) {
			if (ia.getIntentsources() != null) {
				ia.getIntentsources().getIntentsource().addAll(map.get(ia));
			}
		}

		Log.msg("done", Log.NORMAL);
	}

	private void addFlows(Answer answer) {
		Log.msg("Adding accumulated flows...", Log.NORMAL);
		final List<Flow> newFlows = new ArrayList<>();
		if (answer.getFlows() != null) {
			int counter = 0;
			for (final Flow fp : answer.getFlows().getFlow()) {
				counter++;
				if (answer.getFlows().getFlow().size() > 1000) {
					Log.msg("Status: " + counter + " / " + answer.getFlows().getFlow().size(), Log.NORMAL);
				}
				for (Answer std : this.input) {
					std = new DefaultConnectOperator(DefaultConnectOperator.CONNECT_MODE_INTRA_APP).connect(std, std);
					if (std.getFlows() != null) {
						for (final Flow fs : std.getFlows().getFlow()) {
							final Reference fpTo = Helper.getTo(fp.getReference());
							final Reference fpFrom = Helper.getFrom(fp.getReference());
							final Reference fsFrom = Helper.getFrom(fs.getReference());
							final Reference fsTo = Helper.getTo(fs.getReference());
							if (EqualsHelper.equals(fpTo, fsFrom)
									&& fsTo.getStatement().getStatementfull().contains(NEEDLE_SET_RESULT)) {
								for (final Answer std1 : this.input) {
									if (std1.getFlows() != null) {
										for (final Flow p : std1.getFlows().getFlow()) {
											final Reference pFrom = Helper.getFrom(p.getReference());
											if (pFrom.getMethod().contains(NEEDLE_GET_RESULT)) {
												if (EqualsHelper.equals(fpFrom.getApp(), pFrom.getApp())
														&& fpFrom.getClassname().equals(pFrom.getClassname())) {
													final Flow newF = new Flow();
													final Reference from = Helper.copy(fsTo);
													from.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
													newF.getReference().add(from);
													final Reference to = Helper.copy(pFrom);
													to.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
													newF.getReference().add(to);

													boolean add = true;
													for (final Flow isIn : newFlows) {
														if (EqualsHelper.equals(isIn, newF)) {
															add = false;
															break;
														}
													}
													if (add) {
														newFlows.add(newF);
														Log.msg(newFlows.size() + ")\n" + Helper.toString(newF) + "\n",
																Log.DEBUG);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (answer.getFlows() == null) {
			answer.setFlows(new Flows());
		}
		answer.getFlows().getFlow().addAll(newFlows);

		Log.msg("done", Log.NORMAL);
	}

	private void handleExplicit(Answer answer, List<Answer> explicitCases) {
		Log.msg("Adding explicit flows...", Log.NORMAL);

		int counter = 0;
		for (final Answer task : explicitCases) {
			counter++;
			if (explicitCases.size() > 1000) {
				Log.msg("Status: " + counter + " / " + explicitCases.size(), Log.NORMAL);
			}
			final Intentsource source = task.getIntentsources().getIntentsource().iterator().next();
			final Intentsink sink = task.getIntentsinks().getIntentsink().iterator().next();
			if (source.getTarget().getReference().getClassname()
					.equals(sink.getTarget().getReference().getClassname())) {
				final Flow newF = new Flow();
				final Reference from = Helper.copy(sink.getReference());
				from.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
				newF.getReference().add(from);
				final Reference to = Helper.copy(source.getReference());
				to.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
				newF.getReference().add(to);
				if (answer.getFlows() == null) {
					answer.setFlows(new Flows());
				}
				answer.getFlows().getFlow().add(newF);
				Log.msg(answer.getFlows().getFlow().size() + ")\n" + Helper.toString(newF) + "\n", Log.DEBUG);
			}
		}

		Log.msg("done", Log.NORMAL);
	}

	private File relocateFile(File file) {
		File returnFile = null;
		while (returnFile == null || !returnFile.isDirectory()) {
			Log.msg(file.getAbsolutePath() + " could not be found.\nPlease enter the path containing it: ", Log.NORMAL);
			returnFile = new File(this.sc.nextLine().toString().replace("\\", "/"));
		}
		return returnFile;
	}
}
