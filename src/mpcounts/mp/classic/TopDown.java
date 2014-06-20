package mpcounts.mp.classic;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class TopDown {
	private BottomUp bt;
	private TreeMap<String,Integer> counts;
	/**
	 * @param bt
	 */
	public TopDown(BottomUp bt) {
		this.bt = bt;
		this.counts = new TreeMap<String,Integer>();
	}
	
	public void selectCounts(TreeMap<String,Integer> parent){
		if(bt.getChildren().size() == 0)return;
		for(String dclass :  bt.getDclasses()){
			TreeMap<Integer,Integer> scores = bt.getScores().get(dclass);
			Integer minScore = -1;
			Integer minCount = -1;
			for(Integer count : scores.keySet()){
				Integer curScore = scores.get(count) + Math.abs(count - parent.get(dclass));
				
				if(minScore == -1 || curScore < minScore){
					minScore = curScore;
					minCount = count;
				}
			}
			counts.put(dclass, minCount);
		}
		writeCountFile();
		for(BottomUp child : bt.getChildren()){
			TopDown td = new TopDown(child);
			td.selectCounts(counts);
		}
	}
	
	private void writeCountFile(){
		if(counts.size() == 0){
			System.err.println("no counts or total number of proteins is zero -- wrong order of functions?");
			System.exit(0);
		}
		String filename = bt.getDir()+"/mpcounts";
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.println("class\testimated.counts");
			for(String dclass : bt.getDclasses()){
				pw.println(dclass+"\t"+counts.get(dclass));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not write to  mpcounts file in dir: "+bt.getDir());
			System.exit(0);
		}
	}
	
	
	
}
