package toolbox;


import java.util.*;


final public class KeyFreqCounter {
    
    final private HashMap<String, Integer> map = new HashMap(); 
    
    final public int getFreq(String key) {
        Integer freq = this.map.get(key);
        if (freq == null) {
            return 0;
        } else {
            return freq;
        }
    }
    
    final public void addCount(String key) {
        Integer freq = this.map.get(key);
        if (freq == null) {
            this.map.put(key, 1);
        } else {
            this.map.put(key, freq+1);
        }
    }
    
    //降順に並べた
    final public ArrayList<StringIntTuple> getSortedItems() {
        ArrayList<StringIntTuple> ret = new ArrayList(); 
        for (String key:this.map.keySet()) {
            ret.add(new StringIntTuple(key, this.map.get(key)));
        }
        Collections.sort(ret);
        return ret;
    }
}
