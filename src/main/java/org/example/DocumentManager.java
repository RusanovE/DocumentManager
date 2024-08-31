package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String,Document> documentRepository = new HashMap<>();  // I'm using a map as a mock of a real database for simplicity and clarity

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {

        if (document.getId() != null && !document.getId().isEmpty()) {      // Check if the document has an ID
            if (findById(document.getId()).isPresent()) {                   // Check if a document with this ID exists in the repository

                Document existDocument = findById(document.getId()).get();
                document.setCreated(existDocument.getCreated());            // Take the [created] field from the existing document
                documentRepository.put(document.getId(), document);         // and update the document in the repository
                return document;

            } else {
                documentRepository.put(document.getId(), document);         // Simply add the document to the repository,
            }                                                               // if it already has an ID and it's not in the database
        } else {
            String uniqueId = UUID.randomUUID().toString();                 // If the document has no ID, generate a new unique ID for it
            document.setId(uniqueId);
            documentRepository.put(document.getId(), document);
        }
        return document;
    }


    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentRepository.values().stream()
                .filter(doc -> matchesTitlePrefixes(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContent(doc, request.getContainsContents()))
                .filter(doc -> matchesAuthor(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .toList();
    }

    // Checks if the document's title starts with any of the given prefixes
    private boolean matchesTitlePrefixes(Document doc, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) return true;
        return prefixes.stream().anyMatch(prefix -> doc.getTitle() != null && doc.getTitle().startsWith(prefix));
    }

    // Checks if the document contains any of the given keywords in his content
    private boolean matchesContent(Document doc, List<String> contents) {
        if (contents == null || contents.isEmpty()) return true;
        return contents.stream().anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content));
    }

    // Checks if the document is authored by any of the given authors
    private boolean matchesAuthor(Document doc, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return true;
        return doc.getAuthor() != null && authorIds.contains(doc.getAuthor().getId());
    }

    // Checks if the document was created within the given date range
    private boolean matchesCreatedRange(Document doc, Instant createdFrom, Instant createdTo) {
        if (createdFrom != null && (doc.getCreated() == null || doc.getCreated().isBefore(createdFrom))) return false;
        if (createdTo != null && (doc.getCreated() == null || doc.getCreated().isAfter(createdTo))) return false;
        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return Optional.ofNullable(documentRepository.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}