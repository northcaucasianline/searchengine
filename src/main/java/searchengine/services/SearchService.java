package searchengine.services;

import searchengine.models.Index;
import searchengine.models.Lemma;
import searchengine.models.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private LemmatizationService lemmatizationService;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private PageRepository pageRepository;

    public List<SearchResult> search(String query) {
        Set<String> lemmas = lemmatizationService.getLemmas(query);
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        List<Lemma> foundLemmas = lemmaRepository.findByLemmaIn(lemmas);
        if (foundLemmas.isEmpty()) {
            return Collections.emptyList();
        }

        foundLemmas.sort(Comparator.comparingInt(Lemma::getFrequency));
        List<Page> relevantPages = findRelevantPages(foundLemmas);
        if (relevantPages.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Page, Float> relevanceMap = calculateRelevance(relevantPages, foundLemmas);
        return formatSearchResults(relevanceMap);
    }

    private List<Page> findRelevantPages(List<Lemma> lemmas) {
        List<Page> pages = new ArrayList<>();
        for (Lemma lemma : lemmas) {
            List<Index> indices = indexRepository.findByLemma(lemma);
            if (indices.isEmpty()) {
                return Collections.emptyList();
            }
            List<Page> pagesWithLemma = indices.stream().map(Index::getPage).collect(Collectors.toList());
            if (pages.isEmpty()) {
                pages.addAll(pagesWithLemma);
            } else {
                pages.retainAll(pagesWithLemma);
            }
            if (pages.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return pages;
    }

    private Map<Page, Float> calculateRelevance(List<Page> pages, List<Lemma> lemmas) {
        Map<Page, Float> relevanceMap = new HashMap<>();
        float maxRelevance = 0;

        for (Page page : pages) {
            float absoluteRelevance = 0;
            for (Lemma lemma : lemmas) {
                Index index = indexRepository.findByPageAndLemma(page, lemma);
                if (index != null) {
                    absoluteRelevance += index.getRank();
                }
            }
            relevanceMap.put(page, absoluteRelevance);
            if (absoluteRelevance > maxRelevance) {
                maxRelevance = absoluteRelevance;
            }
        }

        for (Map.Entry<Page, Float> entry : relevanceMap.entrySet()) {
            relevanceMap.put(entry.getKey(), entry.getValue() / maxRelevance);
        }

        return relevanceMap;
    }

    private List<SearchResult> formatSearchResults(Map<Page, Float> relevanceMap) {
        return relevanceMap.entrySet().stream()
                .sorted(Map.Entry.<Page, Float>comparingByValue().reversed())
                .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static class SearchResult {
        public String url;
        public String title;
        public String snippet;
        public float relevance;

        public SearchResult(Page page, float relevance) {
            this.url = page.getPath();
            this.title = extractTitle(page.getContent());
            this.snippet = extractSnippet(page.getContent());
            this.relevance = relevance;
        }

        private String extractTitle(String content) {
            return content.split("\n")[0];
        }

        private String extractSnippet(String content) {
            return content.length() > 150 ? content.substring(0, 150) + "..." : content;
        }
    }
}
