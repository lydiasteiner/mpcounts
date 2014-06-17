/**
 * 
 */
package mpcounts;


import java.util.TreeMap;

import mpcounts.mp.classic.BottomUp;
import mpcounts.mp.classic.TopDown;

/**
 * @author lydia
 *
 */
public class MPCounts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 1){
			System.err.println("USAGE: java -jar mpcounts.jar <directory of tree root>");
			System.exit(0);
		}
		System.out.print("Starting calculations of the scores (bottom up step)...");
	
		String fileroot = args[0];
		
		BottomUp root = new BottomUp(fileroot);
		root.buildTree();
		root.calcScores();
		System.out.println("done");
		System.out.print("Selecting best score (top down step)...");
		TreeMap<String,Integer> fakeParent = new TreeMap<String,Integer>();
		for(String dclass :  BottomUp.getDclasses()){
			TreeMap<Integer,Integer> scores = root.getScores().get(dclass);
			Integer minScore = -1; 
			Integer minCount = -1;
			for(Integer count : scores.keySet()){
				if(minScore == -1 || scores.get(count) < minScore){
					minScore = scores.get(count);
					minCount = count;
				}
			}
			fakeParent.put(dclass, minCount);
		}
		TopDown rootTD = new TopDown(root);
		rootTD.selectCounts(fakeParent);
		System.out.println("done");
		
	}

}

