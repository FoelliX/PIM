package de.foellix.aql.pim;

import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.system.defaulttools.operators.DefaultConnectOperator;
import de.foellix.aql.system.defaulttools.operators.DefaultFilterOperator;
import de.foellix.aql.system.defaulttools.operators.DefaultUnifyOperator;

public class PIM {
	public Answer start(List<Answer> input) {
		final AnswerEnhancer ae = new AnswerEnhancer(input);
		ae.enhance();
		final CaseConstructor cc = new CaseConstructor(input);
		final EmulatorHandler eh = new EmulatorHandler();
		return ae.enhance(ae.enhance(eh.launch(cc.getImplicitCases()), cc.getExplicitCases()));
	}

	public Answer buildOutput(List<Answer> input) {
		Log.msg("--- BUILDING FINAL OUTPUT ---", Log.NORMAL);
		Answer result = input.get(0);
		int counter = 0;
		for (int i = 1; i < input.size(); i++) {
			counter++;
			Log.msg("Status: " + counter + " / " + (input.size() - 1), Log.NORMAL);
			if (input.get(i).getFlows() != null && !input.get(i).getFlows().getFlow().isEmpty()) {
				result = new DefaultConnectOperator(DefaultConnectOperator.CONNECT_MODE_INTRA_APP).connect(result,
						input.get(i));
			} else {
				result = new DefaultUnifyOperator().unify(result, input.get(i));
			}
		}
		result = new DefaultFilterOperator().filter(result);
		Log.msg("done", Log.NORMAL);

		return result;
	}
}
