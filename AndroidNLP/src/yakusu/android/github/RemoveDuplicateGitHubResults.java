package yakusu.android.github;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

public class RemoveDuplicateGitHubResults {
	
	public static void main(String[] args) {
		if(args.length!=3){
			System.out.println("usage: java -jar RemoveDuplicateGitHubResults.jar old_results.txt new_results.txt final_results.txt");
			System.exit(-1);
		}
		
		RemoveDuplicateGitHubResults generateGoals = new RemoveDuplicateGitHubResults();
		generateGoals.remove(args[0], args[1], args[2]);
	}
	
	private void remove(String oldResultsFileName, String newResultsFileName, String finalResultsFileName){		
		try{
			List<String> oldResultsList = new ArrayList<String>();
			FileInputStream oldResultsFis = new FileInputStream(oldResultsFileName);
		    DataInputStream oldResultsDis = new DataInputStream(oldResultsFis);
		    BufferedReader oldResultsBr = new BufferedReader(new InputStreamReader(oldResultsDis));
		    String line = "";
		    while ((line = oldResultsBr.readLine()) != null) {
		    	oldResultsList.add(line);
		    }
		    oldResultsBr.close();
		    
			List<String> newResultsList = new ArrayList<String>();
			FileInputStream newResultsFis = new FileInputStream(newResultsFileName);
		    DataInputStream newResultsDis = new DataInputStream(newResultsFis);
		    BufferedReader newResultsBr = new BufferedReader(new InputStreamReader(newResultsDis));
		    line = "";
		    while ((line = newResultsBr.readLine()) != null) {
		    	newResultsList.add(line);
		    }
		    newResultsBr.close();
		    
		    List<String> finalResultsList = new ArrayList<String>();
		    
		    for(String newResult:newResultsList){
		    	boolean alreadyProcessed = false;
		    	for(String oldResult:oldResultsList){
		    		if(newResult.contains(oldResult)){
		    			alreadyProcessed = true;
		    			break;
		    		}
		    	}
		    	if(!alreadyProcessed){
	    			finalResultsList.add(newResult);
		    	}
		    }
		    
		    FileWriter finalResultsFW = new FileWriter(finalResultsFileName);
		    for(String finalResult:finalResultsList){
		    	finalResultsFW.write(finalResult+"\n");
		    }
		    finalResultsFW.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
