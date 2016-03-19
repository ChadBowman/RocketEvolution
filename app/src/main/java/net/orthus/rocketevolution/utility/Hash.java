package net.orthus.rocketevolution.utility;

import android.util.Pair;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Chad on 2/11/2016.
 */
public class Hash<K, V> {

    //===== CONSTANTS
    private static final double GROW_HASH = 0.8,
                                SHRINK_HASH = 0.5;

    private static final int BASE = 9;

    //===== INSTANCE VARIABLES
    private List<Pair<K, V>> hash;
    private double entries;

    //===== CONSTRUCTOR
    public Hash(){
        // create this list
        hash = new ArrayList<Pair<K, V>>(emptyList(BASE));
        entries = 0.0;
    }

    public Hash(int size){
        // size must be equal to or larger than the base size
        size = (size < BASE)? BASE : size;
        hash = new ArrayList<Pair<K, V>>(emptyList(size));
        entries = 0.0;
    }

    //===== PUBLIC METHODS

    public List<Pair<K, V>> getList(){ return hash; }

    public List<K> keys(){

        ArrayList<K> keys = new ArrayList<>();

        for(Pair<K, V> pair : hash)
            if(pair != null)
                keys.add(pair.first);

        return keys;
    }

    public List<V> values(){

        ArrayList<V> vals = new ArrayList<>();

        for(Pair<K, V> pair : hash)
            if(pair != null)
                vals.add(pair.second);

        return vals;
    }

    public int entries(){
        return (int) (entries + 0.5);
    }

    public void add(K key, V value){

        add(new Pair<K, V>(key, value));
    }

    public void add(Pair<K, V> entry){

        // if hash full, double the space
        if(entries / hash.size() > GROW_HASH)
            grow();

        int inx = index(entry.first);

        // check for collision, iterate to space
        while(hash.get(inx) != null)
            inx = (inx + 1) % hash.size();

        // insert the pair
        hash.set(inx, entry);
        entries++;
    }

    public boolean delete(K key){

        // grab key
        int inx = index(key);

        // find object to delete
        int passes = 0;
        while(!hash.get(inx).first.equals(key) && passes < hash.size()){
            inx = (inx + 1) % hash.size();
            passes++;
        }

        // no key was found
        if(passes == hash.size())
            return false;

        // key found, set location null
        hash.set(inx, null);
        entries--;

        // if hash half full, reduce hash
        if(entries / hash.size() < SHRINK_HASH && hash.size() > BASE)
            shrink();

        return true;

    } // delete()

    public V get(K key){

        // calculate hash key
        int inx = index(key);

        // element should be found first pass, but if not,
        // iterate through until the correct Value is found.
        int passes = 0;
        for(; passes < hash.size(); passes++){

            if(hash.get(inx) != null) {
                if (hash.get(inx).first.equals(key))
                    passes = hash.size();
                else
                    inx = (inx + 1) % hash.size();
            }else
                inx = (inx + 1) % hash.size();
        }

        // no key found
        if(passes == hash.size())
            return null;

        return hash.get(inx).second;

    } // end get()

    //=====  PRIVATE VARIABLES

    private int index(K key){
        return Math.abs(key.hashCode() % hash.size());
    }

    private void grow(){

        List<Pair<K, V>> old = hash;
        hash = new ArrayList<>(emptyList((int) (old.size() * 1.5)));
        entries = 0.0;

        for(Pair<K, V> element : old)
            if(element != null)
                add(element);
    }

    private void shrink(){

        List<Pair<K, V>> old = hash;
        hash = new ArrayList<>(emptyList((int) (old.size() * 0.8)));
        entries = 0.0;

        for(Pair<K, V> element : old)
            if(element != null)
                add(element);
    }

    private ArrayList<Pair<K, V>> emptyList(int number){
        ArrayList<Pair<K, V>> list = new ArrayList<>(number);

        for(int i=0; i < number; i++)
            list.add(null);

        return list;
    }

} // Hash
