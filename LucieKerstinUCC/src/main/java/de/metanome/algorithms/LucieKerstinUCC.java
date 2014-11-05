package de.metanome.algorithms;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.UniqueColumnCombinationsAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

public class LucieKerstinUCC extends LucieKerstinUCCAlgorithm implements UniqueColumnCombinationsAlgorithm, RelationalInputParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR
	};
	
	@Override
	public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement> conf = new ArrayList<ConfigurationRequirement>();
		conf.add(new ConfigurationRequirementRelationalInput(LucieKerstinUCC.Identifier.INPUT_GENERATOR.name()));
		//conf.add(new ConfigurationRequirementRelationalInput(LucieKerstinUCC.Identifier.INPUT_GENERATOR.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES)); // For IND discovery, the number of inputs is arbitrary
		return conf;
	}

	@Override
	public void setResultReceiver(UniqueColumnCombinationResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (!LucieKerstinUCC.Identifier.INPUT_GENERATOR.name().equals(identifier))
			throw new AlgorithmConfigurationException("Input generator does not match the expected identifier: " + identifier + " (given) but " + LucieKerstinUCC.Identifier.INPUT_GENERATOR.name() + " (expected)");
		this.inputGenerator = values[0];
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		super.execute();
	}

}
