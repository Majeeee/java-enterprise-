package se.scb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docs")
public class ApiDocsController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDocs() {
        return ResponseEntity.ok(Map.of(
                "title", "SCB Migration API",
                "version", "1.0.0",
                "baseUrl", "/api",
                "endpoints", buildEndpointList()
        ));
    }

    private List<Map<String, Object>> buildEndpointList() {
        return List.of(
                endpoint("POST", "/auth/register", "public",
                        "Register a new user account. Account is inactive until an admin enables it.",
                        Map.of("body", "{ email, password, firstName, lastName }"),
                        "{ message: 'Registration successful' }"),

                endpoint("POST", "/auth/login", "public",
                        "Authenticate and receive a JWT stored in an HttpOnly cookie.",
                        Map.of("body", "{ email, password }"),
                        "{ email, firstName, lastName, role }"),

                endpoint("POST", "/auth/logout", "authenticated",
                        "Invalidate the session by clearing the JWT cookie.",
                        Map.of(),
                        "{ message: 'Logged out' }"),

                endpoint("GET", "/migrations", "ROLE_USER, ROLE_ADMIN",
                        "Fetch migration rows with optional filters. All params are optional.",
                        Map.of(
                                "region",   "string — county name, e.g. 'Stockholms län'",
                                "gender",   "string — 'men' or 'women'",
                                "ageGroup", "string — e.g. '20-29'",
                                "year",     "integer — 1997–2024"
                        ),
                        "[ { id, region, regionCode, gender, ageGroup, year, immigrations, emigrations, netMigration } ]"),

                endpoint("GET", "/migrations/filters", "ROLE_USER, ROLE_ADMIN",
                        "Returns distinct values available for each filter dimension.",
                        Map.of(),
                        "{ regions: [...], ageGroups: [...], years: [...] }"),

                endpoint("GET", "/admin/users", "ROLE_ADMIN",
                        "List all registered user accounts.",
                        Map.of(),
                        "[ { id, email, firstName, lastName, role, enabled } ]"),

                endpoint("PUT", "/admin/users/{id}/enable", "ROLE_ADMIN",
                        "Enable a user account so they can log in.",
                        Map.of("id", "path — user ID"),
                        "{ id, email, firstName, lastName, role, enabled: true }"),

                endpoint("DELETE", "/admin/users/{id}", "ROLE_ADMIN",
                        "Permanently delete a user account.",
                        Map.of("id", "path — user ID"),
                        "204 No Content"),

                endpoint("GET", "/docs", "public",
                        "This documentation page.",
                        Map.of(),
                        "{ title, version, baseUrl, endpoints: [...] }")
        );
    }

    private Map<String, Object> endpoint(String method, String path, String role,
                                          String description, Map<String, String> params,
                                          String exampleResponse) {
        return Map.of(
                "method", method,
                "path", "/api" + path,
                "requiredRole", role,
                "description", description,
                "params", params,
                "exampleResponse", exampleResponse
        );
    }
}
