package searchengine.controllers;

import searchengine.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public List<SearchService.SearchResult> search(@RequestParam String query) {
        return searchService.search(query);
    }


}
