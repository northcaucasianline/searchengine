package searchengine.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;

    public enum Status {
        INDEXING, INDEXED, FAILED
    }
}
