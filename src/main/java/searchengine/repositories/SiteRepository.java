package searchengine.repositories;

import searchengine.models.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    boolean existsByStatus(Site.Status status);
}
