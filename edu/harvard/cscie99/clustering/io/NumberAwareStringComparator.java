package edu.harvard.cscie99.clustering.io;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * This code has been borrowed.
 * Source: http://codereview.stackexchange.com/questions/37192/number-aware-string-sorting-with-comparator
 * @author 
 *
 */
public class NumberAwareStringComparator implements Comparator<String>
{
     
     /* (non-Javadoc)
      * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
      */
     public int compare(String s1, String s2)
     {
            String[] s1Parts = s1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            String[] s2Parts = s2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

            int i = 0;
            while(i < s1Parts.length && i < s2Parts.length){

                //if parts are the same
                if(s1Parts[i].compareTo(s2Parts[i]) == 0){
                    ++i;
                }else{
                    try{

                        int intS1 = Integer.parseInt(s1Parts[i]);
                        int intS2 = Integer.parseInt(s2Parts[i]);

                        //if the parse works

                        int diff = intS1 - intS2; 
                        if(diff == 0){
                            ++i;
                        }else{
                            return diff;
                        }
                    }catch(Exception ex){
                        return s1.compareTo(s2);
                    }
                }//end else
            }//end while

            if(s1.length() < s2.length()){
                return -1;
            }else if(s1.length() > s2.length()){
                return 1;
            }else{
                return 0;
            }
        }
}