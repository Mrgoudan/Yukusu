package yakusu.android.nlp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ie.NumberNormalizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.naturalli.OpenIE;
import edu.stanford.nlp.naturalli.SentenceFragment;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class GenerateGoals {
	
	private Random rand;
	private int MAX_REPEAT = 10;
	private int MIN_REPEAT = 5;
	private double MIN_SENTENCE_SIMILARITY_SCORE = 0.5;//0.7 might be better because 0.5 is too general (keep this same as the one for search)
	private String TEXT_SERVER_ADDRESS = "";
	private String APP_NAME="";
	private String APP_PACKAGE_NAME="";
	private int LEVEL = 2;
	private long SERVER_TIME = 0;
	
	public static void main(String[] args) {
		if(args.length!=5){
			System.out.println("usage: java -jar GenerateGoals.jar text.txt config.json ontology.json string.json goals.json");
			System.exit(-1);
		}
		
		GenerateGoals generateGoalsNew = new GenerateGoals();
		generateGoalsNew.generateGoals(args[0], args[1], args[2], args[3], args[4]);
	}
	
	
	public GenerateGoals(){
		this.rand = new Random();
	}
	
	//return elements in ontology that could be a preference
	private Set<String> returnPreferences(List<Ontology> ontologyList){
		Set<String> preferencesSet = new HashSet<String>();
		for(int i=0; i<ontologyList.size(); ++i){
			Ontology viewOntology = ontologyList.get(i);
			if(viewOntology.getClassName().toLowerCase().contains("preference")){
				preferencesSet.add(viewOntology.getText());
			}
		}
		return preferencesSet;
	}
	
	//replace name of app with generic app
	private String replaceAppName(String text, String appName){
		if(appName.equals("")){
			return text;
		}
		boolean changed = true;
		while(changed){
			changed = false;
			//tokenize and split text
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			int textIndexBegin = 0;
			while(true){
				if(textIndexBegin>=text.length()){
					break;
				}
				textIndexBegin = text.toLowerCase().indexOf(appName.toLowerCase(), textIndexBegin);
				if(textIndexBegin == -1){
					//did not find string in text
					break;
				}
				else{
					//check if it is a valid match
					int beginIndex = textIndexBegin;
					int endIndex = beginIndex + appName.toLowerCase().length(); 
					List<CoreLabel> tokenList = annotation.get(CoreAnnotations.TokensAnnotation.class);
					boolean matchedBeginIndex = false;
					boolean matchedEndIndex = false;
					for(CoreLabel token:tokenList){
						if(token.beginPosition()==beginIndex){
							matchedBeginIndex = true;
						}
						if(token.endPosition()==endIndex){
							matchedEndIndex = true;
						}
					}
					if(matchedBeginIndex && matchedEndIndex){
						//found a valid match
						//replace app name with app
						text = text.substring(0, beginIndex) + "app" + text.substring(endIndex);
						changed = true;
						break;
					}
					else{
						//did not find a valid match
						textIndexBegin = textIndexBegin + 1; 
						continue;
					}
				}
			}
		}
		return text;
	}
	
	//replace text from ontology so it becomes easier to analyze
	private String replaceOntology(String text, List<Ontology> ontologyList, Map<String, String> replacementMap){
		boolean changed = true;
		while(changed){
			changed = false;
			//tokenize and split text
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			for(Ontology ontology:ontologyList){
				String ontologyText = ontology.getText();
				if(ontologyText.equals("")){
					//mf: not interested in empty strings
					continue;
				}
				if(Character.isLowerCase(ontologyText.charAt(0)) || Character.isDigit(ontologyText.charAt(0))){
					//mf: not interested in ontology that starts with lower case or is a number (heuristic) 
					continue;
				}
				int textIndexBegin = 0;
				while(true){
					if(textIndexBegin>=text.length()){
						break;
					}
					textIndexBegin = text.indexOf(ontologyText, textIndexBegin);
					if(textIndexBegin == -1){
						//did not find string in text
						break;
					}
					else{
						//check if it is a valid match
						int beginIndex = textIndexBegin;
						int endIndex = beginIndex + ontologyText.length(); 
						List<CoreLabel> tokenList = annotation.get(CoreAnnotations.TokensAnnotation.class);
						boolean matchedBeginIndex = false;
						boolean matchedEndIndex = false;
						for(CoreLabel token:tokenList){
							if(token.beginPosition()==beginIndex){
								matchedBeginIndex = true;
							}
							if(token.endPosition()==endIndex){
								matchedEndIndex = true;
							}
						}
						if(matchedBeginIndex && matchedEndIndex){
							//found a valid match
							//generate element string
							String elementString = WordUtils.ELEMENT_INDEX+"_"+WordUtils.ELEMENT_STRING;
							WordUtils.ELEMENT_INDEX++;
							replacementMap.put(elementString, ontologyText);
							text = text.substring(0, beginIndex) + elementString + text.substring(endIndex);
//							//mf: logging
//							System.out.println("begin:"+beginIndex);
//							System.out.println("end:"+endIndex);
//							System.out.println("ontology:"+ontologyText);
//							System.out.println("text:"+text);
							changed = true;
							break;
						}
						else{
							//did not find a valid match
							textIndexBegin = textIndexBegin + 1; 
							continue;
						}
					}
				}
				if(changed){
					//restart from the beginning
					break;
				}
			}
		}
		return text;
	}
	
	
	//replace text within quotes so it becomes easier to analyze
	private String replaceQuotes(String text, Map<String, String> replacementMap){
		boolean changed = true;
		while(changed){
			changed = false;
			//take care of quotes that match ontology
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, quote");
			props.put("quote.singleQuotes","true");
			props.put("quote.asciiQuotes", "true");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			List<CoreMap> quoteList = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
			for(CoreMap quote:quoteList){
				String quoteText = quote.get(CoreAnnotations.TextAnnotation.class);
				int beginIndex = quote.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
				int endIndex = quote.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
				if(quoteText.contains("_"+WordUtils.ELEMENT_STRING)){
					//do no replace the text
					text = text.substring(0, beginIndex) + quoteText.substring(1, quoteText.length()-1) + text.substring(endIndex);
//					//mf: logging
//					System.out.println("beginIndex:"+beginIndex);
//					System.out.println("endIndex:"+endIndex);
//					System.out.println("quote:"+quoteText);
//					System.out.println("newText1:"+text);
					changed = true;
					break;
				}
				else{
					//replace text with quote
					String quoteString = WordUtils.QUOTE_INDEX+"_"+WordUtils.QUOTE_STRING;
					WordUtils.QUOTE_INDEX++;
					String quoteTextWithoutQuotes = quoteText.substring(1, quoteText.length()-1);
					text = text.substring(0, beginIndex) + quoteString + text.substring(endIndex);
					replacementMap.put(quoteString, quoteTextWithoutQuotes);
//					//mf: logging
//					System.out.println("beginIndex:"+beginIndex);
//					System.out.println("endIndex:"+endIndex);
//					System.out.println("quote:"+quoteText);
//					System.out.println("newText2:"+text);
					changed = true;
					break;
				}
			}
		}
		return text;
	}
	
	//replace tokens to improve analysis (e.g., press becomes click)
	private String replaceTokens(String text){
		boolean changed = true;
		while(changed){
			changed = false;
			//take care of quotes that match ontology
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			//check all tokens than need to be replaced
			List<CoreMap> sentenceList = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			for(CoreMap sentence : sentenceList){
				for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
					String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
					String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
					int beginIndex = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
					int endIndex = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
					String replacementLemma = WordUtils.replaceToken(lemma, pos);
					if(!replacementLemma.equals("")){						
						text = text.substring(0, beginIndex) + replacementLemma + text.substring(endIndex);
//						//mf: logging
//						System.out.println("lemma:"+lemma);
//						System.out.println("begin:"+beginIndex);
//						System.out.println("end:"+endIndex);
						changed = true;
						break;
					}
					if(lemma.toLowerCase().equals("load")){
						int tokenIndex = token.index();
						String nextTokenLemma = "";
						for (CoreLabel tokenInner: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
							if(tokenInner.index()==(tokenIndex+1)){
								nextTokenLemma = tokenInner.lemma();
								break;
							}
						}
						if(nextTokenLemma.toLowerCase().equals("app")){
							text = text.substring(0, beginIndex) + "install" + text.substring(endIndex);
							changed = true;
							break;
						}
					}
				}
				if(changed){
					break;
				}
			}
		}
		return text;
	}
	
	
	//filter text in parenthesis
	private String removeBrackets(String text){
			//replace parenthesis text
			String result = "";
			boolean finished=false;
			while(!finished){
				finished=true;
				Properties props = new Properties();
				props.put("annotators", "tokenize, ssplit, pos, lemma");
				StanfordCoreNLP core = new StanfordCoreNLP(props);
				Annotation annotation = new Annotation(text);
				core.annotate(annotation);
				//get sentences
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				//process each sentence
				for(CoreMap sentence : sentences){
					//process each token in sentence
					int beginIndex = -1;
					int endIndex = -1;
					for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
						//get lemma of token
						String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
						if(lemma.equals("-lrb-") && beginIndex==-1){//get first round bracket
							beginIndex = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).intValue();
						}
						if(lemma.equals("-rrb-")){//get last round bracket
							endIndex = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).intValue();
						}
					}
					if(beginIndex != -1 && endIndex !=-1){
						text = text.substring(0, beginIndex) + text.substring(endIndex+1);
						finished = false;
					}
					else if(beginIndex != -1){
						text = text.substring(0, beginIndex) + text.substring(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class).intValue()-1);	
						finished = false;
					}
					else if(endIndex != -1){
						text = text.substring(0, sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).intValue()) + text.substring(endIndex+1);
						finished = false;
					}
					if(!finished){
						break;
					}
				}
			}
			result = text;
			return result;
	}
	
	//remove empty sentence
	private String removeEmptySentence(String text){
			//replace parenthesis text
			String result = "";
			boolean finished=false;
			while(!finished){
				finished=true;
				Properties props = new Properties();
				props.put("annotators", "tokenize, ssplit, pos, lemma");
				StanfordCoreNLP core = new StanfordCoreNLP(props);
				Annotation annotation = new Annotation(text);
				core.annotate(annotation);
				//get sentences
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				//process each sentence
				for(CoreMap sentence : sentences){
					int tokenCount = 0;
					for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
						String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
						if(lemma.equals(".")){
							continue;
						}
						tokenCount++;
					}
					if(tokenCount==0){
						text = text.substring(0, sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).intValue()) + text.substring(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class).intValue());
						finished=false;
					}
					if(!finished){
						break;
					}
				}
			}
			result = text;
			return result;
	}
	
	//transform arrows to sentences
	private String replaceArrows(String text){
		String result = text;
		//replace > with period
		result = result.replace("->", ".");
		result = result.replace("=>", ".");
		result = result.replace(">", ".");
		result = result.replace("<-", ".");
		result = result.replace("<--", ".");
		return result;
	}
	
	//convert apposition commas into and for better analysis
	private String changePunctuationSentence(String text){
		boolean changed = true;	
		//change comma into and when suitable
		while(changed){
			changed = false;
			//take care of quotes that match ontology
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			//check all tokens than need to be replaced
			List<CoreMap> sentenceList = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			for(CoreMap sentence : sentenceList){
				SemanticGraph sentenceDependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
				Set<IndexedWord> vertexSubgraphSet= sentenceDependencies.getSubgraphVertices(sentenceDependencies.getFirstRoot());
				//check for appositions
				for(IndexedWord currVertex:vertexSubgraphSet){
					List<SemanticGraphEdge> apposEdgeList = getIncomingNamedEdge(sentenceDependencies, currVertex, "appos", "");
					if(apposEdgeList.size()==1){
						int currVertexIndex = currVertex.index();
						//check if preceding token is punct
						for(IndexedWord currVertexInner:vertexSubgraphSet){
							if(currVertexInner.index()==(currVertexIndex-1)){
								List<SemanticGraphEdge> punctEdgeList = getIncomingNamedEdge(sentenceDependencies, currVertexInner, "punct", "");
								if(punctEdgeList.size()==1){
									int beginIndex = currVertexInner.beginPosition();
									int endIndex = currVertexInner.endPosition();
									text = text.substring(0, beginIndex) + " and " + text.substring(endIndex+1);									
//									System.out.println("*******************punctuation change*********************");
//									System.out.println(text);
									changed = true;
									break;
								}
							}
						}
						if(changed){
							break;
						}
					}
				}
				if(changed){
					break;
				}
			}
		}		
		return text;
	}
	
	//repeat a verb in the case a clause is missing
	private String repeatMissingVerb(String text){
		boolean changed = true;	
		//change comma into and when suitable
		while(changed){
			changed = false;
			//take care of quotes that match ontology
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(text);
			core.annotate(annotation);
			//check all tokens than need to be replaced
			List<CoreMap> sentenceList = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			for(CoreMap sentence : sentenceList){
				SemanticGraph sentenceDependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
				Set<IndexedWord> vertexSubgraphSet= sentenceDependencies.getSubgraphVertices(sentenceDependencies.getFirstRoot());
				//check for appositions
				for(IndexedWord currVertex:vertexSubgraphSet){
					if(!currVertex.tag().startsWith("N")){
						continue;
					}
					List<SemanticGraphEdge> conjEdgeList = getIncomingNamedEdge(sentenceDependencies, currVertex, "conj", "and");
					if(conjEdgeList.size()==1){
						IndexedWord conjGovernorVertex = conjEdgeList.get(0).getGovernor();
						List<SemanticGraphEdge> governorIncomingEdgeList = new ArrayList<SemanticGraphEdge>();
						governorIncomingEdgeList.addAll(getIncomingNamedEdge(sentenceDependencies, conjGovernorVertex, "dobj", ""));
						governorIncomingEdgeList.addAll(getIncomingNamedEdge(sentenceDependencies, conjGovernorVertex, "xcomp", ""));
						governorIncomingEdgeList.addAll(getIncomingNamedEdge(sentenceDependencies, conjGovernorVertex, "nmod", ""));
						if(governorIncomingEdgeList.size()==1 && governorIncomingEdgeList.get(0).getGovernor().tag().startsWith("V")){
							String verbOriginalText = governorIncomingEdgeList.get(0).getGovernor().originalText();
							int beginIndex = Integer.MAX_VALUE;
							Set<IndexedWord> currVertexSubgraphSet= sentenceDependencies.getSubgraphVertices(currVertex);
							for(IndexedWord subgraphVertex:currVertexSubgraphSet){
								if(subgraphVertex.beginPosition()<beginIndex){
									beginIndex = subgraphVertex.beginPosition();
								}
							}
							text = text.substring(0, beginIndex-1) + " " + verbOriginalText + " " + text.substring(beginIndex);
//							System.out.println("*******************missing verb change*********************");
//							System.out.println(text);
							changed = true;
							break;
						}		
					}
				}
				if(changed){
					break;
				}
			}
		}		
		return text;
	}
	
	private String replaceCoreferences(String text){
		boolean changed = true;	
		//change comma into and when suitable
		while(changed){
			changed = false;
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, coref");
			props.put("coref.algorithm", "neural");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			Annotation doc = new Annotation(text);
			pipeline.annotate(doc);
			//get coref chains
			Map<Integer, CorefChain> corefs = doc.get(CorefChainAnnotation.class);
			//get sentences
			List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
			//process each sentence
			for (CoreMap sentence : sentences) {
				List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
				for (CoreLabel token : tokens) {
					Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
					CorefChain chain = corefs.get(corefClustId);
					if(chain!=null){
						//System.out.println(chain);
						int sentINdx = chain.getRepresentativeMention().sentNum -1;
						CoreMap corefSentence = sentences.get(sentINdx);
						List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);
						CorefMention reprMent = chain.getRepresentativeMention();
						String headWord = "";
						//avoids matching the same mention
						boolean same = false;
						if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {
							for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
								CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
								headWord = headWord + " " + matchedLabel.originalText();
								if(i==reprMent.headIndex){
									if( matchedLabel.lemma().equals(token.lemma())){
										same = true;
									}
								}
							}
						}
						headWord = headWord.trim();
						if(!headWord.equals("") && !headWord.equals(token.originalText()) && !same){
							//change text here  
							int beginIndex = token.beginPosition();
							int endIdnex = token.endPosition();
							text = text.substring(0, beginIndex) + headWord + text.substring(endIdnex);
//							System.out.println("*******************coreference change*********************");
//							System.out.println(text);
							changed = true;
							break;
						}
					}
				}
				if(changed){
					break;
				}
			}
		}
		return text;
	}
	
	
	public void generateGoals(String textFileName, String configFileName, String ontologyFileName, String stringFileName, String goalFileName){
		try{
			System.out.println("************************************************************************");
			long startAnalysisTime = System.currentTimeMillis();
			//read text
			String text = "";
			FileInputStream textFis = new FileInputStream(textFileName);
		    DataInputStream textDis = new DataInputStream(textFis);
		    BufferedReader textBr = new BufferedReader(new InputStreamReader(textDis));
		    String line = "";
		    while ((line = textBr.readLine()) != null) {
		    	text = text + line;
		    }
		    textBr.close();
			System.out.println("text:"+text);
			
		    //read string
			String configContent = "";
			FileInputStream configFis = new FileInputStream(configFileName);
		    DataInputStream configDis = new DataInputStream(configFis);
		    BufferedReader configBr = new BufferedReader(new InputStreamReader(configDis));
		    line = "";
		    while ((line = configBr.readLine()) != null) {
		    	configContent = configContent + line;
		    }		   
		    configBr.close();
		    JSONObject configJSON = new JSONObject(configContent);
		    TEXT_SERVER_ADDRESS = configJSON.getString("text_server_address");
			MIN_REPEAT = configJSON.getInt("min_goal_repeat");
			MAX_REPEAT = configJSON.getInt("max_goal_repeat");
			MIN_SENTENCE_SIMILARITY_SCORE = configJSON.getDouble("min_sentence_similarity_score");
			APP_NAME = configJSON.getString("app_name");
			APP_PACKAGE_NAME = configJSON.getString("app_package_name");
			LEVEL = configJSON.getInt("level");
			
			
			
		    //read ontology
			String ontologyContent = "";
			FileInputStream ontologyFis = new FileInputStream(ontologyFileName);
		    DataInputStream ontologyDis = new DataInputStream(ontologyFis);
		    BufferedReader ontologyBr = new BufferedReader(new InputStreamReader(ontologyDis));
		    line = "";
		    while ((line = ontologyBr.readLine()) != null) {
		    	ontologyContent = ontologyContent + line;
		    }		   
		    ontologyBr.close();
		    JSONObject ontologyJSON = new JSONObject(ontologyContent);
		    JSONArray ontologyJSONArray = ontologyJSON.getJSONArray("ontology");
		    Utils.handleStaticAnalysisLimitations(APP_PACKAGE_NAME, ontologyJSONArray);
		    List<Ontology> ontologyList = new ArrayList<Ontology>();
		    for(int i=0; i<ontologyJSONArray.length(); ++i){
		    	ontologyList.add(new Ontology(ontologyJSONArray.getJSONObject(i)));
		    }
			//order and rever
		    Collections.sort(ontologyList);
		    Collections.reverse(ontologyList);
		    
		    
		    //read string
			String stringContent = "";
			FileInputStream stringFis = new FileInputStream(stringFileName);
		    DataInputStream stringDis = new DataInputStream(stringFis);
		    BufferedReader stringBr = new BufferedReader(new InputStreamReader(stringDis));
		    line = "";
		    while ((line = stringBr.readLine()) != null) {
		    	stringContent = stringContent + line;
		    }		   
		    stringBr.close();
		    JSONObject stringJSON = new JSONObject(stringContent);
		    JSONArray stringJSONArray = stringJSON.getJSONArray("string");
		    List<String> stringList = new ArrayList<String>();
		    for(int i=0; i<stringJSONArray.length(); ++i){
		    	stringList.add(stringJSONArray.getString(i));
		    }
		    
		    Map<String, String> replacementMap = new HashMap<String, String>();
		    Set<String> preferenceSet = returnPreferences(ontologyList);
		    
		    //list of goals
		    List<Goal> goalsList = new ArrayList<Goal>();
		    
		    text = replaceAppName(text, APP_NAME);
		    System.out.println("text after app name replacement:"+text);
		    
		    text = replaceOntology(text, ontologyList, replacementMap);
		    System.out.println("text after ontology replacement:"+text);
		    
		    text = replaceQuotes(text, replacementMap);
		    System.out.println("text after quote replacement:"+text);
		    
		    text = replaceTokens(text);
		    System.out.println("text after token replacement:"+text);
		    
		    //remove parenthesis
		    text = removeBrackets(text);
		    text = removeEmptySentence(text);
		    System.out.println("text after bracket removal:"+text);
		    
		    //translate arrows into new sentences
		    text = replaceArrows(text);
		    text = removeEmptySentence(text);
		    System.out.println("text after arrow replacement:"+text);

		    //optional
		    //text = replaceCoreferences(text);
		    System.out.println("text after coreference resolution:"+text);
		    //change punctuation
		    text = changePunctuationSentence(text);
		    System.out.println("text after changing punctation:"+text);
		    //repeat missing verb
		    text = repeatMissingVerb(text);
		    System.out.println("text after replacing missing verb:"+text);
		    
		    
		    System.out.println("analysis text:"+text);
		    
		    long endPreprocessingTime = System.currentTimeMillis();
		    long preprocessingTime = endPreprocessingTime - startAnalysisTime;
		    
		    
		    //sentences ready to be processed
			Properties depProps = new Properties();
			depProps.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP depPipeline = new StanfordCoreNLP(depProps);
			Annotation depAnnotation = new Annotation(text);
			depPipeline.annotate(depAnnotation);
			
			//extract clauses
		    OpenIE openIE = new OpenIE();
		    for (CoreMap sentence : depAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
		    	List<Goal> sentenceGoalList = processSentence(openIE, sentence, replacementMap, preferenceSet, ontologyList, stringList, LEVEL);
		    	goalsList.addAll(sentenceGoalList);
		    }
		    
			//mf: write goals file
			JSONArray goalsJSONArray = new JSONArray();
			for(Goal goal:goalsList){
				goalsJSONArray.put(goal.toJSON());
			}
			JSONObject goalsJSON = new JSONObject();
			goalsJSON.put("goals",goalsJSONArray);
		    FileWriter statesFileWriter = new FileWriter(goalFileName);
		    statesFileWriter.write(goalsJSON.toString());
		    statesFileWriter.close();
		    
		    long endAnalysisTime = System.currentTimeMillis();
		    long analysisTime = endAnalysisTime - startAnalysisTime;
		    System.out.println("analysis time:"+analysisTime);
		    System.out.println("preprocessing time:"+preprocessingTime);
		    System.out.println("server time:"+SERVER_TIME);
		    System.out.println("goals num:"+goalsList.size());
		    int generalGoalCount = 0;
		    for(Goal goal:goalsList){
		    	if(goal instanceof GoalClickGeneral){
		    		generalGoalCount++;
		    	}
		    }
		    System.out.println("generic goals num:"+generalGoalCount);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//get incoming edges from vertex
	private List<SemanticGraphEdge> getIncomingNamedEdge(SemanticGraph dependencies, IndexedWord vertex,  String edgeName, String specific){
		List<SemanticGraphEdge> result =new ArrayList<SemanticGraphEdge>();
		List<SemanticGraphEdge> vertexEdgeList = dependencies.getIncomingEdgesSorted(vertex);
		for(SemanticGraphEdge vertexEdge:vertexEdgeList){
			if(vertexEdge.getRelation().getShortName().equals(edgeName)){
				if(specific.equals("")){
					result.add(vertexEdge);
				}
				else{
					if(vertexEdge.getRelation().getSpecific()!=null && vertexEdge.getRelation().getSpecific().equals(specific)){
						result.add(vertexEdge);
					}
				}
			}
		}
		return result;
	}
	
	//get outgoing edges from vertex
	private List<SemanticGraphEdge> getOutgoingNamedEdge(SemanticGraph dependencies, IndexedWord vertex,  String edgeName, String specific){
		List<SemanticGraphEdge> result =new ArrayList<SemanticGraphEdge>();
		List<SemanticGraphEdge> vertexEdgeList = dependencies.getOutEdgesSorted(vertex);
		for(SemanticGraphEdge vertexEdge:vertexEdgeList){
			if(vertexEdge.getRelation().getShortName().equals(edgeName)){
				if(specific.equals("")){
					result.add(vertexEdge);
				}
				else{
					if(vertexEdge.getRelation().getSpecific()!=null){
						if(vertexEdge.getRelation().getSpecific().equals(specific)){
							result.add(vertexEdge);
						}
					}
					else{
						result.add(vertexEdge);
					}
				}
			}
		}
		return result;
	}
	
	//get the order in which clauses should be processed
	private List<SentenceFragment> orderClauses(OpenIE openIE, CoreMap sentence){
		List<IndexedClause> orderedClauseList = new ArrayList<IndexedClause>();
		List<SentenceFragment> clauseList = openIE.clausesInSentence(sentence);
		for(SentenceFragment clause:clauseList){
			int index = clause.parseTree.getFirstRoot().index();
			orderedClauseList.add(new IndexedClause(index, clause));
		}
//		//mf: logging
//		System.out.println("#####");
//		for(IndexedClause indexedClause:orderedClauseList){
//			System.out.println(indexedClause.getClause().score+"#"+indexedClause.getClause()+"#"+indexedClause.getClause().parseTree.getFirstRoot().originalText());
//		}
		
		//sort based on root token
		Collections.sort(orderedClauseList);
		
		//find after/before relation and order clauses accordingly
		SemanticGraph sentenceDependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
		Set<IndexedWord> vertexSubgraphSet= sentenceDependencies.getSubgraphVertices(sentenceDependencies.getFirstRoot());
		for(IndexedWord currVertex:vertexSubgraphSet){
			List<SemanticGraphEdge> advclEdgeList = getIncomingNamedEdge(sentenceDependencies, currVertex, "advcl", "");
			if(advclEdgeList.size()==1){
				List<SemanticGraphEdge> markEdgeList = getOutgoingNamedEdge(sentenceDependencies, currVertex, "mark", "");
				if(markEdgeList.size()==1){
					SemanticGraphEdge advclEdge = advclEdgeList.get(0);
					SemanticGraphEdge markEdge = markEdgeList.get(0);
					String markLemma = markEdge.getDependent().lemma();
					//get index of sentences that depend on the advcl relation
					List<Integer> advclDependentSubtreeRootList = new ArrayList<Integer>();
					Set<IndexedWord> dependentVertexSubgraphSet = sentenceDependencies.getSubgraphVertices(currVertex);
					for(IndexedWord dependentVertex:dependentVertexSubgraphSet){
						for(IndexedClause indexedClause:orderedClauseList){
							if(dependentVertex.index()==indexedClause.getIndex()){
								//System.out.println("here1:"+dependentVertex.originalText());
								advclDependentSubtreeRootList.add(dependentVertex.index());
								break;
							}
						}
					}
					//get index of sentences that govern the advcl relation
					List<Integer> advclGovernorSubtreeRootList = new ArrayList<Integer>();
					Set<IndexedWord> governorVertexSubgraphSet = sentenceDependencies.getSubgraphVertices(advclEdge.getGovernor());
					for(IndexedWord governorVertex:governorVertexSubgraphSet){
						if(advclDependentSubtreeRootList.contains(governorVertex.index())){
							continue;
						}
						for(IndexedClause indexedClause:orderedClauseList){
							if(governorVertex.index()==indexedClause.getIndex()){
								//System.out.println("here2:"+governorVertex.originalText());
								advclGovernorSubtreeRootList.add(governorVertex.index());
								break;
							}
						}
					}
					if(WordUtils.isBeforeLemma(markLemma)){
						//governor needs to go before dependent
						int firstDependentRootValue = -1;
						for(IndexedClause indexedClause:orderedClauseList){
							if(advclDependentSubtreeRootList.contains(indexedClause.getIndex())){
								firstDependentRootValue = indexedClause.getIndex();
								break;
							}
						}
						
						//remove governor clauses
						List<IndexedClause> governorIndexedClauseList = new ArrayList<IndexedClause>();
						for(Integer governorRootIndex:advclGovernorSubtreeRootList){
							for(int i=0; i<orderedClauseList.size(); ++i){
								IndexedClause indexedClause = orderedClauseList.get(i);
								if(governorRootIndex.intValue()==indexedClause.getIndex()){
									governorIndexedClauseList.add(orderedClauseList.remove(i));
									break;
								}
							}
						}
						//add governor clauses before first dependent
						for(int i=0; i<orderedClauseList.size(); ++i){
							if(orderedClauseList.get(i).getIndex()==firstDependentRootValue){
								orderedClauseList.addAll(i, governorIndexedClauseList);
								break;
							}
						}					
					}
					else if(WordUtils.isAfterLemma(markLemma)){
						//dependent needs to go before governor
						int firstGovernorRootValue = -1;
						for(IndexedClause indexedClause:orderedClauseList){
							if(advclGovernorSubtreeRootList.contains(indexedClause.getIndex())){
								firstGovernorRootValue = indexedClause.getIndex();
								break;
							}
						}
						
						//remove dependent clauses
						List<IndexedClause> dependentIndexedClauseList = new ArrayList<IndexedClause>();
						for(Integer dependentRootIndex:advclDependentSubtreeRootList){
							for(int i=0; i<orderedClauseList.size(); ++i){
								IndexedClause indexedClause = orderedClauseList.get(i);
								if(dependentRootIndex.intValue()==indexedClause.getIndex()){
									dependentIndexedClauseList.add(orderedClauseList.remove(i));
									break;
								}
							}
						}
						//add dependent clauses before first governor
						for(int i=0; i<orderedClauseList.size(); ++i){
							if(orderedClauseList.get(i).getIndex()==firstGovernorRootValue){
								orderedClauseList.addAll(i, dependentIndexedClauseList);
								break;
							}
						}	
					}
					//accept only one order change
					break;
				}
			}
		}
		
//		System.out.println("#####");
//		for(IndexedClause indexedClause:orderedClauseList){
//			System.out.println(indexedClause.getClause().score+"#"+indexedClause.getClause()+"#"+indexedClause.getClause().parseTree.getFirstRoot().originalText());
//		}
		
		List<SentenceFragment> resultList = new ArrayList<SentenceFragment>();
		for(IndexedClause indexedClause:orderedClauseList){
			SentenceFragment clause = indexedClause.getClause();
			resultList.add(clause);
		}
		return resultList;
	}
	
	//return words based on tag
	private List<IndexedWord> keepWordBasedOnList(List<IndexedWord> indexedWordList, List<String> keepTagList){
		//set of words refined by tag
		List<IndexedWord> resultList = new ArrayList<IndexedWord>();
		for(IndexedWord indexedWord:indexedWordList){
			boolean keep = false;
			for(String tag:keepTagList){
				if(indexedWord.tag().startsWith(tag)){
					keep = true;
					break;
				}
			}
			if(keep){
				resultList.add(indexedWord);
			}
		}
		return resultList;
	}
	
	//check if word srefer to the app
	private boolean wordsReferToApp(List<IndexedWord> indexedWordList){
		boolean result = false;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		if(analysisWordList.size()==1 && analysisWordList.get(0).lemma().toLowerCase().equals("app")){
			result = true;
			return result;
		}
		if(analysisWordList.size()==2 && analysisWordList.get(0).lemma().toLowerCase().equals("into") && analysisWordList.get(0).lemma().toLowerCase().equals("app")){
			result = true;
			return result;
		}
		return result;
	}
	
	private boolean wordsReferToMenu(List<IndexedWord> indexedWordList){
		boolean result = false;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		if(analysisWordList.size()==1 && analysisWordList.get(0).lemma().toLowerCase().equals("menu")){
			result = true;
			return result;
		}
		if(analysisWordList.size()==1 && analysisWordList.get(0).lemma().toLowerCase().equals("option")){
			result = true;
			return result;
		}
		if(analysisWordList.size()==2 && analysisWordList.get(0).lemma().toLowerCase().equals("menu") && analysisWordList.get(1).lemma().toLowerCase().equals("button")){
			result = true;
			return result;
		}
		boolean isThree = false;
		boolean isDot = false;
		boolean isOption = false;
		boolean isMenu = false;
		boolean isPreference = false;
		boolean isDialogue = false;
		for(IndexedWord analysisWord:analysisWordList){
			String lemma = analysisWord.lemma().toLowerCase();
			if(lemma.equals("three")){
				isThree = true;
			}
			else if(lemma.equals("dot")){
				isDot = true;
			}
			else if(lemma.equals("option")){
				isOption = true;
			}
			else if(lemma.equals("menu")){
				isMenu = true;
			}
			else if(lemma.equals("preference")){
				isPreference = true;
			}
			else if(lemma.equals("dialogue")){
				isDialogue = true;
			}
		}
		if(isThree && isDot){
			result = true;
			return result;
		}
		if(isOption && isMenu){
			result = true;
			return result;
		}
		if(isPreference && isDialogue){
			result = true;
			return result;
		}
		return result;
	}
	
	private List<Goal> getGoalForClick(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<String> preferenceSet, Set<Integer> generalGoalIndexSet, int level){		
		List<Goal> result = new ArrayList<Goal>();
		boolean isLong = false;
		boolean isDouble = false;
		int repeatNum = -1;
		boolean useDobj = true;
		
		//check if goal should be repeated
		List<SemanticGraphEdge> amodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "amod", "");
		for(SemanticGraphEdge amodEdge:amodEdgeList){
			if(WordUtils.isMultipleLemma(amodEdge.getDependent().lemma().toLowerCase())){
				repeatNum = rand.nextInt((MAX_REPEAT - MIN_REPEAT) + 1) + MIN_REPEAT;
				break;
			}
		}
		List<SemanticGraphEdge> dobjEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", "");
		if(dobjEdgeList.size()==1){
			IndexedWord dobjWord = dobjEdgeList.get(0).getDependent();
			if(dobjWord.lemma().toLowerCase().equals("time")){
				List<SemanticGraphEdge> dobjDepEdgeList = new ArrayList<SemanticGraphEdge>();
				dobjDepEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, dobjWord, "amod", ""));
				dobjDepEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, dobjWord, "nummod", ""));
				for(SemanticGraphEdge dobjDepEdge:dobjDepEdgeList){
					String lemma = dobjDepEdge.getDependent().lemma().toLowerCase();
					if(WordUtils.isMultipleLemma(lemma)){
						repeatNum = rand.nextInt((MAX_REPEAT - MIN_REPEAT) + 1) + MIN_REPEAT;
						useDobj = false;
						break;
					}
					else{
						try{
							int number = NumberNormalizer.wordToNumber(lemma).intValue();
							repeatNum = number;
							useDobj = false;
							break;
						}
						catch(NumberFormatException nfe){

						}
					}
				}
			}
		}
		
		
		
		
		//handle case of click back
		List<SemanticGraphEdge> backCompoundEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "compound:prt", "");
		for(SemanticGraphEdge compoundEdge:backCompoundEdgeList){
			if(compoundEdge.getDependent().lemma().toLowerCase().equals("back")){
				if(repeatNum==-1){
					Goal currGoal = new GoalBack(false, false);
					result.add(currGoal);
					System.out.println(currGoal);
				}
				else{	
					for(int i=0;i<repeatNum;++i){
						Goal currGoal = new GoalBack(false, false);
						result.add(currGoal);
						System.out.println(currGoal);
					}
				}			
				return result;
			}
		}
		//handle case of click back
		List<SemanticGraphEdge> backAdvmodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", "");
		for(SemanticGraphEdge advmodEdge:backAdvmodEdgeList){
			if(advmodEdge.getDependent().lemma().toLowerCase().equals("back")){
				if(repeatNum==-1){
					Goal currGoal = new GoalBack(false, false);
					result.add(currGoal);
					System.out.println(currGoal);
				}
				else{	
					for(int i=0;i<repeatNum;++i){
						Goal currGoal = new GoalBack(false, false);
						result.add(currGoal);
						System.out.println(currGoal);
					}
				}			
				return result;
			}
		}
		//handle back case
		List<SemanticGraphEdge> backDobjEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", "");
		for(SemanticGraphEdge dobjEdge:backDobjEdgeList){
			if(dobjEdge.getDependent().lemma().toLowerCase().equals("key")){
				List<SemanticGraphEdge> backDobjAdvmodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, dobjEdge.getDependent(), "advmod", "");
				for(SemanticGraphEdge dobjAdvmodEdge:backDobjAdvmodEdgeList){
					if(dobjAdvmodEdge.getDependent().lemma().toLowerCase().equals("back")){
						if(repeatNum==-1){
							Goal currGoal = new GoalBack(false, false);
							result.add(currGoal);
							System.out.println(currGoal);
						}
						else{	
							for(int i=0;i<repeatNum;++i){
								Goal currGoal = new GoalBack(false, false);
								result.add(currGoal);
								System.out.println(currGoal);
							}
						}			
						return result;
					}
				}
			}
		}
		
		//taking care of long click
		List<SemanticGraphEdge> advmodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", "");
		for(SemanticGraphEdge advmodEdge:advmodEdgeList){
			if(advmodEdge.getDependent().lemma().toLowerCase().equals("long")){
				isLong = true;
			}
			if(advmodEdge.getDependent().lemma().toLowerCase().equals("double")){
				isDouble = true;
			}
		}
		
		//taking care of double click
		List<SemanticGraphEdge> nsubjEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "nsubj", "");
		for(SemanticGraphEdge nsubjEdge:nsubjEdgeList){
			if(nsubjEdge.getDependent().lemma().toLowerCase().equals("long")){
				isLong = true;
			}
			if(nsubjEdge.getDependent().lemma().toLowerCase().equals("double")){
				isDouble = true;
			}
		}
		//click duration
		int duration = 0;
		if(isLong){
			duration = 1;
		}
		else if(isDouble){
			duration =2;
		}
		
		//core dependents
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		if(componentEdgeList.size()==0){
			if(useDobj){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			}
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}
		
		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", "on"));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}
		
		//handle some parsing quirks
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nsubj", ""));
		}

		if(componentEdgeList.size()==1){
			//get dependent
			IndexedWord dependentIndexedWord = componentEdgeList.get(0).getDependent();
			Set<IndexedWord> dependentSubgraphSet = clauseDependenceGraph.getSubgraphVertices(dependentIndexedWord);
			List<IndexedWord> dependentSubgrapList = new ArrayList<IndexedWord>();
			dependentSubgrapList.addAll(dependentSubgraphSet);
			//sort dependents
			Collections.sort(dependentSubgrapList);
			//filter words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgrapList = keepWordBasedOnList(dependentSubgrapList, keepTagList);
			//action refers to opening app
			boolean refersToApp = wordsReferToApp(dependentSubgrapList);
			if(refersToApp){
				//ignore action
				result = null;
				return result;
			}
			//action refers to opening optiosn menu
			boolean refersToMenu = wordsReferToMenu(dependentSubgrapList);
			if(refersToMenu){
				//ignore action
				if(repeatNum==-1){
					Goal currGoal = new GoalOpenOptions(false, false);
					result.add(currGoal);
					System.out.println(currGoal);
				}
				else{	
					for(int i=0;i<repeatNum;++i){
						Goal currGoal = new GoalOpenOptions(false, false);
						result.add(currGoal);
						System.out.println(currGoal);
					}
				}
				return result;
			}
			//process words to identify target of click action
			boolean containsOntology = false;
//			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
//				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
//					containsOntology = true;
//					break;
//				}
//			}
			//check if target contains ontology
			if(containsOntology){
				String targetText = "";
				for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
					if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
						targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma());
						generalGoalIndexSet.add(dependentSubgraphWord.index());
					}
				}
				//add root as well
				generalGoalIndexSet.add(root.index());
				targetText = targetText.trim();
				boolean canBePreference = preferenceSet.contains(targetText);
				TargetViewModel targetViewModel = new TargetViewModel(targetText,
						"", "", new ArrayList<String>(),
						"", new ArrayList<String>(),
						"",
						true, true, canBePreference);
				if(repeatNum==-1){
					Goal currGoal = new GoalClick(false, false, targetViewModel, duration, 0);
					result.add(currGoal);
					System.out.println(currGoal);
				}
				else{	
					for(int i=0;i<repeatNum;++i){
						Goal currGoal = new GoalClick(false, false, targetViewModel, duration, 0);
						result.add(currGoal);
						System.out.println(currGoal);
					}
				}
				return result;
			}
			//check if it is a positional click action
			int position = -1;
			String container = "";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				String lemma = dependentSubgraphWord.lemma().toLowerCase();
				try{
					position = NumberNormalizer.wordToNumber(lemma).intValue();
				}
				catch(NumberFormatException nfe){
					
				}
				if(lemma.equals("last")){
					position=0;
				}
				if(WordUtils.isContainerLemma(lemma)){
					container=lemma;
				}
				if(!WordUtils.getContainerForLemma(lemma).equals("")){
					container=WordUtils.getContainerForLemma(lemma);
				}
			}
			if(position!=-1 && !container.equals("")){
				if(repeatNum==-1){
					Goal currGoal = new GoalClickPosition(false, false, position, container, duration);
					result.add(currGoal);
					System.out.println(currGoal);
				}
				else{	
					for(int i=0;i<repeatNum;++i){
						Goal currGoal = new GoalClickPosition(false, false, position, container, duration);
						result.add(currGoal);
						System.out.println(currGoal);
					}
				}
				return result;
			}
			
			//use the text as action
			String targetText="";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				if(WordUtils.isGenericLemma(dependentSubgraphWord.lemma().toLowerCase())){
					continue;
				}
				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING) || dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
					targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma().toLowerCase());
					generalGoalIndexSet.add(dependentSubgraphWord.index());
				}
				else{
					targetText = targetText + " " + dependentSubgraphWord.originalText();
					generalGoalIndexSet.add(dependentSubgraphWord.index());
				}
			}
			//add root as well
			generalGoalIndexSet.add(root.index());
			targetText = targetText.trim();
			if(targetText.equals("")){
				System.out.println("###############double check:text is empty for click#################");
				result = new ArrayList<Goal>();
				return result;
			}
			boolean canBePreference = preferenceSet.contains(targetText);
			//target not from ontology
			TargetViewModel targetViewModel = new TargetViewModel(targetText,
					"", "", new ArrayList<String>(),
					"", new ArrayList<String>(),
					"",
					false, true, canBePreference);
			if(repeatNum==-1){
				Goal currGoal = new GoalClick(false, false, targetViewModel, duration, 0);
				result.add(currGoal);
				System.out.println(currGoal);
			}
			else{	
				for(int i=0;i<repeatNum;++i){
					Goal currGoal = new GoalClick(false, false, targetViewModel, duration, 0);
					result.add(currGoal);
					System.out.println(currGoal);
				}
			}
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for click#################");
			result = new ArrayList<Goal>();
			return result;
		}
	}
	
	private Goal getGoalForGo(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<String> preferenceSet, Set<Integer> generalGoalIndexSet, int level){
		Goal result = null;
		
		//taking care of long click
		List<SemanticGraphEdge> advmodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", "");
		for(SemanticGraphEdge advmodEdge:advmodEdgeList){
			if(advmodEdge.getDependent().lemma().toLowerCase().equals("back")){
				result = new GoalBack(false, false);
				System.out.println(result.toString());
				return result;
			}
		}
		
		//core dependents
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));//necessary
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}
		
		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", ""));//necessary without specific
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}

		if(componentEdgeList.size()==1){
			//get dependent
			IndexedWord dependentIndexedWord = componentEdgeList.get(0).getDependent();
			Set<IndexedWord> dependentSubgraphSet = clauseDependenceGraph.getSubgraphVertices(dependentIndexedWord);
			List<IndexedWord> dependentSubgrapList = new ArrayList<IndexedWord>();
			dependentSubgrapList.addAll(dependentSubgraphSet);
			//sort dependents
			Collections.sort(dependentSubgrapList);
			//filter words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			//keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgrapList = keepWordBasedOnList(dependentSubgrapList, keepTagList);
			//action refers to opening app
			boolean refersToApp = wordsReferToApp(dependentSubgrapList);
			if(refersToApp){
				//ignore action
				result = null;
				return result;
			}
			//action refers to opening optiosn menu
			boolean refersToMenu = wordsReferToMenu(dependentSubgrapList);
			if(refersToMenu){
				//ignore action
				result = new GoalOpenOptions(false, false);
				System.out.println(result.toString());
				return result;
			}
			//process words to identify target of click action
			boolean containsOntology = false;
//			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
//				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
//					containsOntology = true;
//					break;
//				}
//			}
			//check if target contains ontology
			if(containsOntology){
				String targetText = "";
				for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
					if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
						targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma());
						generalGoalIndexSet.add(dependentSubgraphWord.index());
					}
				}
				//add root as well
				generalGoalIndexSet.add(root.index());
				targetText = targetText.trim();
				boolean canBePreference = preferenceSet.contains(targetText);
				TargetViewModel targetViewModel = new TargetViewModel(targetText,
						"", "", new ArrayList<String>(),
						"", new ArrayList<String>(),
						"",
						true, true, canBePreference);
				result = new GoalClick(false, false, targetViewModel, 0, 0);			
				System.out.println(result.toString());
				return result;
			}			
			//use the text as action
			String targetText="";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				if(WordUtils.isGenericLemma(dependentSubgraphWord.lemma().toLowerCase())){
					continue;
				}
				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING) || dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
					targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma().toLowerCase());
					generalGoalIndexSet.add(dependentSubgraphWord.index());
				}
				else{
					targetText = targetText + " " + dependentSubgraphWord.originalText();
					generalGoalIndexSet.add(dependentSubgraphWord.index());
				}
			}
			//add root as well
			generalGoalIndexSet.add(root.index());
			targetText = targetText.trim();
			if(targetText.equals("")){
				System.out.println("###############double check:text is empty for go#################");
				return null;
			}
			boolean canBePreference = preferenceSet.contains(targetText);
			//target not from ontology
			TargetViewModel targetViewModel = new TargetViewModel(targetText,
					"", "", new ArrayList<String>(),
					"", new ArrayList<String>(),
					"",
					false, true, canBePreference);
			result = new GoalClick(false, false, targetViewModel, 0, 0);
			System.out.println(result);
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for go#################");
			result = null;
			return result;
		}
	}
	
	private Goal getGoalForOpen(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<String> preferenceSet, int level){
		Goal result = null;
		
		//core dependents
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));//necessary
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}
		
		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", ""));//necessary without specific
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}

		if(componentEdgeList.size()==1){
			//get dependent
			IndexedWord dependentIndexedWord = componentEdgeList.get(0).getDependent();
			Set<IndexedWord> dependentSubgraphSet = clauseDependenceGraph.getSubgraphVertices(dependentIndexedWord);
			List<IndexedWord> dependentSubgrapList = new ArrayList<IndexedWord>();
			dependentSubgrapList.addAll(dependentSubgraphSet);
			//sort dependents
			Collections.sort(dependentSubgrapList);
			//refine words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			//keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgrapList = keepWordBasedOnList(dependentSubgrapList, keepTagList);
			//action refers to opening app
			boolean refersToApp = wordsReferToApp(dependentSubgrapList);
			if(refersToApp){
				//ignore action
				result = null;
				return result;
			}
			//action refers to opening optiosn menu
			boolean refersToMenu = wordsReferToMenu(dependentSubgrapList);
			if(refersToMenu){
				//ignore action
				result = new GoalOpenOptions(false, false);
				System.out.println(result.toString());
				return result;
			}
			//process words to identify target of click action
			boolean containsOntology = false;
//			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
//				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
//					containsOntology = true;
//					break;
//				}
//			}
			//check if target contains ontology
			if(containsOntology){
				String targetText = "";
				for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
					if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
						targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma());
					}
				}
				targetText = targetText.trim();
				boolean canBePreference = preferenceSet.contains(targetText);
				TargetViewModel targetViewModel = new TargetViewModel(targetText,
						"", "", new ArrayList<String>(),
						"", new ArrayList<String>(),
						"",
						true, true, canBePreference);
				result = new GoalClick(false, false, targetViewModel, 0, 0);			
				System.out.println(result.toString());
				return result;
			}			
			//use the text as action
			String targetText="";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				if(WordUtils.isGenericLemma(dependentSubgraphWord.lemma().toLowerCase())){
					continue;
				}
				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING) || dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
					targetText = targetText + " " + replacementMap.get(dependentSubgraphWord.lemma().toLowerCase());
				}
				else{
					targetText = targetText + " " + dependentSubgraphWord.originalText();
				}
			}
			targetText = targetText.trim();
			if(targetText.equals("")){
				System.out.println("###############double check:text is empty for open#################");
				return null;
			}
			boolean canBePreference = preferenceSet.contains(targetText);
			//target not from ontology
			TargetViewModel targetViewModel = new TargetViewModel(targetText,
					"", "", new ArrayList<String>(),
					"", new ArrayList<String>(),
					"",
					false, true, canBePreference);
			result = new GoalClick(false, false, targetViewModel, 0, 0);
			System.out.println(result);
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for open#################");
			result = null;
			return result;
		}
	}
	
	private Goal getGoalForAdd(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<Integer> generalGoalIndexSet, int level){
		Goal result = null;
		
		//core dependents
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));//necessary
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}
		
		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", ""));//necessary without specific
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}
		
		//quirks
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dep", ""));
		}

		if(componentEdgeList.size()==1){
			//get dependent
			IndexedWord dependentIndexedWord = componentEdgeList.get(0).getDependent();
			Set<IndexedWord> dependentSubgraphSet = clauseDependenceGraph.getSubgraphVertices(dependentIndexedWord);
			List<IndexedWord> dependentSubgrapList = new ArrayList<IndexedWord>();
			dependentSubgrapList.addAll(dependentSubgraphSet);
			//sort dependents
			Collections.sort(dependentSubgrapList);
			
			
			boolean allWordAlreadyConsidered = true;
			for(IndexedWord analysisWord:dependentSubgrapList){
				if(!generalGoalIndexSet.contains(analysisWord.index()) && !WordUtils.isGenericLemma(analysisWord.lemma().toLowerCase())){
					allWordAlreadyConsidered=false;
					break;
				}
			}
			if(allWordAlreadyConsidered){
				result = null;
				System.out.println("+++++++++++++++++++++add goal with words already considered+++++++++++++++++++++");
				return result;
			}
			
			//process words to identify target of click action
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			//keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgrapList = keepWordBasedOnList(dependentSubgrapList, keepTagList);
			
			//check if it has a quote and in case place the text inside the quote as the text to be typed
			String typedText = "";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING)){
					typedText = typedText + " " + replacementMap.get(dependentSubgraphWord.lemma());
				}
			}
			typedText = typedText.trim();
			//find the target
			boolean containsOntology = false;
//			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
//				if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
//					containsOntology = true;
//					break;
//				}
//			}
			//check if target contains ontology
			if(containsOntology){
				String targetText = "";
				for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
					if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
						targetText = targetText + replacementMap.get(dependentSubgraphWord.lemma()) + " ";
					}
				}
				targetText = targetText.trim();
				TargetViewModel targetViewModel = new TargetViewModel(targetText,
						"", "", new ArrayList<String>(),
						"", new ArrayList<String>(),
						"",
						true, true, false);
				result = new GoalType(false, false, targetViewModel, typedText, 0);		
				System.out.println(result.toString());
				return result;
			}			
			//use the text as target
			String targetText="";
			for(IndexedWord dependentSubgraphWord:dependentSubgrapList){
				if(WordUtils.isGenericLemma(dependentSubgraphWord.lemma().toLowerCase())){
					continue;
				}
				else if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING)){
					targetText = targetText + "" + replacementMap.get(dependentSubgraphWord.lemma());
				}
				else if(dependentSubgraphWord.lemma().toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING)){
					continue;
				}
				else{
					targetText = targetText + " " +dependentSubgraphWord.originalText();
				}
			}
			targetText = targetText.trim();
			if(targetText.equals("")){
				System.out.println("###############double check:text is empty for type#################");
				result = null;
				return result;
			}
			//target not from ontology
			TargetViewModel targetViewModel = new TargetViewModel(targetText,
					"", "", new ArrayList<String>(),
					"", new ArrayList<String>(),
					"",
					false, true, false);
			result = new GoalType(false, false, targetViewModel, typedText, 0);	
			System.out.println(result);
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for type#################");
			result = null;
			return result;
		}
	}
	
	private Goal getGoalForWrite(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<Integer> generalGoalIndexSet, int level){
		return getGoalForAdd(root, clauseDependenceGraph, replacementMap, generalGoalIndexSet, level);
	}
	
	private Goal getGoalForType(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<Integer> generalGoalIndexSet, int level){
		return getGoalForAdd(root, clauseDependenceGraph, replacementMap, generalGoalIndexSet, level);
	}
	
	private Goal getGoalForLeave(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<Integer> generalGoalIndexSet, int level){
		//taking care of long click
		String text="";
		List<SemanticGraphEdge> xcompEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", "");
		for(SemanticGraphEdge xcompEdge:xcompEdgeList){
			if(xcompEdge.getDependent().lemma().toLowerCase().equals("empty")){
				text="empty";
			}
		}
		Goal result = getGoalForAdd(root, clauseDependenceGraph, replacementMap, generalGoalIndexSet, level);
		if(result instanceof GoalType && text.equals("empty")){
			GoalType goalType = (GoalType) result;
			goalType.setText(text);
		}
		return result;
	}
	
	private boolean isCompleteDirection(List<IndexedWord> indexedWordList){
		boolean result = false;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		for(IndexedWord analysisWord:analysisWordList){
			if(WordUtils.isCompleteDirectionLemma(analysisWord.word().toLowerCase())){
				result = true;
				return result;
			}
		}
		return result;
	}
	
	private int getCompleteDirectionForLemma(List<IndexedWord> indexedWordList){
		int result = -1;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		for(IndexedWord analysisWord:analysisWordList){
			int completeDirection = WordUtils.getCompleteDirectionForLemma(analysisWord.word().toLowerCase()); 
			if(completeDirection!=-1){
				result = completeDirection;
				return result;
			}
		}
		return result;
	}
	
	private boolean isDirection(List<IndexedWord> indexedWordList){
		boolean result = false;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		for(IndexedWord analysisWord:analysisWordList){
			if(WordUtils.isDirectionLemma(analysisWord.word().toLowerCase())){
				result = true;
				return result;
			}
		}
		return result;
	}
	
	private int getDirectionForLemma(List<IndexedWord> indexedWordList){
		int result = -1;
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");
		keepTagList.add("J");
		keepTagList.add("N");
		keepTagList.add("R");
		keepTagList.add("V");
		List<IndexedWord> analysisWordList = keepWordBasedOnList(indexedWordList, keepTagList);
		for(IndexedWord analysisWord:analysisWordList){
			int completeDirection = WordUtils.getDirectionForLemma(analysisWord.word().toLowerCase()); 
			if(completeDirection!=-1){
				result = completeDirection;
				return result;
			}
		}
		return result;
	}
	
	private Goal getGoalForScroll(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, int level){
		Goal result = null;
		
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		//handle cases such as scroll down
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "compound:prt", ""));
		}
		
		//core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));//necessary
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}

		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", ""));//necessary without specific
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}
		
		//handle parser quirks
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dep", ""));
		}
		
		if(componentEdgeList.size()>0){
			List<IndexedWord> dependentSubgraphList = new ArrayList<IndexedWord>();
			for(SemanticGraphEdge rootEdge:componentEdgeList){
				dependentSubgraphList.addAll(clauseDependenceGraph.getSubgraphVertices(rootEdge.getDependent()));
			}
			Collections.sort(dependentSubgraphList);
			
			//process words to identify target of click action
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			//keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgraphList = keepWordBasedOnList(dependentSubgraphList, keepTagList);
			
			if(isCompleteDirection(dependentSubgraphList)){
				int completeDirection = getCompleteDirectionForLemma(dependentSubgraphList);
				result = new GoalScrollComplete(false, false, completeDirection);
				System.out.println(result.toString());
				return result;
			}
			
			if(isDirection(dependentSubgraphList)){
				int direction = getDirectionForLemma(dependentSubgraphList);
				result = new GoalScroll(false, false, direction);
				System.out.println(result.toString());
				return result;
			}
			
			System.out.println("###############double check:could not identify scroll direction#################");
			result = null;
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for scroll#################");
			result = null;
			return result;
		}
	}
	
	//it would be swipe
	private Goal getGoalForNavigate(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, int level){
		Goal result = null;
		
		List<SemanticGraphEdge> componentEdgeList = new ArrayList<SemanticGraphEdge>();
		//handle cases such as scroll down
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "compound:prt", ""));
		}
		
		//core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", ""));
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "xcomp", ""));//necessary
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "ccomp", ""));
				}
			}
		}

		//non-core dependents
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "nmod", ""));//necessary without specific
			if(componentEdgeList.size()==0){
				componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advcl", ""));
				if(componentEdgeList.size()==0){
					componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "advmod", ""));
				}
			}
		}
		
		//handle parser quirks
		if(componentEdgeList.size()==0){
			componentEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, root, "dep", ""));
		}
		
		if(componentEdgeList.size()>0){
			List<IndexedWord> dependentSubgraphList = new ArrayList<IndexedWord>();
			for(SemanticGraphEdge rootEdge:componentEdgeList){
				dependentSubgraphList.addAll(clauseDependenceGraph.getSubgraphVertices(rootEdge.getDependent()));
			}
			Collections.sort(dependentSubgraphList);
			
			//process words to identify target of click action
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			//keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			dependentSubgraphList = keepWordBasedOnList(dependentSubgraphList, keepTagList);
			
			if(isDirection(dependentSubgraphList)){
				int direction = getDirectionForLemma(dependentSubgraphList);
				result = new GoalSwipe(false, false, direction);
				System.out.println(result.toString());
				return result;
			}
			
			System.out.println("###############double check:could not identify swipe direction#################");
			result = null;
			return result;
		}
		else{
			System.out.println("###############double check:wrong number of dependencies for swipe#################");
			result = null;
			return result;
		}
	}
	
	private Goal getGoalForRotate(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap,  Set<Integer> generalGoalIndexSet, int level){
		Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
		List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
		subgrapList.addAll(subgraphSet);
		Collections.sort(subgrapList);
		for(IndexedWord dependentSubgraphWord:subgrapList){
			generalGoalIndexSet.add(dependentSubgraphWord.index());
		}
		Goal result = new GoalRotate(false, false);
		System.out.println(result.toString());
		return result;
	}
	
	private Goal getGoalForChange(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, int level){
		Goal result = null;
		Set<IndexedWord> dependentSubgraphList = clauseDependenceGraph.getSubgraphVertices(root);
		boolean isOrientation = false;
		for(IndexedWord analysisWord:dependentSubgraphList){
			if(analysisWord.lemma().toLowerCase().equals("orientation")){
				isOrientation = true;
				break;
			}
		}
		if(isOrientation){
			result = new GoalRotate(false, false);
			System.out.println(result.toString());
			return result;
		}
		return result;
	}
	
	private List<Goal> getGoalForDo(OpenIE openIE, IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<String> preferenceSet, List<Ontology> ontologyList, List<String> stringList, int level){
		List<Goal> subGoalList = new ArrayList<Goal>();
		Set<IndexedWord> vertexSubgraphList = clauseDependenceGraph.getSubgraphVertices(root);
		vertexSubgraphList.remove(root);
		List<IndexedWord> orderedVertexSubgraphList = new ArrayList<IndexedWord>();
		orderedVertexSubgraphList.addAll(vertexSubgraphList);
		Collections.sort(orderedVertexSubgraphList);
		
		String newText = "";
		for(IndexedWord analysisWord:orderedVertexSubgraphList){
			newText = newText + " " + analysisWord.originalText();
		}
		Properties depProps = new Properties();
		depProps.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		RedwoodConfiguration.current().clear().apply();
		StanfordCoreNLP depPipeline = new StanfordCoreNLP(depProps);
		Annotation depAnnotation = new Annotation(newText);
		depPipeline.annotate(depAnnotation);
	    for (CoreMap sentence : depAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	    	subGoalList.addAll(processSentence(openIE, sentence, replacementMap, preferenceSet, ontologyList, stringList, level));
	    }
		return subGoalList;
	}
	
	private List<Goal> getGoalForRepeat(List<Goal> currGoalList, IndexedWord root, SemanticGraph clauseDependenceGraph){
		List<Goal> resultList = new ArrayList<Goal>();
		int repeatNum = -1;
		
		//case 1
		List<SemanticGraphEdge> amodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "amod", "");
		for(SemanticGraphEdge amodEdge:amodEdgeList){
			if(WordUtils.isMultipleLemma(amodEdge.getDependent().lemma().toLowerCase())){
				repeatNum = rand.nextInt((MAX_REPEAT - MIN_REPEAT) + 1) + MIN_REPEAT;
				break;
			}
		}
		
		//case 2
		List<SemanticGraphEdge> dobjEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "dobj", "");
		if(dobjEdgeList.size()==1){
			IndexedWord dobjWord = dobjEdgeList.get(0).getDependent();
			if(dobjWord.lemma().toLowerCase().equals("time")){
				List<SemanticGraphEdge> dobjDepEdgeList = new ArrayList<SemanticGraphEdge>();
				dobjDepEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, dobjWord, "amod", ""));
				dobjDepEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, dobjWord, "nummod", ""));
				for(SemanticGraphEdge dobjDepEdge:dobjDepEdgeList){
					String lemma = dobjDepEdge.getDependent().lemma().toLowerCase();
					if(WordUtils.isMultipleLemma(lemma)){
						repeatNum = rand.nextInt((MAX_REPEAT - MIN_REPEAT) + 1) + MIN_REPEAT;
						break;
					}
					else{
						try{
							int number = NumberNormalizer.wordToNumber(lemma).intValue();
							repeatNum = number;
							break;
						}
						catch(NumberFormatException nfe){

						}
					}
				}
			}
		}
		
		//case 3
		List<SemanticGraphEdge> nummodEdgeList = getOutgoingNamedEdge(clauseDependenceGraph, root, "nummod", "");
		if(nummodEdgeList.size()==1){
			IndexedWord nummodWord = nummodEdgeList.get(0).getDependent();
			if(nummodWord.lemma().toLowerCase().equals("time")){
				List<SemanticGraphEdge> nummodDepEdgeList = new ArrayList<SemanticGraphEdge>();
				nummodDepEdgeList.addAll(getOutgoingNamedEdge(clauseDependenceGraph, nummodWord, "advmod", ""));
				for(SemanticGraphEdge nummodDepEdge:nummodDepEdgeList){
					String lemma = nummodDepEdge.getDependent().lemma().toLowerCase();
					if(WordUtils.isMultipleLemma(lemma)){
						repeatNum = rand.nextInt((MAX_REPEAT - MIN_REPEAT) + 1) + MIN_REPEAT;
						break;
					}
					else{
						try{
							int number = NumberNormalizer.wordToNumber(lemma).intValue();
							repeatNum = number;
							break;
						}
						catch(NumberFormatException nfe){

						}
					}
				}
			}
		}
		
		//handle repetition
		if(repeatNum != -1){
			for(int i=0; i<(repeatNum-1); ++i){
				resultList.addAll(currGoalList);
				System.out.println("+++++++++++++++++++++repeating goals+++++++++++++++++++++");
			}
		}
		return resultList;
	}
	
	private boolean ignoreClauseBasedOnProperties(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<Integer> generalGoalIndexSet, int level){
		boolean result = false;
		
		//mf: ignore general clause no matter what
		if(level==0){
			//do not do consider the clause
			result = true;
			System.out.println("+++++++++++++++++++++ignored because level 0+++++++++++++++++++++");
			return result;
		}
		
		//get root lemma
		String rootLemma = root.lemma();
		if(rootLemma.toLowerCase().endsWith("_"+WordUtils.ELEMENT_STRING) || rootLemma.toLowerCase().endsWith("_"+WordUtils.QUOTE_STRING)){
			rootLemma = replacementMap.get(rootLemma.toLowerCase());
		}
		
		if(WordUtils.isInstallLemma(root.lemma().toLowerCase())){
			result = true;
			System.out.println("+++++++++++++++++++++lemma install ignored+++++++++++++++++++++");
			return result;
		}
		
		if(WordUtils.isStartLemma(root.lemma().toLowerCase())){
			Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
			subgraphSet.remove(root);
			List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
			subgrapList.addAll(subgraphSet);
			//sort dependents
			Collections.sort(subgrapList);
			//filter words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			subgrapList = keepWordBasedOnList(subgrapList, keepTagList);
			//action refers to opening app
			boolean refersToApp = wordsReferToApp(subgrapList);
			if(refersToApp){
				//start app case
				result = true;
				System.out.println("+++++++++++++++++++++start app ignored+++++++++++++++++++++");
				return result;
			}
		}
		
		if(WordUtils.isErrorLemma(root.lemma().toLowerCase())){
			result = true;
			System.out.println("+++++++++++++++++++++lemma error ignored+++++++++++++++++++++");
			return result;
		}
		
		if(WordUtils.isNothingLemma(root.lemma().toLowerCase())){
			result = true;
			System.out.println("+++++++++++++++++++++lemma nothing ignored+++++++++++++++++++++");
			return result;
		}
		
		
		if(WordUtils.isWaitLemma(root.lemma().toLowerCase())){
			Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
			List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
			subgrapList.addAll(subgraphSet);
			Collections.sort(subgrapList);
			for(IndexedWord dependentSubgraphWord:subgrapList){
				generalGoalIndexSet.add(dependentSubgraphWord.index());
			}
			result = true;
			System.out.println("+++++++++++++++++++++lemma wait ignored+++++++++++++++++++++");
			return result;
		}
		
		//check for properties about the complete clause
		Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
		List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
		subgrapList.addAll(subgraphSet);
		//sort dependents
		Collections.sort(subgrapList);
		//filter words
		List<String> keepTagList = new ArrayList<String>();
		keepTagList.add("CD");//CD cardinal number
		keepTagList.add("IN");//IN Preposition or subordinating conjunction
		keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
		keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
		keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
		keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
		subgrapList = keepWordBasedOnList(subgrapList, keepTagList);
		for(IndexedWord analysisWord:subgrapList){
			String analysisWordLemma = analysisWord.lemma().toLowerCase();
			if(analysisWordLemma.endsWith("_"+WordUtils.ELEMENT_STRING) || analysisWordLemma.endsWith("_"+WordUtils.QUOTE_STRING)){
				analysisWordLemma = replacementMap.get(analysisWordLemma);
			}
			if(WordUtils.isErrorLemma(analysisWordLemma)){
				result = true;
				System.out.println("+++++++++++++++++++++clause error ignored+++++++++++++++++++++");
				return result;
			}
		}
		return result;
	}
	
	private double getScoreFromTextService(String sentence1, String sentence2){
		//System.out.println("getting score from server:"+TEXT_SERVER_ADDRESS);
		long startServerTime = System.currentTimeMillis();
		double score = -1;
		try{
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("http://"+TEXT_SERVER_ADDRESS+":9000");
			JSONObject requestJSON = new JSONObject();
			requestJSON.put("sentence1", sentence1);
			requestJSON.put("sentence2", sentence2);
		    StringEntity params = new StringEntity(requestJSON.toString());
		    request.addHeader("content-type", "application/json");
		    request.setEntity(params);
		    HttpResponse httpResponse = httpClient.execute(request);
		    String responseString = EntityUtils.toString(httpResponse.getEntity());
		    JSONObject responseJSON = new JSONObject(responseString);
		    score = responseJSON.getDouble("score");
		}
		catch(Exception e){
			System.out.println("s1:"+sentence1+"#s2:"+sentence2);
			e.printStackTrace();
		}
		long endServerTime = System.currentTimeMillis();
		long serverTime = endServerTime - startServerTime;
		SERVER_TIME = SERVER_TIME + serverTime;
		return score;
	}
	
	private Goal getGeneralGoal(IndexedWord root, SemanticGraph clauseDependenceGraph, Map<String, String> replacementMap, Set<String> preferenceSet, List<Ontology> ontologyList, List<String> stringList, Set<Integer> generalGoalIndexSet, int level){
		Goal result = null;
		//do not have general clicks
		if(level==0){
			result = null;
			return result;
		}
		else if(level == 1){
			//allow clauses that are related to the text of the app
			//get clause text
			Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
			List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
			subgrapList.addAll(subgraphSet);
			//sort dependents
			Collections.sort(subgrapList);
			//filter words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			subgrapList = keepWordBasedOnList(subgrapList, keepTagList);
			
			boolean allWordAlreadyConsidered = true;
			for(IndexedWord analysisWord:subgrapList){
				if(!generalGoalIndexSet.contains(analysisWord.index()) && !WordUtils.isGenericLemma(analysisWord.lemma().toLowerCase())){
					allWordAlreadyConsidered=false;
					break;
				}
			}
			if(allWordAlreadyConsidered){
				result = null;
				System.out.println("+++++++++++++++++++++general goal with words already considered+++++++++++++++++++++");
				return result;
			}
			
			String clauseText = "";
			for(IndexedWord analysisWord:subgrapList){
				String lemma = analysisWord.lemma().toLowerCase();
				if(lemma.endsWith("_"+WordUtils.ELEMENT_STRING) || lemma.endsWith("_"+WordUtils.QUOTE_STRING)){
					clauseText = clauseText + " " + replacementMap.get(lemma);
				}
				else{
					clauseText = clauseText + " " + analysisWord.originalText();
				}
			}
			clauseText = clauseText.trim();
			
			boolean isRelatedToApp = false;
			//check ontology text
			for(Ontology ontology:ontologyList){
				String ontologyText = ontology.getText();
				if(ontologyText.equals("")){
					//no need to check if ontology is empty
					continue;
				}
				double score = getScoreFromTextService(clauseText, ontologyText);
				if(score>MIN_SENTENCE_SIMILARITY_SCORE){
					System.out.println("score:"+score+"#s1:"+clauseText+"#s2:"+ontologyText);
					isRelatedToApp = true;
					break;
				}
			}	
			//check string text
			for(String stringValue:stringList){
				if(stringValue.equals("")){
					//no need to check if string is empty
					continue;
				}
				double score = getScoreFromTextService(clauseText, stringValue);
				if(score>MIN_SENTENCE_SIMILARITY_SCORE){
					System.out.println("score:"+score+"#s1:"+clauseText+"#s2:"+stringValue);
					isRelatedToApp = true;
					break;
				}
			}
			if(isRelatedToApp){
				boolean canBePreference = preferenceSet.contains(clauseText);
				TargetViewModel targetViewModel = new TargetViewModel(clauseText,
						"", "", new ArrayList<String>(),
						"", new ArrayList<String>(),
						"",
						false, true, canBePreference);
				
				
				result = new GoalClickGeneral(false, false, targetViewModel, 0);
				System.out.println(result.toString());
				for(IndexedWord analysisWord:subgrapList){
					generalGoalIndexSet.add(analysisWord.index());
				}
				return result;
			}
			else{
				System.out.println("###############double check:not related to app#################");
				result = null;
				return result;
			}
			
		}
		else{
			//level == 2
			//allow any clause as target
			Set<IndexedWord> subgraphSet = clauseDependenceGraph.getSubgraphVertices(root);
			List<IndexedWord> subgrapList = new ArrayList<IndexedWord>();
			subgrapList.addAll(subgraphSet);
			//sort dependents
			Collections.sort(subgrapList);
			//filter words
			List<String> keepTagList = new ArrayList<String>();
			keepTagList.add("CD");//CD cardinal number
			keepTagList.add("IN");//IN Preposition or subordinating conjunction
			keepTagList.add("J");//J* Adjective, Adjective comparative, Adjective superlative
			keepTagList.add("N");//N* Noun singular or mass, Noun plural, Proper noun singular, Proper noun plural
			keepTagList.add("R");//R* Adverb, Adverb comparative, Adverb superlative, Particle
			keepTagList.add("V");//V* Verb base form, Verb past tense, Verb gerund or present participle, Verb past participle, Verb past participle, Verb non­3rd person singular present, Verb 3rd person singular present
			subgrapList = keepWordBasedOnList(subgrapList, keepTagList);
			
			String clauseText = "";
			for(IndexedWord analysisWord:subgrapList){
				String lemma = analysisWord.lemma().toLowerCase();
				if(lemma.endsWith("_"+WordUtils.ELEMENT_STRING) || lemma.endsWith("_"+WordUtils.QUOTE_STRING)){
					clauseText = clauseText + " " + replacementMap.get(lemma);
				}
				else{
					clauseText = clauseText + " " + analysisWord.originalText();
				}
			}
			clauseText = clauseText.trim();
			
			if(clauseText.equals("")){
				System.out.println("###############double check:text is empty for general#################");
				result = null;
				return result;
			}
			//target not from ontology
			boolean canBePreference = preferenceSet.contains(clauseText);
			TargetViewModel targetViewModel = new TargetViewModel(clauseText,
					"", "", new ArrayList<String>(),
					"", new ArrayList<String>(),
					"",
					false, true, canBePreference);
			result = new GoalClickGeneral(false, false, targetViewModel, 0);
			System.out.println(result.toString());
			return result;
		}
	}
	

	
	private List<Goal> processSentence(OpenIE openIE, CoreMap sentence, Map<String, String> replacementMap, Set<String> preferenceSet, List<Ontology> ontologyList, List<String> stringList, int level){
		//mf print sentence information
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("sentence:"+sentence);
		SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
		System.out.println(dependencies);
		List<Goal> goalList = new ArrayList<Goal>();
		Set<Integer> generalGoalIndexSet = new HashSet<Integer>();
		//order sentences based on the 
		List<SentenceFragment> clauseList = orderClauses(openIE, sentence);
		//process clauses
		for(SentenceFragment clause:clauseList){
    		IndexedWord root = clause.parseTree.getFirstRoot();
    		String rootLemma = root.lemma().toLowerCase();
    		String rootTag = root.tag();
    		boolean rootProcessed = false;
			System.out.println("clause:"+clause+"#"+clause.parseTree.getFirstRoot().originalText());
			System.out.println(clause.parseTree);
    		if(rootLemma.equals("click")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			List<Goal> clickGoalList = getGoalForClick(root, clause.parseTree, replacementMap, preferenceSet, generalGoalIndexSet, level);
    			if(clickGoalList.size()>0){
    				goalList.addAll(clickGoalList);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for click:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("go")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal goGoal = getGoalForGo(root, clause.parseTree, replacementMap, preferenceSet, generalGoalIndexSet, level);
    			if(goGoal!=null){
    				goalList.add(goGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for go:"+clause+"###############");
    				rootProcessed=false;
    			}
    		} 
    		else if(rootLemma.equals("open")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal openGoal = getGoalForOpen(root, clause.parseTree, replacementMap, preferenceSet, level);
    			if(openGoal!=null){
    				goalList.add(openGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for open:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("add")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal typeGoal = getGoalForAdd(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(typeGoal!=null){
    				goalList.add(typeGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for add:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("write")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal typeGoal = getGoalForWrite(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(typeGoal!=null){
    				goalList.add(typeGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for write:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("type")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal typeGoal = getGoalForType(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(typeGoal!=null){
    				goalList.add(typeGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for type:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("leave")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal typeGoal = getGoalForLeave(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(typeGoal!=null){
    				goalList.add(typeGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for leave:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("scroll")){
//    			System.out.println("clause:"+fragment);
//    			System.out.println(clause.parseTree);
    			Goal scrollGoal = getGoalForScroll(root, clause.parseTree, replacementMap, level);
    			if(scrollGoal!=null){
    				goalList.add(scrollGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for scroll:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("navigate")){//it would be swipe
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal flingGoal = getGoalForNavigate(root, clause.parseTree, replacementMap, level);
    			if(flingGoal!=null){
    				goalList.add(flingGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for navigate:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("rotate")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal rotateGoal = getGoalForRotate(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(rotateGoal!=null){
    				goalList.add(rotateGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for rotate:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("change")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			Goal changeGoal = getGoalForChange(root, clause.parseTree, replacementMap, level);
    			if(changeGoal!=null){
    				goalList.add(changeGoal);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for change:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("do")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			List<Goal> doGoalList = getGoalForDo(openIE, root, clause.parseTree, replacementMap, preferenceSet, ontologyList, stringList, level);
    			if(doGoalList.size()>0){
    				goalList.addAll(doGoalList);
    				rootProcessed=true;
    			}
    			else{
    				System.out.println("###############double check:null goal for do:"+clause+"###############");
    				rootProcessed=false;
    			}
    		}
    		else if(rootLemma.equals("repeat")){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			goalList.addAll(getGoalForRepeat(goalList, root, clause.parseTree));
    		}
    		if(!rootProcessed){
//    			System.out.println("clause:"+clause);
//    			System.out.println(clause.parseTree);
    			boolean ignoreClause = ignoreClauseBasedOnProperties(root, clause.parseTree, replacementMap, generalGoalIndexSet, level);
    			if(!ignoreClause){
    				Goal generalGoal = getGeneralGoal(root, clause.parseTree, replacementMap, preferenceSet, ontologyList, stringList, generalGoalIndexSet, level);
        			if(generalGoal!=null){
        				goalList.add(generalGoal);
        				rootProcessed=true;
        			}
        			else{
        				System.out.println("###############double check:null goal for general goal:"+clause+"###############");
        				rootProcessed=false;
        			}
    			}
    		}
    		
    	}
		return goalList;
	}
}
