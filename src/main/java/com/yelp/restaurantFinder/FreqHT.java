package com.yelp.restaurantFinder;
import java.io.*;

/**
 * This class creates a frequency hash tables that aids in the creation of tf-idf metrics. Storing and calculating them
 * when the time is appropriate in the code.
 *
 * @author David Hennigan and Anthony Impellizzeri
 */
class FreqHT implements java.io.Serializable {
    static final class Node {
	Object key;
	Node next;
	int count;
	int numDocsAppearedIn;
	double tf_idf;

	// Object value;
	Node(Object k, int c, double tfidf, int numDocsAppearedIn, Node n) {
		key = k; count = c; tf_idf = tfidf; this.numDocsAppearedIn = numDocsAppearedIn; next = n; }
    }
    Node[] table = new Node[8]; // always a power of 2
    int size = 0;
	int totalCount = 0;
	int reviewCount = 0;
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
	    	if (key.equals(e.key)) {
				e.count++;
				totalCount++;
				return;
			}
		}

		table[i] = new Node(key, 1, 0, 0, table[i]); //count param included
		++size;
		totalCount++;
		if ((float)size/table.length >= 0.75f) {
				resize();
		}
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
		newTable[j] = new Node(e.key, e.count, e.tf_idf, e.numDocsAppearedIn, newTable[j]); //e.count
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

	/**
	 * Either returns a words existing tf-idf value or calculates the words tf-idf value if none is stored.
	 * @param key the desired word
	 * @return a words corresponding tf-idf value
	 */
	public double getTFIDF(String key){
		int h = key.hashCode();
		int i = h & (table.length - 1);
		for (Node e = table[i]; e != null; e = e.next) {
			if (key.equals(e.key)) {
				if (e.tf_idf != 0) {
					return e.tf_idf;
				} else{
					e.tf_idf = ( (double) e.count / totalCount )
							* Math.log( (double) reviewCount / e.numDocsAppearedIn ) * 100;
				}
			}
		}
		return 0;
	}

	public void setNumDocsAppearedIn(String key, int docCount){
		int h = key.hashCode();
		int i = h & (table.length - 1);
		for (Node e = table[i]; e != null; e = e.next) {
			if (key.equals(e.key)) {
				e.numDocsAppearedIn = docCount;
				return;
			}
		}
	}

	public int getDocNum(String key){
		int h = key.hashCode();
		int i = h & (table.length - 1);
		for (Node e = table[i]; e != null; e = e.next) {
			if (key.equals(e.key)) {
				return e.numDocsAppearedIn;
			}
		}
		return 0;
	}

	public void setReviewCount(int reviewCount){
		this.reviewCount = reviewCount;
	}
}



