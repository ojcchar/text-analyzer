package seers.textanalyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.Pair;

public class DependenciesUtils {

	public static Pair<GrammaticalRelation, IndexedWord> getChildFirstRelation(SemanticGraph dependencies,
			IndexedWord idxWord, String... relations) {

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		List<String> relList = Arrays.asList(relations);
		Optional<Pair<GrammaticalRelation, IndexedWord>> child = childPairs.stream()
				.filter(p -> relList.contains(p.first.getShortName())).findFirst();

		if (child.isPresent()) {
			return child.get();
		}

		return null;
	}

	public static List<SemanticGraphEdge> findRelationsByTgtRelationAndPos(SemanticGraph dependencies,
			String tgtRelation, String pos) {
		GrammaticalRelation relation = GrammaticalRelation.valueOf(Language.UniversalEnglish, tgtRelation);
		List<SemanticGraphEdge> rels = dependencies.findAllRelns(relation);
		if (pos != null) {
			rels = rels.stream().filter(a -> a.getTarget().tag().equals(pos)).collect(Collectors.toList());
		}
		return rels;
	}

	public static boolean checkForRelationsInPairs(List<Pair<GrammaticalRelation, IndexedWord>> pairs,
			String... relations) {

		List<String> relList = Arrays.asList(relations);
		boolean anyMatch = pairs.stream().anyMatch(p -> relList.contains(p.first.getShortName()));
		return anyMatch;
	}

	public static List<SemanticGraphEdge> findRelationsByTgtRelationPosAndLemmas(SemanticGraph dependencies,
			String tgtRelation, String pos, Set<String> lemmas) {
		List<SemanticGraphEdge> rels = findRelationsByTgtRelationAndPos(dependencies, tgtRelation, pos);
		rels = rels.stream().filter(r -> lemmas.contains(r.getTarget().lemma())).collect(Collectors.toList());
		return rels;
	}

	public static List<Pair<GrammaticalRelation, IndexedWord>> getChildRelations(SemanticGraph dependencies,
			IndexedWord idxWord, String... relations) {

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		List<String> relList = Arrays.asList(relations);
		List<Pair<GrammaticalRelation, IndexedWord>> rels = childPairs.stream()
				.filter(p -> relList.stream().anyMatch(r -> p.first.getShortName().equals(r)))
				.collect(Collectors.toList());

		return rels;
	}

}
