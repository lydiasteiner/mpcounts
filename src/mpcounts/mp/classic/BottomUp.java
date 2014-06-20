package mpcounts.mp.classic;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;



public class BottomUp {
	private String dir;
	private TreeMap< String, TreeMap<Integer,Integer> > scores;
	private ArrayList<BottomUp> children;
	private ArrayList<String> dclasses;
	private boolean cooc = false;

	/**
	 * @param dir
	 */
	public BottomUp(String dir) {
		//System.err.println(dir+" without cooc");
		this.dir = dir;
		this.children = new ArrayList<BottomUp>();
		this.scores = new TreeMap< String, TreeMap<Integer,Integer> >();
		this.dclasses = new ArrayList<String>(Arrays.asList("Wac","Wme","Wph","Wub","Eac","Eme","Eub","Rac","Rme","Rph"));
	}	
	
	/**
	 * @param dir
	 */
	public BottomUp(String dir,boolean cooc) {
		this.cooc = cooc;
		this.dir = dir;
		this.children = new ArrayList<BottomUp>();
		this.scores = new TreeMap< String, TreeMap<Integer,Integer> >();
		ArrayList<String> old  = new ArrayList<String>(Arrays.asList("Wac","Wme","Wph","Wub","Eac","Eme","Eub","Rac","Rme","Rph"));
		Collections.sort(old);
		this.dclasses = new ArrayList<String> ();
		for(int i = 0; i < old.size(); i++){
			String dc1 = old.get(i);
			for(int j = i; j < old.size(); j++){
				String dc2 = old.get(j);
				String comb = dc1+"_"+dc2;
				//System.err.println(dc1+" "+dc2+" "+comb);
				this.dclasses.add(comb);
			}
		}
	}
	
	public void buildTree() {
		// read directory and create BottomUp object for each directory
		File directory = new File(this.dir);
		File[] files = directory.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if(files[i].getName().startsWith(".")){continue;}//skip hidden files
				if (files[i].isDirectory()) {
					BottomUp c;
					if(cooc)c = new BottomUp(files[i].getAbsolutePath(),cooc);
					else c = new BottomUp(files[i].getAbsolutePath());
			
					children.add(c);
				}
			}
		}
		
		//build tree for them
		for(BottomUp child :  children)child.buildTree();
	}
	
	public void calcScores(){
		System.err.println("dir "+dir);
		//for(String dc : this.getDclasses())System.err.print(dc+"|");
		//System.err.println("");
		// get real counts with score 0 for leaves
		if(this.children.size() == 0){
			readCounts();
			return;
		}
		// maximal counts of children nodes
		TreeMap<String, Integer> max = getMax();
		for(String dclass : max.keySet()){
			scores.put(dclass, new TreeMap<Integer,Integer>());
			System.err.println(dclass+"\t"+max.get(dclass));
			for(Integer i = 0; i <= max.get(dclass); i++){
				scores.get(dclass).put(new Integer(i), minScore(dclass,i));
			}
		}

	}

	private Integer minScore(String dclass, Integer i) {
		Integer minScore = 0;
		for(BottomUp child : this.children){
			Integer minScoreC = -1;
			TreeMap<Integer,Integer>  childscore = child.getScores().get(dclass); //score for this domain and child
			for(Integer count: childscore.keySet()){
				Integer curScore = childscore.get(count) + Math.abs(i-count);
				if(minScoreC < 0 || curScore < minScoreC)minScoreC = curScore;
			}
			minScore += minScoreC;
		}
		return minScore;
	}

	private TreeMap<String, Integer> getMax() {
		TreeMap<String,Integer> max = new TreeMap<String,Integer>();
		for(BottomUp child : children){
			TreeMap<String, TreeMap<Integer,Integer> > childmap = child.getScores(); 
			for(String dclass : this.getDclasses()){
				//System.err.println(dir+"\t"+dclass);
				Integer childMax = childmap.get(dclass).navigableKeySet().last();
				if(!max.containsKey(dclass) || max.get(dclass) < childMax){
					max.put(dclass, childMax);
				}
			}
		}
		return max;
	}
	


	public TreeMap<String, TreeMap<Integer, Integer>> getScores() {
		if(scores.isEmpty())calcScores();
		return scores;
	}

	private void readCounts() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(dir+"/mpcounts"));
			if(!r.ready()){
				System.err.println("Could not read from mpcounts for leave node defined by directory: "+dir);
				System.exit(0);
			}
//			String line = r.readLine();
			while(r.ready()){
				String line = r.readLine();
				String[] cols = line.split("\t");
				String dclass = cols[0];
				if(this.getDclasses().contains(dclass)){
					Integer count = new Integer(cols[1]);
					scores.put(dclass, new TreeMap<Integer,Integer>());
					scores.get(dclass).put(count, new Integer(0));
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

	public String getDir() {
		return dir;
	}

	public ArrayList<BottomUp> getChildren() {
		return children;
	}

	public List<String>  getDclasses() {
		return dclasses;
	}


}
