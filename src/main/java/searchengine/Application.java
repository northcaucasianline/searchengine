package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//проверить поиск
//- добавить английский поиск
//- LemmatizationService написать (рус/англ)
//- посмотреть почему индексация заканчивается на 1 странице
//- индексация по id (закоментирована в indexingcontroller)

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
