import java.util.HashMap;


public class Compress {
	//instance variables
	private HashMap<Integer, String> m;
	private int[] myCounts;
	private int writtenBits;
	
	public Compress(HashMap<Integer, String> m, int[] myCounts) {
		this.m = m;
		this.myCounts = myCounts;
		this.writtenBits = 0;
	}
	
	//writes the magic number and SCF method
		public void writeSCF(BitInputStream bis, BitOutputStream bos) throws Exception {

			//write magic num
			bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
			writtenBits += IHuffConstants.BITS_PER_INT;
			
			//write SCF info
			bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);	
			writtenBits += IHuffConstants.BITS_PER_INT;
			
			//write header info
			for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
				bos.writeBits(IHuffConstants.BITS_PER_INT, myCounts[i]);
				writtenBits += IHuffConstants.BITS_PER_INT;
			}
			
			//write data
			write(bis, bos);	


			bos.close(); //flush		
		}	
		
		


		//writes encoded data and PSEUDO EOF
		public void write(BitInputStream bis, BitOutputStream bos) throws Exception {
			
	        int bite = bis.read(); 
	        while(bite!=-1) {   
	        	String temp = m.get(bite);
	        	for(int i = 0; i < temp.length(); i++) {
	        		if(temp.charAt(i) == '0') 
	        			bos.writeBits(1,0);        			
	        		else 
	        			bos.writeBits(1,1);
	        		writtenBits += 1;
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
	        	writtenBits += 1;
	        } 
	        
		}
		
		
		
		
		//STF method		
		public void writeSTF(TreeNode root, BitInputStream bis, BitOutputStream bos) throws Exception {
			//write magic num
			bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
			writtenBits += IHuffConstants.BITS_PER_INT;
			
			//write STF info
			bos.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_TREE);
			writtenBits += IHuffConstants.BITS_PER_INT;
			
			//write header info
			int count = 0;
			count = STFCount(root, count);
			
			//writing count/size of tree
			bos.writeBits(IHuffConstants.BITS_PER_INT, count);
			writtenBits += IHuffConstants.BITS_PER_INT;
			
			travelSTFTree(root, bos);
			
			//write data
			write(bis, bos);
			
			bos.close(); //flush
		}
		
		//counts size of tree
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
		
		
		//STANDARD TREE FORMAT
	    public void travelSTFTree(TreeNode root, BitOutputStream bos) {
	            travelSTFTreeHelper(root, bos);
	    }
	    
	    
	    private void travelSTFTreeHelper(TreeNode t, BitOutputStream bos){
			if (t.isLeaf()) { //if a leaf
				bos.writeBits(1, 1); //write 1
				bos.writeBits(IHuffConstants.BITS_PER_WORD+1, t.getValue()); //write ASCII value
				writtenBits += IHuffConstants.BITS_PER_WORD+1;
			}

			else {
				if (t != null) {
					bos.writeBits(1, 0); //if internal node, write 0
					writtenBits += 1;
					travelSTFTreeHelper(t.getLeft(), bos); //go left
					travelSTFTreeHelper(t.getRight(), bos); //go right
				}
			}
	    }
	    
	    public int getWrittenBits() {
	    	return writtenBits;
	    }
		
}
