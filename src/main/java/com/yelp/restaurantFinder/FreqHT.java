package com.yelp.restaurantFinder;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * This class creates a frequency hash tables that aids in the creation of tf-idf metrics. Storing and calculating them
 * when the time is appropriate in the code.
 *
 * @author David Hennigan and Anthony Impellizzeri
 */
public class FreqHT implements java.io.Serializable {
    static final class Node {
	String key;
	Node next;
	int count;
	int numDocsAppearedIn;
	double tf_idf;

	// Object value;
	private Node(String k, int c, double tfidf, int numDocsAppearedIn, Node n) {
		key = k; count = c; tf_idf = tfidf; this.numDocsAppearedIn = numDocsAppearedIn; next = n; }
    }
    Node[] table = new Node[8]; // always a power of 2
    int size = 0;
	int totalCount = 0;
	int reviewCount = 0;
    public boolean contains(Object key) {
	int h = key.hashCode();
	int i = h & (table.length - 1);
	for (Node e = table[i]; e != null; e = e.next) {
	    if (key.equals(e.key))
			return true;
	}
	return false;
    }

    //added getCount
    public int getCount(String key){
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
    public void add(String key) {
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

	private void add(String key, int count, double tf_idf, int numDocsAppeared){
		int h = key.hashCode();
		int i = h & (table.length - 1);

		table[i] = new Node(key, count,  tf_idf, numDocsAppeared, table[i]); //count param included
		++size;
		if ((float)size/table.length >= 0.75f) {
			resize();
		}
	}

    private void resize() {
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
    public void printAll() {
        for (int i = 0; i < table.length; ++i)
            for (Node e = table[i]; e != null; e = e.next)
                System.out.println(e.key + " "  + e.count + " " + e.numDocsAppearedIn);
        System.out.println();
    }
	@Serial
    private void writeObject(ObjectOutputStream s) throws Exception {
	s.defaultWriteObject();
	s.writeInt(size);
	s.writeInt(reviewCount);
		for (int i = 0; i < table.length; ++i) {
			for (Node e = table[i]; e != null; e = e.next) {
				s.writeObject(e.key);
				s.writeInt(e.count);
				s.writeDouble(e.tf_idf);
				s.writeInt(e.numDocsAppearedIn);
			}
		}

		s.writeObject(totalCount);
    }

	@Serial
    private void readObject(ObjectInputStream s) throws Exception {
	s.defaultReadObject();
	int n = s.readInt();
	reviewCount = s.readInt();

//	for (int i = 0; i < n; ++i)
//	    add(s.readObject(), s.readInt(), s.readDouble(), s.readInt());

	totalCount = s.readInt();
    }

	public void writeTable(FileChannel writingChannel) throws IOException{
		ByteBuffer tableBuffer = ByteBuffer.allocate(12);
		tableBuffer.putInt(size);
		tableBuffer.putInt(totalCount);
		tableBuffer.putInt(reviewCount);
		tableBuffer.position(0);
		writingChannel.write(tableBuffer);

		for (int i = 0; i < table.length; ++i) {
			for (Node e = table[i]; e != null; e = e.next) {
				ByteBuffer nodeBuffer = ByteBuffer.allocate(56);
				nodeBuffer.limit(40);
				nodeBuffer.put(e.key.getBytes(StandardCharsets.UTF_8));
				nodeBuffer.limit(56);
				nodeBuffer.position(40);
				nodeBuffer.putInt(e.count);
				nodeBuffer.putDouble(e.tf_idf);
				nodeBuffer.putInt(e.numDocsAppearedIn);
				nodeBuffer.position(0);
				writingChannel.write(nodeBuffer);
			}
		}
	}

	public void readTable(FileChannel readingChannel) throws IOException{
		ByteBuffer tableBuffer = ByteBuffer.allocate(12);
		readingChannel.read(tableBuffer);
		tableBuffer.position(0);
		int size = tableBuffer.getInt();
		int totalCount = tableBuffer.getInt();
		int reviewCount = tableBuffer.getInt();

		for ( int i = 0; i < size; i++){
			tableBuffer = ByteBuffer.allocate(56);
			readingChannel.read(tableBuffer);
			tableBuffer.position(0);
			tableBuffer.limit(40);
			byte[] keyBytes = new byte[40];
			tableBuffer.get(keyBytes);
			tableBuffer.limit(56);
			tableBuffer.position(40);
			int count = tableBuffer.getInt();
			double tf_idf = tableBuffer.getDouble();
			int numDocsAppearedIn = tableBuffer.getInt();
			add((new String(keyBytes, StandardCharsets.UTF_8)).replace("\0", ""), count, tf_idf, numDocsAppearedIn);
		}

		this.totalCount = totalCount;
		this.reviewCount = reviewCount;


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



