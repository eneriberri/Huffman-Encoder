import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SimpleHuffProcessor implements IHuffProcessor {
    
    private HuffViewer myViewer;
    private int header;
    private HashMap<Integer, String> m;  //double check this
    private int[] myCounts;
    private TreeNode huffTree;
    private int bitsSaved;
    
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        try{
        	header = headerFormat;
        	BitInputStream bis = new BitInputStream(in);
	        PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>(in, bis, true);
	        ArrayList<TreeNode> arr = q.arr(); //get array of frequencies
	        
	        //add frequencies from array to priority queue
	    	for(TreeNode tn: arr){ 
				q.add(tn);			
			}	
	    	
	    	HuffTree temp = new HuffTree();
	    	huffTree = temp.createHuffTree(q); //huffman tree
	    	
	    	m = temp.travelTree(huffTree); //creates map 
	    	
	    	myCounts = q.getMyCounts(); //array of counts for SCF method
	    	
	    	//size of tree
	    	TreeNode file = q.peek();
	    	int fileSize = (file.getWeight() - 1)*IHuffConstants.BITS_PER_WORD;

	    	Counts totalCounts = new Counts(m, headerFormat, huffTree);	    	
	    	totalCounts.count(myCounts, bis); 	    	
	    	
	    	int compressedBits = 0;
	    	if(headerFormat == IHuffConstants.STORE_COUNTS) 
	    		compressedBits = totalCounts.getSCFCount(); 
	    	
	    	
	    	else if(headerFormat == IHuffConstants.STORE_TREE) 
	    		compressedBits = totalCounts.getSTFCount();    	

	    	
	    	//print results to GUI
	    	//for loop or create a string
	    	showString("Counting characters in selected file\n");
	    	showString("Frequences: ");
	    	showString("ASCII" + "\t" + "Encode" + "\t\t\t" + "Char" + "  " + "Freq");
	    	for(int i : m.keySet()) {
	    		if(i != 256)
	    			showString(i + "\t" + m.get(i) + "\t\t\t" + (char) i + "\t" + myCounts[i]);
	    	}
	    	
	    	
	    	//number of bits saved	
	    	bitsSaved = fileSize-compressedBits;
	    	return bitsSaved;
        }
        
        
        catch (Exception e){ //if exception found
        	e.printStackTrace(); //debugging, print stack trace
        	throw new IOException("preprocess not implemented");        	
        }        

    }
    
    
    
    
    
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {        

        try {
        	
        	if(force || bitsSaved >= 0) { //if force compression, or compressed file is smaller
	        	BitInputStream bis = new BitInputStream(in);
	        	BitOutputStream bos = new BitOutputStream(out);
	
	        	Compress c = new Compress(m, myCounts);
		        if(header == IHuffConstants.STORE_COUNTS) 	 
		        	c.writeSCF(bis, bos); //write compressed Standard Count Format 
		        
		        if(header == IHuffConstants.STORE_TREE) 
		        	c.writeSTF(huffTree, bis, bos); //write compressed Standard Tree Format	
		        
		        //return bits written
		        int bitsWritten = c.getWrittenBits();
		        showString("Bits Written: " + bitsWritten);
		        return bitsWritten;
        	}
        	
        	else if(bitsSaved < 0 && !force) { //if force is false
        		//error message
        		myViewer.showError("Compressed file is bigger. Select " +
        				"'force compression' option to compress.");
        		return 0;
        	}
        	
        	return 0;
	         
        }
        
        catch(Exception e) {
        	e.printStackTrace(); //debugging, print stack trace
        	throw new IOException("compress is not implemented");
        } 

    }
    
    
    
    public int uncompress(InputStream in, OutputStream out) throws IOException { 
        
        try {
        	BitInputStream bis = new BitInputStream(in); //compressed file
        	BitOutputStream bos = new BitOutputStream(out);
        	
	    	int bitMagic = bis.readBits(IHuffConstants.BITS_PER_INT);
	    	if(bitMagic == IHuffConstants.MAGIC_NUMBER) {	    	
	    		int bitHeader = bis.readBits(IHuffConstants.BITS_PER_INT);
	    		
	    		//IF STANDARD COUNT FORMAT
	    		if(bitHeader == IHuffConstants.STORE_COUNTS) {	    			
	            	PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>(in, bis, false);
	            	ArrayList<TreeNode> arr = q.arr(); //get array of frequencies
	    	        
	    	    	for(TreeNode tn: arr){ //add freq's from array to priority queue
	    	    		q.add(tn);			
	    			}	
	    	    	
	    	    	HuffTree temp = new HuffTree();
	    	    	huffTree = temp.createHuffTree(q); //huffman tree
	    	    	m = temp.travelTree(huffTree); //creates map 
	    	    	
	    	    	bis = q.getBitInputStream();
	    	    	
	    	    	Uncompress u = new Uncompress(huffTree);
	    	    	u.uncompressSCF(bis, bos);	
	    	    	
	    	    	showString("DECOMPRESSING: Codes for values in file: \n");
	    	    	
	    	    	for(int i : m.keySet()) {
	    	    		if(i != 256)
	    	    			showString(i + "\t" + m.get(i) + "\t\t" + (char) i);
	    	    	}
	    	    	
	    	    	showString("Bits Written: " + u.getBitsWritten());
	    	    	return u.getBitsWritten();
	    		}	   			
	    		
	    		
	    		//IF STANDARD TREE FORMAT
	    		if(bitHeader == IHuffConstants.STORE_TREE) {
	    			Uncompress u = new Uncompress(huffTree);
	    	    	u.uncompressSTF(bis, bos);
	    	    	
	    	    	showString("DECOMPRESSING: Codes for values in file: \n");
	    	    	
	    	    	HashMap<Integer, String> STFmap = u.getMap();
	    	    	for(int i : STFmap.keySet()) {
	    	    		if(i != 256)
	    	    			showString(i + "\t" + STFmap.get(i) + "\t\t" + (char) i);
	    	    	}
	    	    	
	    	    	showString("Bits Written: " + u.getBitsWritten());
	    	    	return u.getBitsWritten();
	    		}
	    		bis.close();
	    	} //end of if magic num
        } //end of try
        
        catch(Exception e) {
        	e.printStackTrace(); //debugging, print stack trace
        	throw new IOException("uncompress not implemented");
        }
        
        return 0; 
    }



    
    
    public void setViewer(HuffViewer viewer) {
        myViewer = viewer;
    }

    
    
    private void showString(String s){
        myViewer.update(s);
    }

}
