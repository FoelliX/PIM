package de.foellix.aql.pim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;

class AllTests {
	@BeforeAll
	public static void before() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
		EmulatorHandler.start();
	}

	@AfterAll
	public static void after() {
		EmulatorHandler.stop();
	}

	@Test
	void interApp1Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/InterApp1/InterApp1a.xml"));
		final Answer input2 = AnswerHandler.parseXML(new File("test/InterApp1/InterApp1b.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);
		input.add(input2);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/InterApp1/result.xml"));

			assertEquals(26, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void intraAppExplicit1Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/IntraAppExplicit1/IntraAppExplicit1a.xml"));
		final Answer input2 = AnswerHandler.parseXML(new File("test/IntraAppExplicit1/IntraAppExplicit1b.xml"));
		final Answer input3 = AnswerHandler.parseXML(new File("test/IntraAppExplicit1/IntraAppExplicit1c.xml"));
		final Answer input4 = AnswerHandler.parseXML(new File("test/IntraAppExplicit1/IntraAppExplicit1d.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);
		input.add(input2);
		input.add(input3);
		input.add(input4);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/IntraAppExplicit1/result.xml"));

			assertEquals(33, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void intraAppImplicit1Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/IntraAppImplicit1/IntraAppImplicit1a.xml"));
		final Answer input2 = AnswerHandler.parseXML(new File("test/IntraAppImplicit1/IntraAppImplicit1b.xml"));
		final Answer input3 = AnswerHandler.parseXML(new File("test/IntraAppImplicit1/IntraAppImplicit1c.xml"));
		final Answer input4 = AnswerHandler.parseXML(new File("test/IntraAppImplicit1/IntraAppImplicit1d.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);
		input.add(input2);
		input.add(input3);
		input.add(input4);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/IntraAppImplicit1/result.xml"));

			assertEquals(46, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void droidBenchCase1Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/DroidBenchCase1/DroidBenchCase1a.xml"));
		final Answer input2 = AnswerHandler.parseXML(new File("test/DroidBenchCase1/DroidBenchCase1b.xml"));
		final Answer input3 = AnswerHandler.parseXML(new File("test/DroidBenchCase1/DroidBenchCase1c.xml"));
		final Answer input4 = AnswerHandler.parseXML(new File("test/DroidBenchCase1/DroidBenchCase1d.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);
		input.add(input2);
		input.add(input3);
		input.add(input4);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/DroidBenchCase1/result.xml"));

			assertEquals(18, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void intentMatching1Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/IntentMatching/IntentMatching1.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/IntentMatching/result1.xml"));

			assertEquals(34, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void intentMatching2Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/IntentMatching/IntentMatching2.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/IntentMatching/result2.xml"));

			assertEquals(61, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	void intentMatching3Test() {
		final Answer input1 = AnswerHandler.parseXML(new File("test/IntentMatching/IntentMatching3.xml"));

		final List<Answer> input = new ArrayList<>();
		input.add(input1);

		boolean noException = true;
		Answer answer = null;
		try {
			final PIM pim = new PIM();
			input.add(pim.start(input));
			answer = pim.buildOutput(input);

			AnswerHandler.createXML(answer, new File("test/IntentMatching/result3.xml"));

			assertEquals(503, answer.getFlows().getFlow().size());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}