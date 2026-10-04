package com.jobfiller.Controller;

import com.jobfiller.model.CompanyLogin;
import com.jobfiller.service.CompanyLoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("logins")
@CrossOrigin(origins = "*")
public class CompanyLoginController {

    @Autowired
    private CompanyLoginService loginService;

    // ================================
    // GET ALL LOGINS
    // ================================
    @GetMapping
    public ResponseEntity<List<CompanyLogin>> getAllLogins() {
        return ResponseEntity.ok(
                loginService.getAllLogins());
    }

    // ================================
    // SAVE LOGIN
    // ================================
    @PostMapping
    public ResponseEntity<CompanyLogin> saveLogin(
            @RequestBody CompanyLogin login) {
        return ResponseEntity.ok(
                loginService.saveLogin(login));
    }

    // ================================
    // DELETE LOGIN
    // ================================
    @DeleteMapping("/{portalUrl}")
    public ResponseEntity<Void> deleteLogin(
            @PathVariable String portalUrl) {
        loginService.deleteLogin(portalUrl);
        return ResponseEntity.ok().build();
    }

    // ================================
    // CHECK IF LOGIN EXISTS
    // ================================
    @GetMapping("/exists/{portalUrl}")
    public ResponseEntity<Boolean> loginExists(
            @PathVariable String portalUrl) {
        return ResponseEntity.ok(
                loginService.loginExists(portalUrl));
    }
}
