package com.carpool.backend.user;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User User = this.userService.findById(id);
        if (User == null)
            return ResponseEntity.notFound().build();
        return  ResponseEntity.ok(User);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserReq req
    ) {
        User User = userService.update(id, req);
        if (User == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(User);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean delete = userService.delete(id);
        if (!delete) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    public ResponseEntity<User> create(
            @Valid @RequestBody CreateUserReq req
    ) {
        User User = userService.create(req);

        return ResponseEntity
                .created(URI.create("/api/users/" + User.getId()))
                .body(User);
    }
}
