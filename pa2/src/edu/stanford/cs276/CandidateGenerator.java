package edu.stanford.cs276;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;

public class CandidateGenerator implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;
	private static final int BIGRAM_THRESHOLD = 0;

  /** 
  * Constructor
  * IMPORTANT NOTE: As in the NoisyChannelModel and LanguageModel classes, 
  * we want this class to use the Singleton design pattern.  Therefore, 
  * under normal circumstances, you should not change this constructor to 
  * 'public', and you should not call it from anywhere outside this class.  
  * You can get a handle to a CandidateGenerator object using the static 
  * 'get' method below.  
  */
  private CandidateGenerator() {}

  public static CandidateGenerator get() throws Exception {
    if (cg_ == null) {
      cg_ = new CandidateGenerator();
    }
    return cg_;
  }

  public static final Character[] alphabet = { 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
      'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', ' ', ',' };

  
  public int editDistance(String s1, String s2){
	  
	  // Initiate table
	  int[][] m=new int[s1.length()+1][s2.length()+1];
	  
	  // Initiate all [i,0] and [0,j] entries to 1	
	  for (int i=1;i<=s1.length();i++){
		  m[i][0]=i;
	  }
	  for (int j=1;j<=s2.length();j++){
		  m[0][j]=j;
	  }
	  
	  // Main loop
	  for (int i=1;i<=s1.length();i++){
		  for (int j=1;j<=s2.length();j++){
			  int substitutionCost = 1;
			  int transpositioncost = 1;
			  
			  if (s1.charAt(i-1)==s2.charAt(j-1)) {
				  substitutionCost =0;
			  }
			  
			  if (0<i & i<s1.length() & 0<j & j<s2.length()) {
				  if (s1.charAt(i-1)==s2.charAt(j) & s1.charAt(i)==s2.charAt(j-1) ) {
					  transpositioncost =0;
				  }
			  }
			  
			  int[] costs  = {
							  m[i-1][j]+1,
							  m[i][j-1]+1,
							  m[i-1][j-1] + substitutionCost,
							  m[i-1][j-1] + transpositioncost
					  		 };
			  Arrays.sort(costs);
			  m[i][j]=costs[0];
		  }
	  }
	  
	  //printMatrix(m);
	  return m[s1.length()][s2.length()];
  }
  
  
  // Generate all candidates for the target query
  public Set<String> getCandidates(String query, LanguageModel lm) throws Exception {
    /*
     * Your code here
     */
    return getCandidatesByBigram(query, lm);
  }
  
  
  public Set<String> getCandidatesByDistance(String query, LanguageModel lm) throws Exception {
	    Set<String> candidates = new HashSet<String>();
	    /*
	     * Your code here
	     */
	    
	    String[] tokens = query.trim().split("\\s+");
	    
	    
	    
	    return candidates;
	  }
  
  public Set<String> getCandidatesByBigram(String query, LanguageModel lm) throws Exception {
	    Set<String> candidates = new HashSet<String>();
	    /*
	     * Your code here
	     */
	    
	    query = "I am sam"; // this is just for experimenting
	    
	    String[] tokens = query.trim().split("\\s+"); // Tokenize the query
	    
	    // Select each two tokens
	    for (int i=0;i<tokens.length-1;i++){
	    	String tok1 = tokens[i];
	    	String tok2 = tokens[i+1];
	    	Dictionary unigramCounts = lm.bigram.get(tok2);
	    	
	    	/* 
	    	 * Get the unigram counts of tok2 if they exist
	    	 * (remember they are supposed to be sorted by highest value)
	    	 */
	    	
	    	List<HashMap.Entry<String, Integer>> entries;
	    	try {
	    		entries = unigramCounts.getEntries();
	    	} catch (NullPointerException e) {
	    		entries = new LinkedList<HashMap.Entry<String, Integer>>();
	    	}
	    	
	    	/*
	    	 * For each token in the counts, 
	    	 * if its frequency before tok2 is greater than the bigram threshold, 
	    	 * and the edit distance between it and tok1 is less than the edit threshold,
	    	 * create a new candidate in which it replaces the actual tok1 
	    	 */
	    	for (HashMap.Entry<String, Integer> entry : entries){
	    		if (entry.getValue() > BIGRAM_THRESHOLD & editDistance(entry.getKey(), tok1)<3){
	    			candidates.add(buildCandidate(tokens,i,entry.getKey()));
	    		}
	    	}
	    }
	    
	    return candidates;
	  }
  
  
	/**
	 * @param tokens	array of tokens, basis of candidate
	 * @param index		index in which a word will be replaced
	 * @param token		the token that will be replaced in tokens	
	 * @return	the candidate which is tokens with token replaced in tokens[index]
	 */
  public String buildCandidate(String[] tokens, int index, String token) {
	  tokens[index]=token;
	  StringBuilder builder = new StringBuilder();
	  for (String string : tokens) {
		    if (builder.length() > 0) {
		        builder.append(" ");
		    }
		    builder.append(string);
		}

	  return builder.toString();
  }
  
  public static void printMatrix(int[][] m){
	  for (int i = 0; i < m.length; i++) {
		    for (int j = 0; j < m[i].length; j++) {
		        System.out.print(m[i][j] + " ");
		    }
		    System.out.println();
		}
  }

}
