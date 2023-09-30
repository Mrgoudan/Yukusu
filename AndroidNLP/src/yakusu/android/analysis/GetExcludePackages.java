package yakusu.android.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GetExcludePackages {

	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("usage: java -jar GetExcludedPackages.jar source_root package1,package2,package3");
			System.exit(-1);
		}
		
		String sourceRoot = args[0];
		String includes = args[1];
		String[] includesArray = includes.split(",");
		Set<String> includesSet = new HashSet<String>();
		for(String include:includesArray){
			if(include!=null && !includesSet.equals("")){
				includesSet.add(include);
			}
		}
		File sourceRootFolder = new File(sourceRoot);
		List<File> workList = new ArrayList<File>();
		workList.add(sourceRootFolder);
		Set<String> candidateSet = new HashSet<String>();
		String rootAbsPath = sourceRootFolder.getAbsolutePath();
		while(!workList.isEmpty()){
			File currFile = workList.remove(0);
			String absPath = currFile.getAbsolutePath();
			//process root differently
			if(absPath.equals(rootAbsPath)){
				File[] containedFilesArray = currFile.listFiles();
				for(File containedFile:containedFilesArray){
					if(containedFile.isDirectory()){
						workList.add(containedFile);
					}
				}
				continue;
			}
			//do not process paths that do not contain smali
			if(!absPath.contains("smali")){
				continue;
			}
//			//do not process unless it contains a non directory
//			boolean process = false;
//			File[] containedFilesArray = currFile.listFiles();
//			for(File containedFile:containedFilesArray){
//				if(containedFile.isHidden()){
//					continue;
//				}
//				if(!containedFile.isDirectory()){
//					process = true;
//				}
//			}
//			if(process){
				//get rel path from after smali
				String relPath = absPath.substring(absPath.indexOf("smali")+5);
				if(relPath.indexOf("/")!=-1){
					relPath = relPath.substring(relPath.indexOf("/")+1);
					relPath = relPath.replaceAll("/", ".");
					boolean discard = false;
					for(String include:includesSet){
						if(relPath.startsWith(include) || include.startsWith(relPath)){
							discard = true;
							break;
						}
					}
					if(!discard){
						candidateSet.add(relPath);
					}
				}
//			}
			File[] containedFilesArray = currFile.listFiles();
			//add contained folders in the next
			for(File containedFile:containedFilesArray){
				if(containedFile.isDirectory()){
					workList.add(containedFile);
				}
			}
		}
//		for(String result:candidateSet){
//			System.out.println(result);
//		}
		
		Set<String> discardedSet = new HashSet<String>();
		for(String candidate:candidateSet){
			boolean discard = false;
			for(String inner:candidateSet){
				if(candidate.equals(inner)){
					continue;
				}
				if(candidate.startsWith(inner)){
					discard = true;
					break;
				}
			}
			if(discard){
				discardedSet.add(candidate);
			}
		}
		Set<String> resultSet = new HashSet<String>();
		for(String candidate:candidateSet){
			if(!discardedSet.contains(candidate)){
				resultSet.add(candidate);
			}
		}
		
		String include = "";
		for(String result:includesSet){
			include = include + ","+result+".*";
		}
		if(resultSet.size()>0){
			include = include.substring(1);
		}
		System.out.println(include);
		
		String exclude = "";
		for(String result:resultSet){
			exclude = exclude + ","+result+".*";
		}
		if(resultSet.size()>0){
			exclude = exclude.substring(1);
		}
		System.out.println(exclude);
		
	}

}
