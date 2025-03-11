package searchengine.repositories;

import searchengine.models.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.models.Site;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    List<Lemma> findByLemmaIn(Set<String> lemmas);
    int countBySite(Site site);
}
