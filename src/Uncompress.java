import java.io.IOException;
import java.util.HashMap;


public class Uncompress {
	private TreeNode huffTree;
	private int bitsWritten;
	private HashMap<Integer, String> m;	

	
	public Uncompress(TreeNode huffTree) {
		this.huffTree = huffTree;
		bitsWritten = 0;
		m = new HashMap<Integer, String>();
	}
	
	
	public void uncompressSCF(BitInputStream bis, BitOutputStream bos) throws IOException {		
		readHuffTree(bis, bos, huffTree); 
	}
	
	public void uncompressSTF(BitInputStream bis, BitOutputStream bos) throws IOException {
		bis.readBits(IHuffConstants.BITS_PER_INT); //size of tree
    	
    	TreeNode STFTree = null; //create empty TreeNode
    	
    	STFTree = travelSTFTree(bis); //our huffman tree for STF
    	
    	HuffTree temp = new HuffTree(STFTree);    	
    	m = temp.travelTree(STFTree); //creates map 
    	
    	readHuffTree(bis, bos, STFTree); 
	}
	
	//recursive call to recreate huffTree
	private TreeNode travelSTFTree(BitInputStream bis) throws IOException {
	    
		int bite = bis.readBits(1); //read next bit
		if(bite == 0) { //if 0, we know internal node
			TreeNode n = new TreeNode(-1, -1);
			n.setLeft(travelSTFTree(bis));	    			
			
			n.setRight(travelSTFTree(bis));
			return n;
		}
		
		else { //at a leaf
			int bit = bis.readBits(IHuffConstants.BITS_PER_WORD+1);
			TreeNode leaf = new TreeNode(bit, 0);
			return leaf; //bit is value, 0 is weight	
		}

    }
	
	//returns bits written
	public int getBitsWritten() {
		return bitsWritten;
	}
	
	//travel huffTree and write to file
	public void readHuffTree(BitInputStream bis, BitOutputStream bos, TreeNode n) throws IOException {
		TreeNode root = n;	    	
    	TreeNode curr = n;

    	//writing data to the file
    	int bite = bis.readBits(1);
    	while(bite != -1 && curr.getValue() != IHuffConstants.PSEUDO_EOF) {
    		if(curr.isLeaf()) { //base case
    			bos.writeBits(IHuffConstants.BITS_PER_WORD, curr.getValue());
    			bitsWritten += IHuffConstants.BITS_PER_WORD;
    			curr = root; //reset to root
    		}
    		else {
	    		if(bite == 1) {//go right 
	    			curr = curr.getRight();
	    		}
	    		else { //go left
	    			curr = curr.getLeft();
	    		}
	    		bite = bis.readBits(1); //read next
    		}
    	}   
	}
	
	public HashMap<Integer, String> getMap() {
		return m; //returns map of encodings for STF
	}
}
