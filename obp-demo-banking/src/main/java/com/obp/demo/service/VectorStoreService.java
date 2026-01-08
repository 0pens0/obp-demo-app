package com.obp.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * Add chat history to vector store for RAG retrieval
     */
    public void addChatHistory(String username, String question, String answer, Map<String, Object> metadata) {
        try {
            String content = String.format("Question: %s\nAnswer: %s", question, answer);
            Document document = new Document(content, metadata);
            vectorStore.add(List.of(document));
            log.debug("Added chat history to vector store for user: {}", username);
        } catch (Exception e) {
            log.error("Error adding chat history to vector store: {}", e.getMessage(), e);
        }
    }

    /**
     * Search for similar chat history or context
     */
    public List<Document> searchSimilar(String query, int maxResults) {
        try {
            return vectorStore.similaritySearch(query, maxResults);
        } catch (Exception e) {
            log.error("Error searching vector store: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
