package com.yelp.restaurantFinder;
import java.util.*;
import java.io.*;

class FreqHT implements java.io.Serializable {
    static final class Node {
	Object key;
	Node next;
	int count;
	// Object value;
	Node(Object k, int c, Node n) { key = k; count = c; next = n; }
    }
    Node[] table = new Node[8]; // always a power of 2
    int size = 0;
    boolean contains(Object key) {
	int h = key.hashCode();
	int i = h & (table.length - 1);
	for (Node e = table[i]; e != null; e = e.next) {
	    if (key.equals(e.key))
		return true;
	}
	return false;
    }

    //added getCount
    public int getCount(Object key){
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next){
            if (key.equals(e.key)){
                return e.count;
            }
        }
        return 0;
    }

    //increase count before return
    void add(Object key) {
	int h = key.hashCode();
	int i = h & (table.length - 1);
	for (Node e = table[i]; e != null; e = e.next) {
	    if (key.equals(e.key))
                e.count++;
		return;
	}
	table[i] = new Node(key, 1, table[i]); //count param included
	++size;
	if ((float)size/table.length >= 0.75f)
	    resize();
    }

    void resize() {
	Node[] oldTable = table;
	int oldCapacity = oldTable.length;
	int newCapacity = oldCapacity << 1;
	Node[] newTable = new Node[newCapacity];
	for (int i = 0; i < oldCapacity; ++i) {
	    for (Node e = oldTable[i]; e != null; e = e.next) {
		int h = e.key.hashCode();
		int j = h & (newTable.length - 1);
		newTable[j] = new Node(e.key, e.count, newTable[j]); //e.count
	    }
	}
	table = newTable;
    }
    void remove(Object key) {
	int h = key.hashCode();
	int i = h & (table.length - 1);
	Node e = table[i], p = null;
	while (e != null) {
	    if (key.equals(e.key)) {
		if (p == null)
		    table[i] = e.next;
		else
		    p.next = e.next;
		break;
	    }
	    p = e;
	    e = e.next;
	}
    }
    void printAll() {
        for (int i = 0; i < table.length; ++i)
            for (Node e = table[i]; e != null; e = e.next)
                System.out.println(e.key);
        System.out.println();
    }
    private void writeObject(ObjectOutputStream s) throws Exception {
	s.defaultWriteObject();
	s.writeInt(size);
	for (int i = 0; i < table.length; ++i) {
	    for (Node e = table[i]; e != null; e = e.next) {
		s.writeObject(e.key);
	    }
	}
    }
    private void readObject(ObjectInputStream s) throws Exception {
	s.defaultReadObject();
	int n = s.readInt();
	for (int i = 0; i < n; ++i)
	    add(s.readObject());
    }
/*
    public static void main(String[] args){
        FreqHT table = new FreqHT();
        for (int i = 1; i <= 10; i++){
            table.add(i);
            table.printAll();
        }
        table.add(2);
        table.add(5);
        table.add(1);
        table.printAll();
        System.out.println(table.getCount(2));
    }
*/
}



