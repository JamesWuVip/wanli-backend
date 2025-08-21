package com.wanli.backend.controller;

import com.wanli.backend.service.GoogleDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/docs")
@CrossOrigin(origins = "*")
public class GoogleDocsController {

    @Autowired
    private GoogleDocsService googleDocsService;

    /**
     * 检查Google Docs集成状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", googleDocsService.isEnabled());
        status.put("service", "Google Docs Integration");
        return ResponseEntity.ok(status);
    }

    /**
     * 通过文档ID读取Google Docs内容
     */
    @GetMapping("/read/{documentId}")
    public ResponseEntity<Map<String, Object>> readDocument(@PathVariable String documentId) {
        try {
            String content = googleDocsService.readDocument(documentId);
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("content", content);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to read document: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        } catch (UnsupportedOperationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 通过URL读取Google Docs内容
     */
    @PostMapping("/read-by-url")
    public ResponseEntity<Map<String, Object>> readDocumentByUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "URL is required");
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String documentId = googleDocsService.extractDocumentId(url);
            String content = googleDocsService.readDocumentByUrl(url);
            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("documentId", documentId);
            response.put("content", content);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid Google Docs URL: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to read document: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        } catch (UnsupportedOperationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 在文档中搜索关键词
     */
    @PostMapping("/search/{documentId}")
    public ResponseEntity<Map<String, Object>> searchKeywords(
            @PathVariable String documentId,
            @RequestBody Map<String, List<String>> request) {
        
        List<String> keywords = request.get("keywords");
        if (keywords == null || keywords.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Keywords are required");
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<String> results = googleDocsService.searchKeywords(documentId, keywords);
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("keywords", keywords);
            response.put("results", results);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to search document: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        } catch (UnsupportedOperationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 从URL提取文档ID
     */
    @PostMapping("/extract-id")
    public ResponseEntity<Map<String, Object>> extractDocumentId(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "URL is required");
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            String documentId = googleDocsService.extractDocumentId(url);
            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("documentId", documentId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid Google Docs URL: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }
}