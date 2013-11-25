import java.util.HashMap;


public class Counts {
	private int sizeSCF;
	private int sizeSTF;
	private HashMap<Integer, String> m;
	private int header;
	private TreeNode huffTree;
	
	public Counts(HashMap<Integer, String> theMap, int headerFormat, TreeNode t) {
		sizeSCF = 0;
		sizeSTF = 0;
		m = theMap;
		header = headerFormat;
		huffTree = t;
	}
	
	//writes the magic number and SCF method
	public void count(int[] myCounts, BitInputStream bis) throws Exception {
		
		BitOutputStream bos = new BitOutputStream("test2.hf");
		
		//count magic num
		sizeSCF += IHuffConstants.BITS_PER_INT;
		sizeSTF += IHuffConstants.BITS_PER_INT;		
		
		//count SCF/STF info	
		sizeSCF += IHuffConstants.BITS_PER_INT;	
		sizeSTF += IHuffConstants.BITS_PER_INT;
		
		//count SCF header info
		if(header == IHuffConstants.STORE_COUNTS) {
			for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
				sizeSCF += IHuffConstants.BITS_PER_INT;
			}
		}
		
		//count STF header info
		else if(header == IHuffConstants.STORE_TREE) {	
			//write header info
			int count = 0;
			count = STFCount(huffTree, count);
			
			//writing count/size of tree
			sizeSTF += IHuffConstants.BITS_PER_INT;
			travelSTFTree(huffTree, bos);
		}		
		
		
		//count data
		countData(myCounts);
		
      //write PSEUDO_EOF
      String EOF = m.get(IHuffConstants.PSEUDO_EOF);
      for(int i = 0; i < EOF.length(); i++) {
      	sizeSCF += 1;
      	sizeSTF += 1;
      } 

		bos.close(); //flush		
	}	
	

	public void countData(int[] myCounts) {
		for(int i : m.keySet()) {
			if(i != 256) { //if not pseudo_EOF char
				String s = m.get(i);
				
				sizeSCF += s.length()*myCounts[i]; //length of encoding times freq
				sizeSTF += s.length()*myCounts[i];
			}
		}
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
	
	
	public int getSCFCount() {
		return sizeSCF;
	}
	
	public int getSTFCount() {
		return sizeSTF;
	}
		
}
