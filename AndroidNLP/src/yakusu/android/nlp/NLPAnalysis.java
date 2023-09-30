package yakusu.android.nlp;

public class NLPAnalysis {
	public static void main(String[] args) {
		if(args.length!=5){
			System.out.println("usage: java -jar GenerateGoals.jar text.txt ontology.json string.json config.json goals.json");
			System.exit(-1);
		}
		
		GenerateGoals generateGoalsNew = new GenerateGoals();
		generateGoalsNew.generateGoals(args[0], args[1], args[2], args[3], args[4]);
	}
}
