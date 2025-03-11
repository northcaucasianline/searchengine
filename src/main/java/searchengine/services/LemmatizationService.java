package searchengine.services;

import org.apache.lucene.analysis.morfologik.MorfologikFilterFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.morfologik.MorfologikAnalyzer;


@Service
public class LemmatizationService {

    private final Analyzer analyzer;

    public LemmatizationService() {
        // Используем MorfologikAnalyzer напрямую, без фабрики
        this.analyzer = new MorfologikAnalyzer();
    }

    public Set<String> getLemmas(String text) {
        Set<String> lemmas = new HashSet<>();
        try {
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                lemmas.add(charTermAttribute.toString());
            }
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lemmas;
    }
}
