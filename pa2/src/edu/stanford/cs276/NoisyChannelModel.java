package edu.stanford.cs276;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * NoisyChannelModel class constructs a channel model (which is a model of errors that
 * occur in our dataset of queries - the probability of a character getting inserted into a
 * query, deleted from a query, substituted by another character or transposed with a neighboring
 * character in the query).
 * 
 * This class uses the Singleton design pattern
 * (https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class NoisyChannelModel implements Serializable {
	
  private static final long serialVersionUID = 1L;
  private static NoisyChannelModel ncm_ = null;
  EditCostModel ecm_ = null;

  EmpiricalCostModel empiricalCostModel = null;
  UniformCostModel uniformCostModel = null;
  
  
  /*
   * Feel free to add more members here.
   * Your code here ...
   */
 
  public double editProbability(String original, String R) {
	  
	  // Edit distance between original and R
	  int distance = editDistance(original, R);
	  
	  // P(R) - Prior probability of R given the language model
	  String[] RTokens = R.trim().split("\\s+");
	  double RProbability = RunCorrector.languageModel.interpolatedProbability(RTokens);
	  
	  // P(Q|R) - the probability of original given that the user meant R
	  double editProbability = ecm_.editProbability(original, R, distance);
	  
	  // P(Q|R)*P(R) - since probabilities are calculated in logs, it is the sun
	  return editProbability+RProbability;
  }
  

  
  /*
   * @author Omer Korat
   */
  public static int editDistance(String s1, String s2){
	  
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
  private NoisyChannelModel(String editsFile) throws Exception {
	empiricalCostModel = new EmpiricalCostModel(editsFile);
    uniformCostModel = new UniformCostModel();
   
  }

  /**
   * Creates a new NoisyChannelModel object from the query corpus. This method should be used to
   * create a new object rather than calling the constructor directly from outside this class
   */
  public static NoisyChannelModel create(String editsFile) throws Exception {
	  if (ncm_ == null) {
      ncm_ = new NoisyChannelModel(editsFile);
    }
    return ncm_;
  }

  /**
   * Loads the model object (and all associated data) from disk
   */
  public static NoisyChannelModel load() throws Exception {
    try {
      // Don't load from disk if it's already been loaded.
      if (ncm_ == null) {
        FileInputStream fiA = new FileInputStream(Config.noisyChannelFile);
        ObjectInputStream oisA = new ObjectInputStream(fiA);
        ncm_ = (NoisyChannelModel) oisA.readObject();
        oisA.close();
      }
    } catch (Exception e) {
      throw new Exception("Unable to load noisy channel model.  You may not have run buildmodels.sh!");
    }
    return ncm_;
  }

  /**
   * Saves the object (and all associated data, e.g. EditCostModel) to disk
   */
  public void save() throws Exception {
    FileOutputStream saveFile = new FileOutputStream(Config.noisyChannelFile);
    ObjectOutputStream save = new ObjectOutputStream(saveFile);
    save.writeObject(this);
    save.close();
  }

  /**
   * Set the EditCostModel to be used
   */
  public void setProbabilityType(String type) throws Exception {
    if (type.equals("empirical")) {
      ecm_ = this.empiricalCostModel;
    } else if (type.equals("uniform")) {
      ecm_ = this.uniformCostModel;
    } else {
      throw new Exception("Invalid noisy channel probability type: "
          + "must be one of <uniform | empirical>");
    }
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
