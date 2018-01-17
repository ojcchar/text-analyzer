package seers.textanalyzer;

import java.util.*;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.Pair;

public class DependenciesUtils {

	public static Pair<GrammaticalRelation, IndexedWord> getFirstChildByRelation(SemanticGraph dependencies,
			IndexedWord idxWord, String... relations) {

		if (idxWord == null) {
			return null;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);
		
		childPairs.sort( (c1,c2) -> Integer.compare(c1.second.index(), c2.second.index()));

		List<String> relList = Arrays.asList(relations);
		Optional<Pair<GrammaticalRelation, IndexedWord>> child = childPairs.stream()
				.filter(p -> relList.contains(p.first.getShortName())).findFirst();

		if (child.isPresent()) {
			return child.get();
		}

		return null;
	}

	public static Pair<GrammaticalRelation, IndexedWord> getFirstChildByRelationSpecific(SemanticGraph dependencies,
			IndexedWord idxWord, Map<String, List<String>> relations) {

		if (idxWord == null) {
			return null;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		for (Pair<GrammaticalRelation, IndexedWord> p : childPairs) {
			List<String> specifics = relations.get(p.first.getShortName());
			if (specifics != null) {
				if (p.first.getSpecific() != null && specifics.contains( p.first.getSpecific())) {
					return p;
				}
			}
		}

		return null;
	}

	public static List<SemanticGraphEdge> findRelationsByTgtRelationAndPos(SemanticGraph dependencies,
			String tgtRelation, String pos) {

		List<SemanticGraphEdge> rels = findAllRellns(dependencies, tgtRelation);
		if (rels != null && pos != null) {
			rels = rels.stream().filter(a -> a.getTarget().tag().equals(pos)).collect(Collectors.toList());
		}
		return rels;
	}

	private static List<SemanticGraphEdge> findAllRellns(SemanticGraph dependencies, String tgtRelation) {

		Iterable<SemanticGraphEdge> edgeIterable = dependencies.edgeIterable();
		List<SemanticGraphEdge> edges = new ArrayList<>();

		for (SemanticGraphEdge edge : edgeIterable) {
			if (edge.getRelation().getShortName().equals(tgtRelation)) {
				edges.add(edge);
			}
		}

		return edges;

	}

	public static List<SemanticGraphEdge> findRelationsByTgtRelations(SemanticGraph dependencies,
			String... tgtRelations) {

		if (tgtRelations == null) {
			return null;
		}

		Set<SemanticGraphEdge> edges = new LinkedHashSet<>();

		for (String rel : tgtRelations) {
			List<SemanticGraphEdge> edgedRel = findRelationsByTgtRelationAndPos(dependencies, rel, null);
			edges.addAll(edgedRel);
		}

		return new ArrayList<>(edges);
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
		
		if (idxWord==null) {
			return new ArrayList<>();
		}

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		List<String> relList = Arrays.asList(relations);
		List<Pair<GrammaticalRelation, IndexedWord>> rels = childPairs.stream()
				.filter(p -> relList.stream().anyMatch(r -> p.first.getShortName().equals(r)))
				.collect(Collectors.toList());

		return rels;
	}

	public static Pair<GrammaticalRelation, IndexedWord> getFirstChildByRelationAndPos(SemanticGraph dependencies,
			IndexedWord idxWord, Set<String> relations, Set<String> pos) {

		if (idxWord == null) {
			return null;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		Optional<Pair<GrammaticalRelation, IndexedWord>> child = childPairs.stream()
				.filter(p -> relations.contains(p.first.getShortName()) && pos.contains(p.second.tag())).findFirst();

		if (child.isPresent()) {
			return child.get();
		}

		return null;
	}

	public static List<Pair<GrammaticalRelation, IndexedWord>> getChildrenByRelationPosAndLemma(
			SemanticGraph dependencies, IndexedWord idxWord, Set<String> relations, Set<String> pos,
			Set<String> lemmas) {

		if (idxWord == null) {
			return null;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependencies.childPairs(idxWord);

		List<Pair<GrammaticalRelation, IndexedWord>> children = childPairs.stream()
				.filter(p -> relations.contains(p.first.getShortName()) && pos.contains(p.second.tag())
						&& lemmas.contains(p.second.lemma()))
				.collect(Collectors.toList());

		return children;
	}

	public static List<IndexedWord> getParentsbyRelation(SemanticGraph dependencies, IndexedWord vertex,
			String... relations) {

		List<Pair<GrammaticalRelation, IndexedWord>> pars = dependencies.parentPairs(vertex);
		List<String> relsSet = Arrays.asList(relations);
		List<Pair<GrammaticalRelation, IndexedWord>> parsFiltered = pars.stream()
				.filter(p -> relsSet.contains(p.first.getShortName())).collect(Collectors.toList());

		Set<IndexedWord> parents = new LinkedHashSet<>();
		for (Pair<GrammaticalRelation, IndexedWord> parPair : parsFiltered) {
			parents.add(parPair.second);
		}

		return new ArrayList<>(parents);
	}

}
