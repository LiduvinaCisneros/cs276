package edu.stanford.cs276;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericDeclaration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

public class CandidateGenerator implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;
	
	private static LanguageModel languageModel;
	
	private static final int EDIT_THRESHOLD = 2; // Maximum edit distance to be considered as candidate
	private static final int BIGRAM_FREQ_THRESHOLD = 1; // Minimum bigram frequency to be considered as candidate
	private static final int UNIGRAM_CANDS_COUNT = 1; // Minimum bigram frequency to be considered as candidate
	private static final String DISTANCE_2_MARKER = "DISTANCE_2_MARKER";
  /** 
  * Constructor
  * IMPORTANT NOTE: As in the NoisyChannelModel and LanguageModel classes, 
  * we want this class to use the Singleton design pattern.  Therefore, 
  * under normal circumstances, you should not change this constructor to 
  * 'public', and you should not call it from anywhere outside this class.  
  * You can get a handle to a CandidateGenerator object using the static 
  * 'get' method below.  
  */
  private CandidateGenerator(){
	  languageModel = RunCorrector.languageModel;
  }

  public static CandidateGenerator get() throws Exception {
    if (cg_ == null) {
      cg_ = new CandidateGenerator();
    }
    return cg_;
  }

  public static final Character[] alphabet = { 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
      'u', 'v', 'w', 'x', 'y', 'z'};


  public static final Character[] nonLetters = { '0', '1', '2', '3', '4', '5', '6', '7',
	      '8', '9', ' ', ',' };

  
  public static final Character[] letters  = { 'a', 'b', 'c', 'd', 'e', 'f',
	      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
	      'u', 'v', 'w', 'x', 'y', 'z'};

  
  /* Generate all candidates for the target query 
   * Strategy idea:
   * For each token tok, generate all the candidates from:
   * 1. bigrams that pass BIGRAM_FREQ_THRESHOLD and are within EDIT_THRESHOLD from tok
   * 2. the UNIGRAM_CANDS_COUNT most frequent unigrams that are within EDIT_THRESHOLD from tok*/
  
  
  /*
  * @author Omer Korat
  * Bigram Strategy:
	  * For each pair of tokens tok1 and tok2,
	  * generate all candidates with one or two of tok1 and tok2 replaced by any
	  * token tok3 in the dictionary such that the bigram frequency of tok3 
	  * before the word following tok1/tok2 is greater than BIGRAM_FREQ_THRESHOLD,
	  * and the overall edit distance between the candidate and query is no greater than EDIT_THRESHOLD 
	  * TODO: Known problem: the corrections are selected based on bigrams before corrections are generated;
	  * therefore, if two consecutive words are misspelled, then the bigram counts for the first word
	  * is based on a misspelled word. the solution would be generating connections for the first word
	  * based on the second word.
  * Unigram Strategy:
  	  * select the most likely unigrams as candidates. Not yet implemented. 
  * TODO: handle mistyped spaces within word
  */
  public Set<String> getCandidates(String query) throws Exception {
	    Set<String> candidates = new HashSet<String>();
	    /*
	     * Your code here
	     */
	    
	    //query = "I am sam"; // this is just for experimenting
	    
	    // Tokenize the query and create and create array of indices
	    String[] tokens = query.trim().split("\\s+"); 
	    int[] indices = new int[tokens.length];
	    for (int i=0; i<tokens.length;i++){
	    	indices[i] = i;
	    }
	    
	    
	    // Maps misspellings to edit distance to sets of corrections
	    HashMap<String,HashMap<Integer,HashSet<String>>> tokensToEditDistanceToCorrections = 
	    		new HashMap<String,HashMap<Integer,HashSet<String>>>();
	    
	    // Augment this dictionary by choosing most likely bigrams
	    tokensToEditDistanceToCorrections =
	    		findAllCorrectionsByBigram(tokens);
	    
	    // Augment this dictionary by choosing most likely unigrams
	    /* TODO: implement this */
	    
	    
	    // Iteate over the Cartesian product of the indices of the tokens array (matching every two words)
	    Iterator<int[]> pairsIterator= cartesianProductIter(indices);
	    while (pairsIterator.hasNext()){
	    	int[] pair = pairsIterator.next();
	    	int i = pair[0]; int j = pair[1];
	    	Map<Integer,HashSet<String>> indexToCorrections = new HashMap<Integer,HashSet<String>>();
	    	
	    	if (i==j){
	    		// If a word is matched with itself, get all existing edits of this word
	    		HashMap<Integer,HashSet<String>> editDistanceToCorrections = 
	    				tokensToEditDistanceToCorrections.get(tokens[i]);
	    		if (editDistanceToCorrections != null) {
	    			indexToCorrections.put(i,getAllValues(editDistanceToCorrections));
	    		} else {
	    			indexToCorrections = null;
	    		}
	    	//TODO: implement this.
	    	} else if (Math.abs(i-j)==1) {
	    		// If the words are consecutive, calculate the bigrams of the one based on the choice of the other
	    	} else {
	    		// Else, get all the distance 1 corrections of both words
	    		
	    		HashMap<Integer,HashSet<String>> editDistanceToCorrections1 = 
	    				tokensToEditDistanceToCorrections.get(tokens[i]);
	    		HashMap<Integer,HashSet<String>> editDistanceToCorrections2 = 
	    				tokensToEditDistanceToCorrections.get(tokens[j]);
	    		
	    		HashSet<String> corrections1;
	    		HashSet<String> corrections2;
	    		if (editDistanceToCorrections1!=null){
	    			corrections1 = editDistanceToCorrections1.get(1);
	    			indexToCorrections.put(i, corrections1);
	    		}
	    		if (editDistanceToCorrections2!=null){
	    			corrections2 = editDistanceToCorrections2.get(1);
	    			indexToCorrections.put(j, corrections2);
	    		}
	    		
	    	}
	    	
	    	// If any corrections were found
	    	if (indexToCorrections!=null){
	    		
	    		// Get a list of all pairs of (index, correction set)
	    		ArrayList<Map.Entry<Integer,HashSet<String>>> entries = 
	    				new ArrayList<Map.Entry<Integer,HashSet<String>>>(indexToCorrections.entrySet());
	    		// The only two meaningful sizes are 1 and 2, because this is Cartesian product so we only care about pairs
	    		if (entries.size()==1){
	    			// Get the single entry:
	    			Map.Entry<Integer,HashSet<String>> entry = entries.get(0); 
    				for (String correction : entry.getValue()){
    					
    					// The crucial line: build a candidate from each correction
    					candidates.add(buildCandidate(tokens, entry.getKey(), correction));
    				}
	    				
	    		} else if (entries.size()==2) {
	    			/* This is the Cartesian step: if we have two lists of corrections,
	    			 * for each possible combination create a candidate
	    			 */
	    			Map.Entry<Integer,HashSet<String>> entry1 = entries.get(0); 
	    			for (String correction1 : entry1.getValue()){
	    				// Build candidate with first correction
	    				String candidate = buildCandidate(tokens, entry1.getKey(), correction1);
	    				Map.Entry<Integer,HashSet<String>> entry2 = entries.get(1);
	    				for (String correction2 : entry2.getValue()){
	    					// Integrate it with second correction and add to candidate set
	    					candidates.add(buildCandidate(candidate.split("\\s"), entry2.getKey(), correction2));
	    				}
    				}
	    		}
	    	}
	    }
	    
	    return candidates;
	    
	  }
  
  /**
 * @author Omer Korat
 * For an array of tokens, finds all corrections to each token which pass the bigram count threshold
 * and within the maximum edit distance. Organizes corrections in a dictionary from 
 * mispelled token to edit distance to list of corrections within that distance 
 * (these corrections are all possible bigrams with the next word, and are within the allowed edit distance)  
 * @param tokens	an array of tokens
 * @return a dictionary as described above 
 * TODO: combine backward and forward bigrams; right now only backward bigrams are taken into account.
 */
public HashMap<String,HashMap<Integer,HashSet<String>>> findAllCorrectionsByBigram(String[] tokens) {
	//System.out.println("========================");
    HashMap<String,HashMap<Integer,HashSet<String>>> tokensToEditDistanceToCorrections = 
    		new HashMap<String,HashMap<Integer,HashSet<String>>>();
    
    /* Select each two adjacent tokens: 
     * Note: right now our bigram model only has data about the tokens that follow some token.
     * Therefore we cannot take the last token of a sentence into account. This can be fixed by 
     * adding an additional bigram model which looks forward.*/
    for (int i=0;i<tokens.length-1;i++){
    	String tok1 = tokens[i];
    	String tok2 = tokens[i+1];
    	Dictionary unigramCounts = languageModel.bigram.get(tok2);
    	/*  Get the unigram counts of tok2 if they exist
    		(remember they are supposed to be sorted by highest value) */
    	List<HashMap.Entry<String, Integer>> entries;
    	try {
    		entries = unigramCounts.getEntries();
    	} catch (NullPointerException e) {
    		entries = new LinkedList<HashMap.Entry<String, Integer>>();
    	}
    	//System.out.println("tok1: "+tok1);
    	/* For each token in the counts: */ 
    	for (HashMap.Entry<String, Integer> entry : entries){
    		//System.out.println("entry: "+entry.getKey());
    		/* if its frequency before tok2 is greater than the bigram threshold: */
    		if (entry.getValue() >= BIGRAM_FREQ_THRESHOLD){
    			int editDistance = NoisyChannelModel.editDistance(entry.getKey(), tok1, EDIT_THRESHOLD);
    			/* if its edit distance is less than the edit distance threshold: */
    			if (editDistance>0 & editDistance<=EDIT_THRESHOLD){
    				/* Add this token to the correction map: */

    				String correction = entry.getKey();
    				
	    			HashMap<Integer,HashSet<String>> editDistanceToCorrections;
	    			if (tokensToEditDistanceToCorrections.containsKey(tok1)){
	    				editDistanceToCorrections = tokensToEditDistanceToCorrections.get(tok1);
	    				
	    				HashSet<String> corrections;
	    				if (editDistanceToCorrections.containsKey(editDistance)){
	    					corrections = editDistanceToCorrections.get(editDistance);
	    				} else {
	    					corrections = new HashSet<String>();
	    				}
	    				
	    				corrections.add(correction);
	    				
	    				editDistanceToCorrections.put(editDistance, corrections);
	    				
	    				tokensToEditDistanceToCorrections.put(tok1,editDistanceToCorrections);
	    				
	    			} else {
	    				editDistanceToCorrections = new HashMap<Integer,HashSet<String>>();
	    				HashSet<String> newCorrections = new HashSet<String>();
	    				newCorrections.add(correction);
	    				editDistanceToCorrections.put(editDistance, newCorrections);
	    				
	    				tokensToEditDistanceToCorrections.put(tok1, editDistanceToCorrections);
	    			}
	    			
    			}
    		}
    	}
    }
    
    return tokensToEditDistanceToCorrections;
  }
  


  /**
 * @author Omer ASUS
 * Generate all edits of a misspelled word which are within some edit distance.
 * Actually if the distance is set to more than 1 runtime is crazy so don't try it.
 * @param misspelling	A misspelled word
 * @param distance		How distant the generated edits can be 
 * @return	a set of all edits of misspelling within the given distance
 */
 public static HashSet<String> generateEdits(String misspelling, int distance) {
	HashSet<String> edits = new HashSet<String>();
	
	for (Character c:nonLetters){
		misspelling.replace(c.toString(), "");
	}
	
	edits.addAll(generateTranspositions(misspelling));
	edits.addAll(generateSubstitutions(misspelling));
	edits.addAll(generateDeletions(misspelling));
	edits.addAll(generateInsertions(misspelling));
	
	distance--;
	
	while (distance >0){
		HashSet<String> newEdits = new HashSet<String>();
		for (String edit : edits){
			newEdits.addAll(generateTranspositions(edit));
			newEdits.addAll(generateSubstitutions(edit));
			newEdits.addAll(generateDeletions(edit));
			newEdits.addAll(generateInsertions(edit));
		}
		edits.addAll(newEdits);
	}
	
	return edits;
	
 }

 /**@author Omer Korat */
 public static HashSet<String> generateInsertions(String misspelling) {
	 HashSet<String> insertions = new HashSet<String>();
	 
	 char[] chars = (misspelling+"$").toCharArray();
	 
	 for (int i=0;i<chars.length-1;i++){
		 for (char c : letters){
			 char[] newChars = new char[chars.length];
			 int offset = 0;
			 for (int j=0;j<chars.length-1;j++){
				 if (j==i){
					 newChars[i]=c;
					 offset++;
				 } 
				 newChars[j+offset] = chars[j];
			 }
			 String correction = String.valueOf(newChars);
			 insertions.add(correction);
		 }
	 }
	 
	 return insertions;
 }
 
 /**@author Omer Korat */
 public static HashSet<String> generateDeletions(String misspelling) {
	 HashSet<String> deletions= new HashSet<String>();
	 
	 char[] chars = (misspelling).toCharArray();
	 
	 for (int i=0;i<chars.length;i++){
		 char[] newChars = new char[chars.length-1];
		 int offset = 0;
		 for (int j=0;j<chars.length-1;j++){
			 if (j==i){
				 offset++;
			 } 
			 newChars[j] = chars[j+offset];
		 }
		 String correction = String.valueOf(newChars);
		 deletions.add(correction);
	 }
	 
	 return deletions;
 }
 
 /**@author Omer Korat */
 public static HashSet<String> generateSubstitutions(String misspelling) {
	 HashSet<String> substitutions = new HashSet<String>();
	 
	 char[] chars = (misspelling).toCharArray();
	 
	 for (int i=0;i<chars.length;i++){
		 for (char c : letters){
			 if (c!=chars[i]){
				 char[] newChars = new char[chars.length];
				 for (int j=0;j<chars.length;j++){
					 if (j==i){
						 newChars[j] = c;
					 } else {
						 newChars[j] = chars[j];
					 }
				 }
				 String correction = String.valueOf(newChars);
				 substitutions.add(correction);
			 }
		 }
	 }
	 
	 return substitutions;
 }

/**@author Omer Korat */
public static HashSet<String> generateTranspositions(String misspelling) {
	 HashSet<String> transpositions = new HashSet<String>();
	 

	 char[] chars = (misspelling).toCharArray();
	 
	 for (int i=0;i<chars.length-1;i++){
		 char[] newChars = new char[chars.length];
		 for (int j=0;j<chars.length;j++){
			 if (j==i){
				 newChars[j] = chars[j+1];
				 newChars[j+1] = chars[j];
			 } else if (j==i+1) {
			 } else {
				 newChars[j] = chars[j];
			 }
		 }
		 String correction = String.valueOf(newChars);
		 transpositions.add(correction);
	 }
	 return transpositions;
 }
 
 /**
  * @author Omer Korat
  * @param tokens	array of tokens, basis of candidate
  * @param index		index in which a word will be replaced
  * @param token		the token that will be replaced in tokens	
  * @return	the candidate which is tokens with token replaced in tokens[index]
  */
  public String buildCandidate(String[] tokens, int index, String correction) {

	  StringBuilder builder = new StringBuilder();
	  for (int i=0; i<tokens.length;i++) {
		    if (builder.length() > 0) {
		        builder.append(" ");
		    }
		    if (i==index){
		    	builder.append(correction);
		    } else {
		    	builder.append(tokens[i]);
		    }
		}

	  return builder.toString();
  }
  
  
	 /**
	 * @author Omer ASUS
	 * @param arr
	 * @return	all pairs in the array
	 */
	public static Iterator<int[]> cartesianProductIter(int[] arr){
	
		ArrayList<int[]> pairs = new ArrayList<int[]>();
		  
	      for (int i = 0; i < arr.length; i++)
	          for (int j = i; j < arr.length; j++){
	        	  int[] pair = new int[2];
	        	  pair[0]=i;pair[1]=j;
	        	  pairs.add(pair);
	          }
	      
	      return pairs.iterator();
	  }


  /**
 * @author Omer Korat
 * @param A map
 * @return A set with all the values of all the keys in the map.
 */
  public HashSet<String> getAllValues(HashMap<Integer,HashSet<String>> map) {
	HashSet<String> values = new HashSet<String>();
	for (Map.Entry entry: map.entrySet()){
		values.addAll(map.get(entry.getKey()));
	}
	return values;
  }

  /** @author Omer Korat */
  public static void printArray(String[] a){
	  for (int i = 0; i < a.length; i++) {
		       System.out.print(a[i] + " ");
		    }
		    System.out.println();
		}
  /** @author  Omer Korat*/
  public static void printArray(int[] a){
	  for (int i = 0; i < a.length; i++) {
		       System.out.print(a[i] + " ");
		    }
		    System.out.println();
		}
  /** @author Omer Korat */
  public static void printArray(char[] a){
	  for (int i = 0; i < a.length; i++) {
		       System.out.print(a[i] + " ");
		    }
		    System.out.println();
		}
}


