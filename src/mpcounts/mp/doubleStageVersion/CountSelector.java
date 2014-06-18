package mpcounts.mp.doubleStageVersion;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class CountSelector {

	private PenaltyCalculator pc; 
	private Integer total;
	private TreeMap<String,Integer> counts;
	
	/**
	 * @param pc
	 */
	public CountSelector(PenaltyCalculator pc) {
		this.pc = pc;
		this.counts = new TreeMap<String,Integer>();
	}
	
	public void initSelection(){
		//total count
		Integer minpenalty = -1;
		for(Integer t :  pc.getPenaltyTotal().keySet()){
			if(minpenalty < 0 || minpenalty > pc.getPenaltyTotal().get(t)){
				minpenalty = pc.getPenaltyTotal().get(t);
				total = t;
			}
		}
		
		// class counts 
		for(String dclass :  PenaltyCalculator.getDclasses()){
			Integer count = pc.minPenalityCountConstrainted(dclass, total);
			counts.put(dclass, count);
		}
		
		//start recursion
		for(PenaltyCalculator child : pc.getChildren()){
			CountSelector cs = new CountSelector(child);
			cs.select(this);
		}
		
		//write file
		writeFile();
	}
	

	protected void select(CountSelector parent){
		if(pc.getChildren().size() == 0)return;
		Integer minpenalty = -1;
		for(Integer t : pc.getPenaltyTotal().keySet()){
			Integer curpenalty = Math.abs(parent.getTotal() - t) + pc.getPenaltyTotal().get(t);
			if(minpenalty < 0 || minpenalty > curpenalty){
				total = t;
				minpenalty = curpenalty;
			}
		}
		selectClassCounts(parent);
		
		//continue recursion
		for(PenaltyCalculator child : pc.getChildren()){
			CountSelector cs = new CountSelector(child);
			cs.select(this);
		}
		
		//write file
		writeFile();
	}
	
	private void selectClassCounts (CountSelector parent){
		for(String dclass :  PenaltyCalculator.getDclasses()){
			Integer count = -1;
			Integer minpenalty = -1;
			for(Integer i = 0; i <= total; i++){
				Integer curpenalty = pc.getPenaltyClass().get(dclass).get(i) + Math.abs(i - parent.getCounts().get(dclass));
				if(minpenalty < 0 || curpenalty < minpenalty){
					count = i;
					minpenalty= curpenalty;
				}
			}
			counts.put(dclass, count);
		}
	}

	private void writeFile() {
		if(counts.size() == 0 || total == null){
			System.err.println("no counts or total number of proteins is zero -- wrong order of functions?");
			System.exit(0);
		}
		String filename = pc.getDir()+"/mpcounts";
		try {
			PrintWriter pw = new PrintWriter(filename);
			pw.println("class\testimated.counts");
			for(String dclass : PenaltyCalculator.getDclasses()){
				pw.println(dclass+"\t"+((double)counts.get(dclass)/(double)total));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not write to  mpcounts file in dir: "+pc.getDir());
			System.exit(0);
		}
		
	}
	/**
	 * @return the pc
	 */
	public PenaltyCalculator getPc() {
		return pc;
	}
	/**
	 * @return the total
	 */
	public Integer getTotal() {
		return total;
	}
	/**
	 * @return the counts
	 */
	public TreeMap<String, Integer> getCounts() {
		return counts;
	}
}
