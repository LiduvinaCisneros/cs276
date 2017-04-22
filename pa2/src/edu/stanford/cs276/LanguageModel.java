package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOError;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;

/**
 * LanguageModel class constructs a language model from the training corpus.
 * This model will be used to score generated query candidates.
 * 
 * This class uses the Singleton design pattern
 * (https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class LanguageModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
  private static LanguageModel lm_;

  Set<String> vocabulary = new HashSet<String>(); 
  double V;
  double T=0;
  double LAMBDA = 0.1;
  
  Dictionary unigram = new Dictionary();
  HashMap<String, Dictionary> bigram = new HashMap<String, Dictionary>();
  
  Dictionary laplaceSmoothedUnigram = new Dictionary();
  HashMap<String, Dictionary> laplaceSmoothedBigram = new HashMap<String, Dictionary>();

  
  /*
   * Feel free to add more members here (e.g., a data structure that stores bigrams)
   */

  /**
   * Constructor
   * IMPORTANT NOTE: you should NOT change the access level for this constructor to 'public', 
   * and you should NOT call this constructor outside of this class.  This class is intended
   * to follow the "Singleton" design pattern, which ensures that there is only ONE object of
   * this type in existence at any time.  In most circumstances, you should get a handle to a 
   * NoisyChannelModel object by using the static 'create' and 'load' methods below, which you
   * should not need to modify unless you are making substantial changes to the architecture
   * of the starter code.  
   *
   * For more info about the Singleton pattern, see https://en.wikipedia.org/wiki/Singleton_pattern.  
   */
  private LanguageModel(String corpusFilePath) throws Exception {
    constructDictionaries(corpusFilePath);
    
  }

  public double interpolatedProbability(String[] tokens){
	  String first = tokens[0];
	  double unigramCount = (double)unigram.count(first);
	  double p= -Math.log10(unigramCount/T);

	  for (int i=1;i<tokens.length;i++){
		  String token1 = tokens[i-1];
		  String token2 = tokens[i];
		  double bigramCount = (double)bigram.get(token1).count(token2);
		  p -= Math.log10(LAMBDA*(unigram.count(token2)/T)+
				  		(1-LAMBDA)*(bigramCount/unigram.count(token1)));
	  }
	  
	  return p;
  }

  
  public double unigramProbability(String[] tokens){
	  double p= 0;
	  
	  for (int i=0;i<tokens.length;i++){
		  String token = tokens[i];
		  double count = (double)unigram.count(token);
		  p -= Math.log10(count/T);
	  }
	  return p;
  }
  
  public double bigramProbability(String[] tokens){
	  String first = tokens[0];
	  double unigramCount = (double)unigram.count(first);
	  double p= -Math.log10(unigramCount/T);

	  for (int i=1;i<tokens.length;i++){
		  String token1 = tokens[i-1];
		  String token2 = tokens[i];
		  double bigramCount = (double)bigram.get(token1).count(token2);
		  p -= Math.log10(bigramCount/unigram.count(token1));
	  }
	  
	  return p;
  }

  
  public double laplaceSmoothedunigramProb(String[] tokens){
	  double p= 0;
	  
	  for (int i=0;i<tokens.length;i++){
		  String token = tokens[i];
		  double count = (double)unigram.count(token);
		  p -= Math.log10((count+1)/(T+V));
	  }
	  return p;
  }
  
  public double laplaceSmoothedbigramProb(String[] tokens){
	  String first = tokens[0];
	  double unigramCount = (double)unigram.count(first);
	  double p= -Math.log10((unigramCount+1)/(T+V));

	  for (int i=1;i<tokens.length;i++){
		  String token1 = tokens[i-1];
		  String token2 = tokens[i];
		  double bigramCount = (double)bigram.get(token1).count(token2);
		  p -= Math.log10((bigramCount+1)/(unigram.count(token1)+V));
	  }
	  
	  return p;
  }
  
  
  /**
   * This method is called by the constructor, and computes language model parameters 
   * (i.e. counts of unigrams, bigrams, etc.), which are then stored in the class members
   * declared above.  
   */
  public void constructDictionaries(String corpusFilePath) throws Exception {

    System.out.println("Constructing dictionaries...");
    File dir = new File(corpusFilePath);
    for (File file : dir.listFiles()) {
      if (".".equals(file.getName()) || "..".equals(file.getName())) {
        continue; // Ignore the self and parent aliases.
      }
      System.out.printf("Reading data file %s ...\n", file.getName());
      BufferedReader input = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = input.readLine()) != null) {
        /*
         * Remember: each line is a document (refer to PA2 handout)
         * TODO: Your code here
         */
    	
    	  // Split the line into tokens
    	  String[] tokens = line.trim().split("\\s+");

    	  // Go over each index of the tokens
    	  for (int i=0;i<tokens.length;i++) {
    		String tok1 = tokens[i];
    		T++; // For each token increase the total number of tokens
    		vocabulary.add(tok1); // Add each token to the vocabulary
    		unigram.add(tok1);
    		// For all the tokens except the first one
    		if (i!=0){
    			String tok2 = tokens[i-1];
    			//System.out.println("Tok2: "+tok2 );
    			// If tok2 is not in the bigram hashmap, associate an empty dictionary with it
    			if (!bigram.containsKey(tok2)){
    				bigram.put(tok2, new Dictionary());
    			} 
    			// Add tok1 as one of the entries in the dictionary associated with tok2
    			bigram.get(tok2).add(tok1);
    			bigram.get(tok2).sortByValue(); // This is a bad way to do this
    		}
    	}
    	
        
      }
      
      V = vocabulary.size(); // Set V to be the number of words in the dictionary
      
      
      // Sort all unigram counts of each token (I still couldn't get it to work)
      Iterator<String> vocabIter1 = vocabulary.iterator();
      while (vocabIter1.hasNext()){
    	  String token1 = vocabIter1.next();
    	  Dictionary unigramCounts = bigram.get(token1);
    	  

    	  //unigramCounts.sortByValue();
    	  Iterator<String> vocabIter2 = vocabulary.iterator();
    	  while (vocabIter2.hasNext()){
    		  String token2 = vocabIter2.next();
    		  int count;
    		  try {
    			  count = unigramCounts.count(token2);
    		  } catch (NullPointerException e) {
    			  count =0;
    		  }
    	  }
    	  //System.out.println(unigramCounts.count("sam"));
    	  //System.out.println(bigram.get(token).count("I"));
    	  //bigram.put(token, unigramCounts);
    	  //System.out.println(bigram.get(token).count("sam"));
    	  
      
      }
      input.close();
    }
    System.out.println("Done.");
  }

  /**
   * Creates a new LanguageModel object from a corpus. This method should be used to create a
   * new object rather than calling the constructor directly from outside this class
   */
  public static LanguageModel create(String corpusFilePath) throws Exception {
    if (lm_ == null) {
      lm_ = new LanguageModel(corpusFilePath);
    }
    return lm_;
  }

  /**
   * Loads the language model object (and all associated data) from disk
   */
  public static LanguageModel load() throws Exception {
    try {
      if (lm_ == null) {
        FileInputStream fiA = new FileInputStream(Config.languageModelFile);
        ObjectInputStream oisA = new ObjectInputStream(fiA);
        lm_ = (LanguageModel) oisA.readObject();
      }
    } catch (Exception e) {
      throw new Exception("Unable to load language model.  You may not have run buildmodels.sh!");
    }
    return lm_;
  }

  /**
   * Saves the object (and all associated data) to disk
   */
  public void save() throws Exception {
    FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
    ObjectOutputStream save = new ObjectOutputStream(saveFile);
    save.writeObject(this);
    save.close();
  }
}
