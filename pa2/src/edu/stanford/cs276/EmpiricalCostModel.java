package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

/**
 * Implement {@link EditCostModel} interface. Use the query corpus to learn a model
 * of errors that occur in our dataset of queries, and use this to compute P(R|Q).
 */
public class EmpiricalCostModel implements EditCostModel {
  private static final long serialVersionUID = 1L;
  private int errors;
  
  public EmpiricalCostModel(String editsFile) throws IOException {
    BufferedReader input = new BufferedReader(new FileReader(editsFile));
    System.out.println("Constructing edit distance map...");
    String line = null;
    
    
    Dictionary editCounts = new Dictionary();
    errors = 0; 
    while ((line = input.readLine()) != null) {
      errors++;
      Scanner lineSc = new Scanner(line);
      lineSc.useDelimiter("\t");
      String noisy = lineSc.next();
      String clean = lineSc.next();

      /*
       * TODO: Your code here
       */
      
      String[] noisyTokens = noisy.trim().split("\\s");
      String[] cleanTokens = clean.trim().split("\\s");
      
      // If there is the same number of tokens, it means that one of the words is misspelled. Find it
      if (noisyTokens.length==cleanTokens.length){
    	  for (int i=0;i<noisyTokens.length;i++){
    		  String noisyToken = noisyTokens[i];
    		  String cleanToken = cleanTokens[i];
    		  Pair<String, String> edit = findEdit(noisyToken, cleanToken);
    		  if (edit.getFirst() != "" | edit.getSecond() != ""){
    			  // Add to editCounts
    			  editCounts.add(edit.getFirst()+"|"+edit.getSecond());
    		  }
    	  }
      } 
      // If there are different numbers in noisy and clean tokens, 
      // find the space which is missing/redundant
      else {
    	  //TODO: implement this.
      }
       
      
    }
    
    input.close();
    System.out.println("Done.");
  }

  // You need to add code for this interface method to calculate the proper empirical cost.
  @Override
  public double editProbability(String original, String R, int distance) {
    return 0.5;
    /*
     * TODO: Your code here
     */
  }
  
  
  
  /**
 * @author Omer ASUS
 * Iterate over all possible distance 1 edits of noisyToken until cleanToken is found, 
 * and then return a pair which represents the edit that is necessary. 
 * @return pair of two strings where the first element is the confused characters 
 * and the second element is the intended characters. 
 * Returns a pair with two empty strings if no edit is found.
 */
  public Pair<String, String> findEdit(String noisyToken, String cleanToken) {
	char[] chars;
	
	 /* Try insertions */
	 chars = (noisyToken+"$").toCharArray();
	 
	 
	 for (int i=0;i<chars.length-1;i++){
		 for (char c : CandidateGenerator.letters){
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
			 if (correction.equals(cleanToken)){
				 Pair<String,String> edit = 
						 new Pair<String, String>(String.valueOf(c), String.valueOf(chars[i]));
				 return edit;
			 }
		 }
	 }

	 /* Try deletions */
 	 chars = (noisyToken).toCharArray();
 	 
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
 		 if (correction.equals(cleanToken)){
			 Pair<String,String> edit = 
					 new Pair<String, String>(String.valueOf(chars[i]), "");
			 return edit;
 		 }
 	 }
 	
 	/* Try substitutions */ 
 	chars = (noisyToken).toCharArray();
	 
	 for (int i=0;i<chars.length;i++){
		 for (char c : CandidateGenerator.letters){
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
			 if (correction.equals(cleanToken)){
				 Pair<String,String> edit = 
						 new Pair<String, String>(String.valueOf(chars[i]), String.valueOf(c));
				 return edit;
			 }
			}
		}
	 }
 	
	 /* Try transpositions */ 
	 chars = (noisyToken).toCharArray();
	 
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
		 if (correction.equals(cleanToken)){
			 Pair<String,String> edit = 
					 new Pair<String, String>(String.valueOf(chars[i+1])+String.valueOf(chars[i]),
							 String.valueOf(chars[i])+String.valueOf(chars[i+1]));
			 
			 return edit;
		 }	
	 }
	 
	 Pair<String,String> edit = 
			 new Pair<String, String>("","");
	 
	 return edit;
	 
	}
}
