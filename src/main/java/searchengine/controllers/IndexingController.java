package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import searchengine.models.Site;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;

import java.util.Map;

@RequestMapping("/api")
@RestController
public class IndexingController {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private IndexingService indexingService;

    @GetMapping("/startIndexing")
    public Map<String, Object> startIndexing() {
        new Thread(() -> indexingService.startIndexingAllSites()).start();
        return Map.of("result", true);
    }

    @GetMapping("/stopIndexing")
    public Map<String, Object> stopIndexing() {
        indexingService.stopIndexing();
        return Map.of("result", true);
    }

    @PostMapping("/indexPage")
    public Map<String, Object> indexPage(@RequestParam String url) {
        boolean success = indexingService.indexSinglePage(url);
        return Map.of("result", success);
    }

//    @GetMapping("/api/startIndexing/{siteId}")
//    public String startIndexing(@PathVariable Long siteId) {
//        Site site = siteRepository.findById(siteId).orElse(null);
//        if (site == null) {
//            return "Site not found!";
//        }
//        indexingService.startIndexing(site);
//        return "Indexing started for site: " + site.getName();
//    }

}
