package edu.stanford.cs276;

/**
 * Implement {@link EditCostModel} interface by assuming assuming
 * that any single edit in the Damerau-Levenshtein distance is equally likely,
 * i.e., having the same probability
 */
public class UniformCostModel implements EditCostModel {
	
	private static final long serialVersionUID = 1L;
	int len = CandidateGenerator.alphabet.length;
	private double uniformEditPrbability = .05;
	
	
	
  
  @Override
  public double editProbability(String original, String R, int distance) {
	  // TODO: Your code here
	  
	/* Since all edits are equally probable, 
	 * the probability of an edited candidate equals 
	 * the probability of one edit to the power of the number of edits 
	 */
	  return Math.log10(Math.pow(uniformEditPrbability,distance)); 
	  
  }
}
