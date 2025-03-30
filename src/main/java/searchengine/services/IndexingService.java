package searchengine.services;

import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.models.Lemma;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class IndexingService {

    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private volatile boolean isIndexing = false;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private LemmatizationService lemmatizationService;

    @Autowired
    private SitesList sitesList;


    /**
     * Запуск индексации для всех сайтов, сохранённых в базе данных.
     */
    public void startIndexingAllSites() {
        if (isIndexing) {
            return;
        }
        isIndexing = true;
        System.out.println("1");

        for (SiteConfig site : sitesList.getSites()) {
            executor.submit(() -> startIndexing(site));
        }
    }

    /**
     * Остановка индексации.
     */
    public void stopIndexing() {
        isIndexing = false;
        executor.shutdownNow();
    }

    /**
     * Индексация одного сайта.
     *
     * @param siteConfig Сайт, для которого нужно запустить индексацию.
     */
    public void startIndexing(SiteConfig siteConfig) {
        Site site = new Site();
        System.out.println("2");

        site.setUrl(siteConfig.getUrl());
        site.setName(siteConfig.getName());
        site.setStatus(Site.Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        Set<String> visitedUrls = new HashSet<>();
        recursiveIndex(site, site.getUrl(), visitedUrls);

        site.setStatus(Site.Status.INDEXED);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    /**
     * Индексация одной страницы.
     *
     * @param url URL страницы для индексации.
     * @return true, если индексация прошла успешно, иначе false.
     */
    public boolean indexSinglePage(String url) {
        // Находим сайт по URL
        Site site = siteRepository.findAll().stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst()
                .orElse(null);

        if (site == null) {
            return false; // Не найден сайт для данного URL
        }

        Set<String> visitedUrls = new HashSet<>();
        try {
            recursiveIndex(site, url, visitedUrls);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Рекурсивный обход страниц сайта.
     *
     * @param site        Сайт, который индексируем.
     * @param url         Текущий URL для обхода.
     * @param visitedUrls Множество уже посещённых URL, чтобы избежать повторов.
     */
    private void recursiveIndex(Site site, String url, Set<String> visitedUrls) {
        if (!isIndexing) {
            return; // Если индексация остановлена, прерываем обход
        }
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);

        try {
            // Настроим User-Agent и реферер для обхода
            Document doc = Jsoup.connect(url)
                    .userAgent("SearchEngineBot")
                    .referrer("http://www.google.com")
                    .get();
            String content = doc.body().text();

            // Сохраняем страницу в базе данных
            Page page = new Page();
            page.setSite(site);
            page.setPath(url);  // Сохраняем URL
            page.setContent(content);
            page.setCode(200);  // Статус успешной загрузки
            pageRepository.save(page);

            // Лемматизация текста страницы и сохранение лемм
            Set<String> lemmas = lemmatizationService.getLemmas(content);
            for (String lemmaStr : lemmas) {
                Lemma lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaStr);
                lemma.setFrequency(1); // В базовой версии ставим 1
                lemmaRepository.save(lemma);
            }

            // Рекурсивный обход по всем ссылкам на странице, принадлежащим данному сайту
            doc.select("a[href]").forEach(link -> {
                String nextUrl = link.absUrl("href");
                if (nextUrl.startsWith(site.getUrl())) { // Ограничиваем обход страницами данного сайта
                    recursiveIndex(site, nextUrl, visitedUrls);
                }
            });

        } catch (IOException e) {
            e.printStackTrace(); // Логируем ошибку, если не удалось получить страницу
        }
    }
}
