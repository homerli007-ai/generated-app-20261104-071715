package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody List<String> paths) {
        List<String> deleted = new ArrayList<>();
        List<Map<String, String>> refused = new ArrayList<>();

        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "allowed_root not accessible: " + allowedRoot));
        }

        for (String p : paths) {
            try {
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) {
                    refused.add(Map.of("path", p, "reason", "outside allowed_root"));
                    continue;
                }
                if (Files.isSymbolicLink(target)) {
                    refused.add(Map.of("path", p, "reason", "symlink refused"));
                    continue;
                }
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", p, "reason", "does not exist"));
                    continue;
                }
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", p, "reason", "not a regular file"));
                    continue;
                }
                Files.delete(target);
                deleted.add(p);
            } catch (Exception e) {
                refused.add(Map.of("path", p, "reason", e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", deleted);
        body.put("refused", refused);
        return ResponseEntity.ok(body);
    }
}
