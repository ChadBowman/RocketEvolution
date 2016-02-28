package net.orthus.rocketevolution.utility;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Chad on 2/11/2016.
 */
public class Hash<K, V> {

    //===== INSTANCE VARIABLES

    private ArrayList<Pair<K, V>> hash;

    //===== CONSTRUCTOR

    public Hash(){
        // make this list
        hash = new ArrayList<Pair<K, V>>();

        //add some null elements
        hash.addAll(nullList(10));
    }

    //===== PUBLIC METHODS

    public void add(K key, V value){

        Pair<K, V> entry = new Pair<K, V>(key, value);

        // if hash full, double the space
        if(isFull())
            reHash();

        int inx = key.hashCode() % hash.size();

        // check for collision, iterate to space
        while(hash.get(inx) == null)
            inx = (inx + 1) % hash.size();

        // insert the pair
        hash.add(inx, entry);

    } // add()

    public V get(K key){

        // calculate hash key
        int inx = key.hashCode() % hash.size();

        // element should be found first pass, but if not,
        // iterate through until the correct Value is found.
        while(!hash.get(inx).first.equals(key))
            inx = (inx + 1) % hash.size();

        return hash.get(inx).second;
    }

    public void delete(K key){

        // grab key
        int inx = key.hashCode() % hash.size();

        // find object to delete
        while(!hash.get(inx).first.equals(key))
            inx = (inx + 1) % hash.size();

        hash.remove(inx);
    }

    public int numberOfEntries(){

        int entries = 0;
        for(int i=0; i < hash.size(); i++)
            if(hash.get(i) != null)
                entries++;

        return entries;
    }

    //=====  PRIVATE VARIABLES

    private void reHash(){

        ArrayList<Pair<K, V>> newHash = new ArrayList<Pair<K, V>>(hash.size() * 2);
        newHash.addAll(nullList(hash.size() * 2));

        for(Pair<K, V> t : hash)
            newHash.add(t.first.hashCode() % newHash.size(), t);

        hash = newHash;
    }

    private ArrayList<Pair<K, V>> nullList(int number){

        ArrayList<Pair<K, V>> list = new ArrayList<Pair<K, V>>(number);

        for(int i=0; i <  number; i++)
            list.add(null);

        return list;
    }

    private int getAvailable(){

        int available = 0;
        for(Pair<K, V> val : hash)
            if(val == null)
                available++;

        return available;
    }

    private boolean isFull(){
        if(getAvailable() == 0)
            return true;

        return false;
    }

} // Hash
