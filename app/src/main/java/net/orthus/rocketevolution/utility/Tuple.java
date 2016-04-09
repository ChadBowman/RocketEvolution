package net.orthus.rocketevolution.utility;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Chad on 2/11/2016.
 */
public class Tuple<O> implements Collection<O>{

    //==== INSTANCE VARIABLES

    private ArrayList<O> list;

    //===== CONSTRUCTORS

    public Tuple(){
        list = new ArrayList<>();
    }

    public Tuple(O value){
        list = new ArrayList<>();
        list.add(value);
    }

    public Tuple(Collection<O> values){
        list = new ArrayList<>();
        list.addAll(values);
    }

    public Tuple(ArrayList<O> list){
        this.list = list;
    }

    //===== PUBLIC METHODS
    public Tuple<O>swap(){

        try {
            if (list.size() == 2) {
                Tuple<O> t = new Tuple<>();
                t.add(list.get(1));
                t.add(list.get(0)) ;
                return t;
            }else
                throw new Exception("Only a Tuple with two elements can be swapped!");

        }catch( Exception e){
            e.printStackTrace();
            return null;
        }

    } // Tuple()


    public O get(int index){
        return list.get(index);
    }

    public void set(int index, O value){
        list.set(index, value);
    }

    public O first(){ return list.get(0); }
    public O last(){ return list.get(list.size() - 1); }

    public int size(){
        return list.size();
    }

    //===== INTERFACES

    @Override
    public boolean add(O object) {
        return list.add(object);
    }

    @Override
    public boolean isEmpty(){
        return list.isEmpty();
    }

    @Override
    public boolean addAll(Collection<? extends O> collection) {
        return list.addAll(collection);
    }

    @Override
    public void clear() {
        list = new ArrayList<>();
    }

    @Override
    public boolean contains(Object target) {
        return list.contains(target);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return list.containsAll(collection);
    }

    @NonNull
    @Override
    public Iterator<O> iterator() {
        return list.iterator();
    }

    @Override
    public boolean remove(Object object) {
        return list.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return list.retainAll(collection);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return list.toArray(array);
    }

    //=== ACCESSORS
    public ArrayList<O> getList(){ return list; }
    public void setTuple(ArrayList<O> x){ list = x; }

} // Tuple
