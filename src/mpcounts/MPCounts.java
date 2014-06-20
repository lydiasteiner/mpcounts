/**
 * 
 */
package mpcounts;


import java.util.TreeMap;

import mpcounts.mp.classic.BottomUp;
import mpcounts.mp.classic.TopDown;
import mpcounts.mp.doubleStageVersion.CountSelector;
import mpcounts.mp.doubleStageVersion.PenaltyCalculator;

/**
 * @author lydia
 *
 */
public class MPCounts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length > 2 || (args.length == 2 && !args[0].equals("-n") && !args.equals("-c"))){
			System.err.println("USAGE: java -jar mpcounts.jar [-n|c] <directory of tree root>");
			System.exit(0);
		}
		
	
		String fileroot = args[0];
		boolean norm = false;
		boolean cooc = false;
		if(fileroot.equals("-n")){
			norm = true;
			fileroot = args[1];
		}else if(fileroot.equals("-c")){
			cooc = true;
			fileroot = args[1];
		}

		if(!norm){
			System.out.print("Starting calculations of the scores (bottom up step)...");
			BottomUp root; 
			if(cooc)root = new BottomUp(fileroot,cooc);
			else root = new BottomUp(fileroot);
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
		}else{
			System.out.print("Starting calculations of the penalties (bottom up step)...");
			PenaltyCalculator root = new PenaltyCalculator(fileroot);
			TreeMap<String,Integer> max = root.getMax();
			root.calcPenaltyClass(max);
			System.out.println("done");
			System.out.print("Selecting best counts (top down step)...");
			CountSelector cs = new CountSelector(root);
			cs.initSelection();
			System.out.println("done");
			
		}
	}

}

