package com.auth.jwt.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo", description = "Endpoints for demo purposes")
public class DemoController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a PUBLIC endpoint — no authentication needed.";
    }

    @GetMapping("/secured")
    public String securedEndpoint() {
        return "This is a SECURED endpoint — you are authenticated!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "This is an ADMIN endpoint — you have admin privileges!";
    }
}
