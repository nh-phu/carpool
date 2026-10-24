package com.carpool.backend.user;

import com.carpool.backend.exception.DupPhoneNumberExp;
import com.carpool.backend.user.UserRepo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    public User create(CreateUserReq req) {
        if (userRepo.findByPhoneNumber(req.phoneNumber()).isPresent()) {
            throw new DupPhoneNumberExp("This phone number is already registered");
        }
        User User = new User(
                req.name(),
                req.phoneNumber()
        );
        return userRepo.save(User);
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }
    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User update(Long id, CreateUserReq req) {
        User User = findById(id);
        if (User == null) {
            return null;
        }

        User.setName(req.name());
        User.setPhoneNumber(req.phoneNumber());

        return userRepo.save(User);
    }

    public boolean delete(Long id) {
        User user = findById(id);
        if (user == null) {
            return false;
        }
        userRepo.delete(user);
        return true;
    }
}
