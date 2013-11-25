import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;




public class PQueue<E> {
	
	//**********PRIORITY QUEUE CLASS
	//instance variables
	private LinkedList<TreeNode> container;
	private int size;
	
	//instance variables for parser
	public static final int FOUND = -2;
	ArrayList<TreeNode> array = new ArrayList<TreeNode>();
	
	//instance variable for SCF format    
	private int[] myCounts = new int[IHuffConstants.ALPH_SIZE];
	
	
	
	
	//**********HUFF TREE CLASS
	//instance variable for Map containing codes
	private HashMap<Integer, String> m;
	
	//instance variable for HuffTree
	private TreeNode huffTree;
	
	
	
	//constructor
	public PQueue() throws Exception {
		container = new LinkedList<TreeNode>();
		size = 0;
		parser();	//parse the file and read bits
		m = new HashMap<Integer, String>();
		huffTree = new TreeNode(0, 0);
	}
	
	
	//PRIORITY QUEUE CLASS
	//returns arraylist of frequencies
	public ArrayList<TreeNode> arr() {
		return array;
	}
	

	/**
	 * @purpose, add to queue by priority
	 * @param data, takes in TreeNode to be added to container
	 */
	//PRIORITY QUEUE CLASS
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
	//PRIORITY QUEUE CLASS
	public int size() {
		return size;
	}
	
	//PRIORITY QUEUE CLASS
	public TreeNode remove() {
		if(size() >= 1) {
			size--;
			return container.remove(0); //remove front element
		}
		return null;
	}

	//PRIORITY QUEUE CLASS
	//returns treenode at front of queue
	public TreeNode peek(){
		Iterator<TreeNode> it = container.iterator();
		return it.next();
	}
	
	//PRIORITY QUEUE CLASS
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
	
	
	//HUFF TREE CLASS
	//travel the tree, travels huff tree to create map
	public void travelTree(TreeNode root) {		
		StringBuilder s = new StringBuilder();
		
		travelTreeHelper(root, s); //travel the tree
		System.out.println("MAP " + m.toString());
	}
	
	
	//HUFF TREE CLASS
	//recursion, travels huff tree to create map
	public void travelTreeHelper(TreeNode t, StringBuilder s) {
		
		//base case: if you hit a leaf
		if(t.getLeft() == null && t.getRight() == null) {
			m.put(t.getValue(), s.toString());	//add to map		
		}
		
		else {
			if(t != null) {
				//if left exists
				if(t.getLeft() != null) { 
					s.append("0"); //append 0
					travelTreeHelper(t.getLeft(), s); //go left
					s.delete(s.length()-1, s.length()); //backtrack
				}
				
				//if right exists
				if(t.getRight() != null) { 
					s.append("1"); //append 1
					travelTreeHelper(t.getRight(), s); //go right
					s.delete(s.length()-1, s.length()); //backtrack
				}
			}
		}

	}
	
	
	
	//HUFF TREE CLASS
	//PRINT TREE
	public static void printTree(TreeNode root) {
        printTree(root, "");
    }
	
	//HUFF TREE CLASS
    public static void printTree(TreeNode n, String spaces) {
        if(n != null){
            printTree(n.getRight(), spaces + "  ");
            System.out.println(spaces + (char) n.getValue());
            printTree(n.getLeft(), spaces + "  ");
        }
    }
	
	
    //PRIORITY QUEUE CLASS
	//read file by bits and add into arraylist
	public void parser() throws Exception {	    
	    
	        BitInputStream bis = new BitInputStream("test.txt");
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
	        
	        
//	        for(int ctr = 0; ctr < array.size(); ctr++)  //debug
//	        {
//	            System.out.println((char)array.get(ctr).myValue + " "+array.get(ctr).myWeight);
//	        }
	       
//	       for(int i = 0; i < myCounts.length; i++) {
//	    	   System.out.println("MY COUNT: " + i + " " + myCounts[i]);
//	       }
	   } 
	
	
	
	
	
	
	//COMPRESSION
	private int sizeSCF = 0; //size of SCF compressed file
	
	//writes the magic number and SCF method
	public void writeSCF() throws Exception {
		BitInputStream bis = new BitInputStream("test.txt");
		BitOutputStream bos = new BitOutputStream("test2.hf");
		//write magic num
		bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
		sizeSCF += IHuffConstants.BITS_PER_INT;
		//write SCF info
		bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);	
		sizeSCF += IHuffConstants.BITS_PER_INT;
		
		//write header info
		for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			bos.writeBits(IHuffConstants.BITS_PER_INT, myCounts[i]);
			sizeSCF += IHuffConstants.BITS_PER_INT;
		}
		
		//write data
		write(bis, bos);		

		System.out.println("SCF FILE SIZE: " + sizeSCF);
		bos.close(); //flush		
	}	
	
	


	//writes encoded data and PSEUDO EOF
	public void write(BitInputStream bis, BitOutputStream bos) throws Exception {
		
        int bite = bis.read(); 
        while(bite!=-1) {   
        	String temp = m.get(bite);
        	System.out.println("TEMP: " + temp);
        	for(int i = 0; i < temp.length(); i++) {
        		if(temp.charAt(i) == '0') 
        			bos.writeBits(1,0);        			
        		else 
        			bos.writeBits(1,1);
        		sizeSCF += 1;
        		sizeSTF += 1;
        	}
            bite = bis.read();
        }   
        
        //write PSEUDO_EOF
        String EOF = m.get(IHuffConstants.PSEUDO_EOF);
        for(int i = 0; i < EOF.length(); i++) {
        	if(EOF.charAt(i) == '0')
    			bos.writeBits(1,0);
    		else 
    			bos.writeBits(1,1);
        	sizeSCF += 1;
        	sizeSTF += 1;
        } 
        
        bis.reset(); //flush
	}
	
	
	
	
	//STF method ***EDIT THIS USED FOR STANDARD TREE FORMAT
	private int sizeSTF = 0; //size of STF compressed file
	
	public void writeSTF(TreeNode root) throws Exception {
		BitInputStream bis = new BitInputStream("test.txt");
		BitOutputStream bos = new BitOutputStream("STFtest4.hf");
		//write magic num
		bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
		sizeSTF += IHuffConstants.BITS_PER_INT;
		
		//write STF info
		bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_TREE);
		sizeSTF += IHuffConstants.BITS_PER_INT;
		
		//write header info
		int count = 0;
		count = STFCount(root, count);
		System.out.println("COUNT: " + count);
		
		//writing count/size of tree
		bos.writeBits(IHuffConstants.BITS_PER_INT, count);
		sizeSTF += IHuffConstants.BITS_PER_INT;
		travelSTFTree(root, bos);
		
		//write data
		write(bis, bos);
		
		System.out.println("STF SIZE: " + sizeSTF);
		bos.close(); //flush
	}
	
	
	
	//STANDARD TREE FORMAT
    public void travelSTFTree(TreeNode root, BitOutputStream bos) {
            travelSTFTreeHelper(root, bos);
    }
    
    
    private void travelSTFTreeHelper(TreeNode t, BitOutputStream bos){
		if (t.getLeft() == null && t.getRight() == null) {
			bos.writeBits(1, 1);
			sizeSTF += 1;
			bos.writeBits(IHuffConstants.BITS_PER_WORD+1, t.getValue());
			sizeSTF += IHuffConstants.BITS_PER_WORD+1;
		}

		else {
			if (t != null) {
				bos.writeBits(1, 0);
				sizeSTF += 1;
				travelSTFTreeHelper(t.getLeft(), bos);
				travelSTFTreeHelper(t.getRight(), bos);
			}
		}
    }
	
	
	public int STFCount(TreeNode t, int count) {
		if (t.getLeft() == null && t.getRight() == null) {
			count += 10; //make a global instance var
			return count;
		}

		else {
			if (t != null) {
				count += 1 + STFCount(t.getLeft(), count) + STFCount(t.getRight(), count);
			}
		}
		
		return count;
	}
	
	
	
	
	//HUFFMAN TREE CLASS
	//creates huffTree
	public void createHuffTree() throws Exception {
		PQueue<TreeNode> q = new PQueue<TreeNode>();
        ArrayList<TreeNode> arr = q.arr();
    	
        for(TreeNode tn: arr){
			q.add(tn); //fill priority queue with freq and char's
		}
    	
        //build the tree
    	while(q.size() != 1) {
	    	TreeNode tn1 = q.remove();
	    	TreeNode tn2 = q.remove();
	    	
	    	if(tn2 != null) {
	    		int weight = tn1.getWeight()+tn2.getWeight();
	    		//set value to max value so you know not leaf
	    		TreeNode tn3 = new TreeNode(Integer.MAX_VALUE, weight, tn1, tn2);   
	    		q.add(tn3);
	    	}
	    	
	    	System.out.println("QUEUED TREE " + q.toString()); 
    	}
    	
    	huffTree = q.peek();
	}
	
	//HUFFMAN TREE CLASS
	//returns huffTree
	public TreeNode getHuffTree() {
		return huffTree;
	}
	
	
	
	
	
	
	//main method
    public static void main(String[] args) throws Exception {
    	PQueue<TreeNode> q = new PQueue<TreeNode>();
        ArrayList<TreeNode> arr = q.arr(); //get array of frequencies
    	for(TreeNode tn: arr){ //add frequencies from array to priority queue
			q.add(tn);
		}
    	//System.out.println("ArrayList " + arr.toString());
    	System.out.println("QUEUE " + q.toString()); //sorted queue
    	
    	
    	
    	//build the tree
    	while(q.size() != 1) {
	    	TreeNode tn1 = q.remove();
	    	TreeNode tn2 = q.remove();
	    	
	    	if(tn2 != null) {
	    		int weight = tn1.getWeight()+tn2.getWeight();
	    		//set value to max value so you know not leaf
	    		TreeNode tn3 = new TreeNode(Integer.MAX_VALUE, weight, tn1, tn2);   
	    		q.add(tn3);
	    	}
	    	
	    	System.out.println("QUEUED TREE " + q.toString());	 
	    	
    	}
    	
    	TreeNode tn5 = q.peek();
    	printTree(tn5);

    	
    	
    	q.travelTree(tn5);
    	
    	//q.writeSCF();
    	q.writeSTF(tn5);
    	

    } 
	
}

