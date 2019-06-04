package de.foellix.aql.pim;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;

public class CaseConstructor {
	private final List<Answer> explicitCases;
	private final List<Answer> implicitCases;
	private final List<Intentsource> intentSources;
	private final List<Intentsink> intentSinks;

	public CaseConstructor(List<Answer> input) {
		this.explicitCases = new ArrayList<>();
		this.implicitCases = new ArrayList<>();
		this.intentSources = new ArrayList<>();
		this.intentSinks = new ArrayList<>();
		for (final Answer answer : input) {
			if (answer.getIntentsources() != null) {
				for (final Intentsource is : answer.getIntentsources().getIntentsource()) {
					this.intentSources.add(is);
				}
			}
			if (answer.getIntentsinks() != null) {
				for (final Intentsink is : answer.getIntentsinks().getIntentsink()) {
					this.intentSinks.add(is);
				}
			}
		}
		constructCases();
	}

	private void constructCases() {
		for (final Intentsource source : this.intentSources) {
			for (final Intentsink sink : this.intentSinks) {
				final Answer answer = new Answer();
				final Intentsources intentSources = new Intentsources();
				intentSources.getIntentsource().add(source);
				answer.setIntentsources(intentSources);
				final Intentsinks intentSinks = new Intentsinks();
				intentSinks.getIntentsink().add(sink);
				answer.setIntentsinks(intentSinks);

				if (source.getTarget().getReference() == null && sink.getTarget().getReference() == null) {
					// Implicit case
					this.implicitCases.add(answer);
				} else if (source.getTarget().getReference() != null && sink.getTarget().getReference() != null) {
					// Explicit case
					this.explicitCases.add(answer);
				}
			}
		}
	}

	public List<Answer> getExplicitCases() {
		return this.explicitCases;
	}

	public List<Answer> getImplicitCases() {
		return this.implicitCases;
	}
}
