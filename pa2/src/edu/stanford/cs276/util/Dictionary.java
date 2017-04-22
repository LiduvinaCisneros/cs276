package edu.stanford.cs276.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public class Dictionary implements Serializable {

  private int termCount;
  private HashMap<String, Integer> map;
  public int termCount() {
    return termCount;
  }

  public Dictionary() {
    termCount = 0;

    map = new HashMap<String, Integer>();
  }

  public void add(String term) {
    termCount++;
    if (map.containsKey(term)) {
      map.put(term, map.get(term) + 1);
    } else {
      map.put(term, 1);
    }
  }
  
  public List<HashMap.Entry<String, Integer>> getEntries(){
	  List<HashMap.Entry<String, Integer>> list =
		      new LinkedList<HashMap.Entry<String, Integer>>( map.entrySet() );
	  return list;
  }
  
  public int count(String term) {
    if (map.containsKey(term)) {
      return map.get(term);
    } else {
      return 0;
    }
  }
  
  public void sortByValue(){
	  List<HashMap.Entry<String, Integer>> list =
		      new LinkedList<HashMap.Entry<String, Integer>>( map.entrySet() );
			  Collections.sort( list, new Comparator<Map.Entry<String, Integer>>(){
		      public int compare( HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2 ){
		          return -1* (o1.getValue()).compareTo( o2.getValue() );
		      	}
			  } );
		  HashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		  for (HashMap.Entry<String, Integer> entry : list)
		  {
		      result.put(entry.getKey(), entry.getValue());
		      //System.out.println(entry.getKey() + " "+ entry.getValue());
		  }
  }
  
  public static <String, Integer extends Comparable<? super Integer>> HashMap<String, Integer> sortMapByValue( HashMap<String, Integer> map ){
	  List<HashMap.Entry<String, Integer>> list =
      new LinkedList<HashMap.Entry<String, Integer>>( map.entrySet() );
	  Collections.sort( list, new Comparator<Map.Entry<String, Integer>>(){
      public int compare( HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2 ){
          return (o1.getValue()).compareTo( o2.getValue() );
      	}
	  } );
  HashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
  for (HashMap.Entry<String, Integer> entry : list)
  {
	  
      result.put(entry.getKey(), entry.getValue());
  }
  return result;
}
}
