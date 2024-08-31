

import org.example.DocumentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    private DocumentManager manager;

    @BeforeEach
    public void setUp() {
        manager = new DocumentManager();

        // Create test authors
        DocumentManager.Author author1 = DocumentManager.Author.builder()
                .id("author1")
                .name("Author One")
                .build();

        DocumentManager.Author author2 = DocumentManager.Author.builder()
                .id("author2")
                .name("Author Two")
                .build();

        // Create test documents
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .id("doc1")
                .title("Introduction to Java")
                .content("Java is a high-level, class-based, object-oriented programming language.")
                .author(author1)
                .created(Instant.now().minusSeconds(3600))  // Document created 1 hour ago
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .id("doc2")
                .title("Advanced section")
                .content("In this section, we dive deeper into Java Streams and Lambdas.")
                .author(author2)
                .created(Instant.now().minusSeconds(7200))  // Document created 2 hours ago
                .build();

        DocumentManager.Document doc3 = DocumentManager.Document.builder()
                .id("doc3")
                .title("Java Streams")
                .content("Streams are a new abstraction that lets you process data in a declarative way.")
                .author(author1)
                .created(Instant.now().minusSeconds(1800))  // Document created 30 minutes ago
                .build();

        // Save documents
        manager.save(doc1);
        manager.save(doc2);
        manager.save(doc3);
    }

    @Test
    public void testFindById() {
        Optional<DocumentManager.Document> foundDoc = manager.findById("doc2");
        assertTrue(foundDoc.isPresent(), "Document with ID 'doc2' should be found");
        assertEquals("Advanced section", foundDoc.get().getTitle(), "Title should match 'Advanced section'");
    }

    @Test
    public void testSearchByTitlePrefix() {
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .build();

        List<DocumentManager.Document> searchResults = manager.search(searchRequest);
        assertEquals(1, searchResults.size(), "There should be 1 document found with title prefix 'Java'");
        assertTrue(searchResults.stream().anyMatch(doc -> doc.getTitle().equals("Java Streams")));
    }

    @Test
    public void testSearchByContent() {
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("Streams"))
                .build();

        List<DocumentManager.Document> searchResults = manager.search(searchRequest);
        assertEquals(2, searchResults.size(), "There should be 2 documents found containing 'Streams'");
        assertTrue(searchResults.stream().anyMatch(doc -> doc.getTitle().equals("Advanced section")));
        assertTrue(searchResults.stream().anyMatch(doc -> doc.getTitle().equals("Java Streams")));
    }

    @Test
    public void testSearchByAuthor() {
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("author1"))
                .build();

        List<DocumentManager.Document> searchResults = manager.search(searchRequest);
        assertEquals(2, searchResults.size(), "There should be 2 documents found by 'author1'");
        assertTrue(searchResults.stream().anyMatch(doc -> doc.getTitle().equals("Introduction to Java")));
        assertTrue(searchResults.stream().anyMatch(doc -> doc.getTitle().equals("Java Streams")));
    }

    @Test
    public void testSearchByMultipleCriteria() {
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .containsContents(List.of("Streams"))
                .authorIds(List.of("author1"))
                .createdFrom(Instant.now().minusSeconds(4000))
                .createdTo(Instant.now())
                .build();

        List<DocumentManager.Document> searchResults = manager.search(searchRequest);
        assertEquals(1, searchResults.size(), "There should be 1 document found matching all criteria");
        assertEquals("Java Streams", searchResults.get(0).getTitle(), "The document found should be 'Java Streams'");
    }

    @Test
    public void testSearchByAllCriteria() {
        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Advanced"))
                .containsContents(List.of("we dive deeper into"))
                .authorIds(List.of("author2"))
                .createdFrom(Instant.now().minusSeconds(8000))
                .createdTo(Instant.now().minusSeconds(6000))
                .build();

        List<DocumentManager.Document> searchResults = manager.search(searchRequest);
        assertEquals(1, searchResults.size(), "There should be 1 document found matching all criteria");
        assertEquals("Advanced section", searchResults.get(0).getTitle(), "The document found should be 'Advanced section'");
    }
}
