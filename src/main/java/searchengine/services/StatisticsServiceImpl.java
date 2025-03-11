package searchengine.services;

import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.models.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    /**
     * Метод для получения статистики о проиндексированных данных.
     *
     * @return StatisticsResponse со сводной информацией
     */
    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites((int) siteRepository.count());
        total.setPages((int) pageRepository.count());
        total.setLemmas((int) lemmaRepository.count());

        // Проверяем, есть ли сайты, которые сейчас индексируются
        boolean isIndexing = siteRepository.existsByStatus(Site.Status.INDEXING);
        total.setIndexing(isIndexing);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = siteRepository.findAll();

        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setPages(pageRepository.countBySite(site));
            item.setLemmas(lemmaRepository.countBySite(site));
            item.setStatus(site.getStatus().name());
            item.setError(site.getLastError() != null ? site.getLastError() : "");
            item.setStatusTime(site.getStatusTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000);

            detailed.add(item);
        }

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);

        return response;
    }
}
