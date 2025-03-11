package searchengine.repositories;

import searchengine.models.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.models.Lemma;
import searchengine.models.Page;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {
    List<Index> findByLemma(Lemma lemma);
    Index findByPageAndLemma(Page page, Lemma lemma);
}



