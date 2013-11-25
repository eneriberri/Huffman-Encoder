import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


public class PriorityQueue<E> {
	//instance variables
	private LinkedList<TreeNode> container;
	private int size;
	
	//instance variables for parser
	private static final int FOUND = -2;
	ArrayList<TreeNode> array = new ArrayList<TreeNode>();
	
	//instance variable for SCF format and when we initially parse data   
	private int[] myCounts = new int[IHuffConstants.ALPH_SIZE];	
	
	
	private BitInputStream bis;
		
	//constructor
	public PriorityQueue(InputStream in, BitInputStream bis, boolean compress) throws IOException {
		container = new LinkedList<TreeNode>();
		size = 0;
		if(compress)
			parserCompress(in);	//parse the file and read bits
		else
			parserDecompress(bis);
	}	
	
	
	
	/**
	 * @purpose, add to queue by priority
	 * @param data, takes in TreeNode to be added to container
	 */
	public void add(TreeNode data) {
		if(size() == 0) { //if queue is empty, add
			container.add(data);
			size++;
		}
		
		else {
			int index = 0;
			for(int i = 0; i < size(); i++) {
				if(container.get(i).compareTo(data) < 0) {
					index++; //if data is less than, keep going
				}
				if(container.get(i).compareTo(data) == 0) { //if data is equal
					if(container.get(i).getValue() <= data.getValue()) {
						index++; //compare vals, keep going if greater
					}
				}
			}
			container.add(index, data); //add at index
			size++;
		}
	}
	
	
	//returns size
	public int size() {
		return size;
	}
	

	public TreeNode remove() {
		if(size() >= 1) {
			size--;
			return container.remove(0); //remove front element
		}
		return null;
	}

	
	//returns treenode at front of queue
	public TreeNode peek(){
		Iterator<TreeNode> it = container.iterator();
		return it.next();
	}
	
	
	//priority queue to string
	public String toString() {
		Iterator<TreeNode> it = container.iterator();
		String s = "[";
			while(it.hasNext()) {
				s += it.next().toString() + ", ";
			}
		s += "]";
		return s;
	}
	
	public void parserDecompress(BitInputStream bis) throws IOException
	{
        	for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) { //read 256 lines
        		int bits = bis.readBits(IHuffConstants.BITS_PER_INT);
        		myCounts[i] = bits; //add freq to myCounts    //myCounts[69] = 1
        		if(bits != 0) {
        			array.add(new TreeNode(i, myCounts[i])); //i = int index == char
        		}
        	}
//        }
        array.add(new TreeNode(IHuffConstants.PSEUDO_EOF, 1)); //add psuedo eof char to array
        this.bis = bis;
	}
	
	public BitInputStream getBitInputStream() {
		return bis;
	}
	
	
	
	//read file by bits and add into arraylist
	public void parserCompress(InputStream in) throws IOException {	    
	    
        BitInputStream bis = new BitInputStream(in);
        int bite = bis.read();        
        
        while(bite!=-1) {
            for(int ctr = 0; ctr < array.size(); ctr++) {
                if(array.get(ctr).myValue == bite) {
                    array.get(ctr).myWeight++; //increase freq
                    myCounts[bite]++; //increase myCount
                    bite = FOUND;	                    
                    break;
                } 
            } 
            if(bite != FOUND) {
                array.add(new TreeNode(bite, 1)); //freq is 1
                myCounts[bite] = 1; //create myCount
            } 
            
            bite = bis.read();
        } 
       
        //add pseudo_eof to frequency list
       array.add(new TreeNode(IHuffConstants.PSEUDO_EOF, 1));

	} 
	
	//returns arraylist of frequencies
	public ArrayList<TreeNode> arr() {
		return array;
	}
	
	//returns array of counts
	public int[] getMyCounts() {
		return myCounts;
	}
	
}

