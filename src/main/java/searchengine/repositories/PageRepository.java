package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.models.Page;
import org.springframework.stereotype.Repository;
import searchengine.models.Site;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    int countBySite(Site site);
}
