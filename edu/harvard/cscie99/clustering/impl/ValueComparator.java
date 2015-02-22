package edu.harvard.cscie99.clustering.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



// TODO: Auto-generated Javadoc
/**
 * This code has been borrowed
 * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
 * @author 
 *
 */
public class ValueComparator 
{
	
	/**
	 * Sort by value.
	 *
	 * @param map the map
	 * @return the map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public Map sortByValue(Map map)
	{
		List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          
			public int compare(Object o1, Object o2)
			{
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();)
	    {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 

}
