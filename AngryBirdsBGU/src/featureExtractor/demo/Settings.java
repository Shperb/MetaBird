package featureExtractor.demo;

import Jama.Matrix;

import java.io.*;
import java.util.ArrayList;

public class Settings
{

	private int method = 0;	 				// method {0: rbf, 1: grid, 2:linear}
	private boolean RBFnorm = true;			// normalization
	private int Regressors = 35;			// number of regressors used 
	
	// method parameters 
	private double alpha = 0.1;
	private double beta  = 1;
	private int num_of_clusters = 150;
	private int num_of_dims = 3;
	private int typeOfFeatures = 1;									// 0 for RelevantHeight, 1 for totalWeight
	private boolean dumnyBasis = false;								// use of dumny basis function or not
	private int episode = 1;											// select episode for training
	private boolean load_from_file = true;							// load parameters from files
	
	// Files needed for initialization and update
	File LevelScore;													// file to write the scores of each level
	private ArrayList<File> WFiles = new ArrayList<File>();
	private ArrayList<File> NFiles = new ArrayList<File>();
	private ArrayList<File> SFiles = new ArrayList<File>();
	
	public Settings(){
		// Files needed initialization
		this.LevelScore = new File("Scores/Scores.txt");
		InitializeWSNFiles();	
	}
		
	public int get_num_of_regressors() {
		return Regressors;
	}
	
	public boolean get_dumnyBasis() {
		return dumnyBasis;
	}
	
	public int get_num_of_clusters() {
		return num_of_clusters;
	}
	
	public int get_num_of_dims() {
		return num_of_dims;
	}
	
	public int get_rbf_flag() {
		return method;
	}
	
	public boolean get_rbf_norm() {
		return RBFnorm;
	}
	
	public int get_num_of_episode() {
		return episode;
	}
	
	public double get_alpha() {
		return alpha;
	}
	
	public double get_beta() {
		return beta;
	}
	
	public boolean get_load_from_file() {
		return load_from_file;
	}
	
	public int get_type_of_features() {
		return typeOfFeatures;
	}
	
	/*
	 * function to initialize all files needed
	 */
	private void InitializeWSNFiles(){
		
		for (int i=0; i<Regressors; i++){
			
			// initialization of files w
			String filew = new String("w"+i+".txt");
			WFiles.add(new File("WSN/" + filew));
			
			// initialization of files n
			String filen = new String("n"+i+".txt");
			NFiles.add(new File("WSN/" + filen));
			
			// initialization of files S
			String fileS = new String("S"+i+".txt");
			SFiles.add(new File("WSN/" + fileS));
		}
		
		if(!load_from_file){
			try{
				for(int i=0; i<Regressors; i++){
					if (!WFiles.get(i).exists()){
						WFiles.get(i).createNewFile();
					}
					
					if (!NFiles.get(i).exists()){
						NFiles.get(i).createNewFile();
					}
					PrintWriter inFileNI = new PrintWriter(new FileWriter(NFiles.get(i), false));
					inFileNI.println(0);
					inFileNI.close();
					inFileNI = null;
					
					if (!SFiles.get(i).exists()){
						SFiles.get(i).createNewFile();
					}
					PrintWriter inFileW = new PrintWriter(new FileWriter(WFiles.get(i), false));
					PrintWriter inFileS = new PrintWriter(new FileWriter(SFiles.get(i), false));
					
					Matrix w = new Matrix(num_of_clusters,1);
					Matrix S = new Matrix(num_of_clusters, num_of_clusters);
					
					// write to files
					w.print(inFileW,0,10);
					S.print(inFileS,0,10);
					
					inFileW.close();
					inFileS.close();
					inFileW = null;
					inFileS = null;
				}
		
			}catch(IOException e){
				// System.out.println("Cannot access file, WMatrix error ...!!!");
			}
		}	
	}

	public void WriteScoresInFile(int level, int score, int shots){
		try{
			if (LevelScore.exists() == false) {
				LevelScore.createNewFile();
			}
		
			PrintWriter inFile = new PrintWriter(new FileWriter(LevelScore,true));
			inFile.println(level + "\t" + score + "\t" + shots);
			inFile.close();
			inFile = null;
		} catch(IOException e){
			// System.out.println("PrintWriter ERROR while updating Scores.txt file...");
			// System.out.println("Exception found: " +e);
		}
	}
	
}
