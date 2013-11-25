import java.util.HashMap;


public class HuffTree {
	
	//instance variable for HuffTree
	private TreeNode huffTree;
	
	//instance variable for Map containing codes
	private HashMap<Integer, String> m;
	
	public HuffTree() {
		huffTree = new TreeNode(0, 0);
		m = new HashMap<Integer, String>();
	}
	
	public HuffTree(TreeNode t) {
		huffTree = t;
		m = new HashMap<Integer, String>();
	}
	
	//creates huffTree
	public TreeNode createHuffTree(PriorityQueue<TreeNode> q) throws Exception {    	
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
    	}    	
    	huffTree = q.peek();
    	return huffTree;
	}
	
	

	//travel the tree, travels huff tree to create map
	//returns map
	public HashMap<Integer, String> travelTree(TreeNode root) {		
		StringBuilder s = new StringBuilder();
		
		travelTreeHelper(root, s); //travel the tree
		return m;
	}
	
	

	//recursion, travels huff tree to create map
	private void travelTreeHelper(TreeNode t, StringBuilder s) {
		
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
	
}
