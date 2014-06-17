package mpcounts.mp.doubleStageVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import mpcounts.mp.classic.BottomUp;

public class PenaltyCalculator {
	private String dir;
	private TreeMap<String, TreeMap<Integer, Integer>> penaltyClass;
	private TreeMap<Integer,Integer> penaltyTotal;
	private ArrayList<PenaltyCalculator>  children;
	private static List<String> dclasses =	Arrays.asList("Wac","Wme","Wph","Wub","Eac","Eme","Eub","Rac","Rme","Rph");
	

	/**
	 * @param dir
	 */
	public PenaltyCalculator(String dir) {
		this.dir = dir;
		this.children = new ArrayList<PenaltyCalculator> ();
		this.penaltyClass = new TreeMap<String, TreeMap<Integer, Integer>> ();
		this.penaltyTotal = new TreeMap<Integer,Integer> ();
		
		// read directory and create BottomUp object for each directory
		File directory = new File(this.dir);
		File[] files = directory.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if(files[i].getName().startsWith(".")){continue;}//skip hidden files
				if (files[i].isDirectory()) {
					PenaltyCalculator c = new PenaltyCalculator(files[i].getAbsolutePath());
					children.add(c);
				}
			}
		}
		
	}
	

	public void calcPenaltyClass(){
		// get real counts with score 0 for leaves
		if(this.children.size() == 0){
			readCounts();
			return;
		}
		
		// maximal counts of children nodes
		Integer max = getMax();
		for(String dclass : PenaltyCalculator.getDclasses()){
			this.penaltyClass.put(dclass, new TreeMap<Integer,Integer>());
			for(Integer i = 0; i <= max; i++){
				this.penaltyClass.get(dclass).put(new Integer(i), minPenalty(dclass,i));
			}
		}

		calcPenaltyTotal();
	}
	


	private void calcPenaltyTotal(){
		Integer max = getMax();
		for(Integer i = 0; i <= max; i++){
			//penalty for difference in counts
			Integer pen1 = 0;
			for(PenaltyCalculator child : children){
				Integer minChild = -1;
				for(Integer childCount : child.getPenaltyTotal().keySet()){
					Integer curPen = Math.abs(i-childCount) + child.getPenaltyTotal().get(childCount);
					if(minChild < 0 || curPen < minChild)minChild = curPen;
				}
				pen1 += minChild;
			}
			//min penalty for score change satisfying the current count
			Integer pen2 = 0;
			for(String dclass : PenaltyCalculator.getDclasses()){
				pen2 += minPenaltyConstrainted(dclass,i);
			}
			//sum up for total penalty
			penaltyTotal.put(i, pen1+pen2);
		}
	}
	
	
	private Integer minPenaltyConstrainted(String dclass, Integer upperBound){
		Integer minPen = Integer.MAX_VALUE;
		for(Integer i = 0; i <= upperBound; i++){
			if(penaltyClass.get(dclass).get(i) < minPen)minPen = penaltyClass.get(dclass).get(i);
		}
		return minPen;
	}
	
	private Integer getMax() {
		Integer max = -1;
		for(PenaltyCalculator child : this.children){
			if(max < 0 || max < child.penaltyTotal.lastKey())max = child.penaltyTotal.lastKey();
		}
		return max;
	}



	
	private Integer minPenalty(String dclass, Integer i) {
		Integer minScore = 0;
		for(PenaltyCalculator child : this.children){
			Integer minScoreC = -1;
			TreeMap<Integer,Integer>  childscore = child.getPenaltyClass().get(dclass); //score for this domain and child
			for(Integer count: childscore.keySet()){
				Integer curScore = childscore.get(count) + Math.abs(i-count);
				if(minScoreC < 0 || curScore < minScoreC)minScoreC = curScore;
			}
			minScore += minScoreC;
		}
		return minScore;
	}

	
	
	private void readCounts() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(dir+"/mpcounts"));
			if(!r.ready()){
				System.err.println("Could not read from mpcounts for leave node defined by directory: "+dir);
				System.exit(0);
			}
			String line = r.readLine();
			while(r.ready()){
				line = r.readLine();
				String[] cols = line.split("\t");
				String dclass = cols[0];
				if(PenaltyCalculator.getDclasses().contains(dclass)){
					Integer count = new Integer(cols[1]);
					penaltyClass.put(dclass, new TreeMap<Integer,Integer>());
					penaltyClass.get(dclass).put(count, 0);
				}
				if(dclass.equals("total")){
					Integer count = new Integer(cols[1]);
					penaltyTotal.put(count, 0);
				}
			}
			r.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not open mpcounts for leave node defined by directory: "+dir);
			System.exit(0);
		} catch (IOException e) {
			System.err.println("Could not read from mpcounts for leave node defined by directory: "+dir);
			System.exit(0);
		}

	}
	
	
	
	
	
	
	
	
	/**
	 * @return the dir
	 */
	public String getDir() {
		return dir;
	}


	/**
	 * @return the penaltyClass
	 */
	public TreeMap<String, TreeMap<Integer, Integer>> getPenaltyClass() {
		if(penaltyClass.size() == 0)calcPenaltyClass();
		return penaltyClass;
	}


	/**
	 * @return the penaltyTotal
	 */
	public TreeMap<Integer, Integer> getPenaltyTotal() {
		if(penaltyClass.size() == 0)calcPenaltyClass();
		return penaltyTotal;
	}


	/**
	 * @return the children
	 */
	public ArrayList<PenaltyCalculator> getChildren() {
		return children;
	}


	/**
	 * @return the dclasses
	 */
	public static List<String> getDclasses() {
		return dclasses;
	}


	
}
