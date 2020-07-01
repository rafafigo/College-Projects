package pt.tecnico.sauron.silo.replica;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VecTimestamp implements Iterator<Map.Entry<Integer, Integer>> {

    private Map<Integer, Integer> TS;
    private Iterator<Map.Entry<Integer, Integer>> iterator = null;

    public VecTimestamp() {
        this.TS = new HashMap<>();
    }

    public Set<Integer> keys() {
        return this.TS.keySet();
    }

    public void update(Integer key, Integer value) {
        this.TS.put(key, value);
    }

    public void increment(Integer key) { this.TS.put(key, this.TS.getOrDefault(key, 0) + 1); }

    public void decrement(Integer key) { this.TS.put(key, this.TS.getOrDefault(key, 1) - 1); }

    public Integer get(Integer key) {
        return this.TS.getOrDefault(key, 0);
    }

    /***
     * @param other Timestamp to use in the comparison
     * @param eqAccept Boolean when activated the Method performs isLessOrEqual behaviour
     * @return this.TS (eqAccept ? <= : <) other.TS ? true : false
     ***/
    public boolean isLess(VecTimestamp other, boolean eqAccept) {

        boolean flag = false;

        for (Integer key : Sets.union(this.keys(), other.keys())) {
            Integer v1 = this.get(key);
            Integer v2 = other.get(key);
            if (v1 > v2) return false;
            if (v1 < v2) flag = true;
        }
        return eqAccept || flag;
    }

    @Override
    public boolean hasNext() {
        if (this.iterator == null) this.iterator = this.TS.entrySet().iterator();
        boolean hasN = this.iterator.hasNext();
        if (!hasN) this.iterator = null;
        return hasN;
    }

    @Override
    public Map.Entry<Integer, Integer> next() {
        return this.iterator.next();
    }

    @Override
    public String toString() {
        return this.TS.toString();
    }
}
