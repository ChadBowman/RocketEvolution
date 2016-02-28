package net.orthus.rocketevolution.utility;

import java.util.ArrayList;

/**
 * Created by Chad on 2/11/2016.
 */
public class Tuple<O> {

    //==== INSTANCE VARIABLES

    private ArrayList<O> list;

    //===== CONSTRUCTORS

    public Tuple(){
        list = new ArrayList<O>();
    }

    public Tuple(O... values){
        list = new ArrayList<O>();

        // add them all in
        for( O value : values)
            list.add(value);
    }

    //===== PUBLIC METHODS
    public Tuple(ArrayList<O> list){
        this.list = list;
    }

    public Tuple<O>swap(){

        try {
            if (list.size() == 2)
                return new Tuple<O>(list.get(1), list.get(0));
            else
                throw new Exception("Only a Tuple with two elements can be swapped!");

        }catch( Exception e){
            e.printStackTrace();
            return null;
        }

    } // Tuple()

    public void add(O element){
        list.add(element);
    }

    public O get(int index){
        return list.get(index);
    }

    public O first(){ return list.get(0); }
    public O last(){ return list.get(list.size() - 1); }

    public int size(){
        return list.size();
    }

    //=== ACCESSORS
    public ArrayList<O> getList(){ return list; }
    public void setTuple(ArrayList<O> x){ list = x; }
}
