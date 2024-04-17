package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public HogwartsUser save(HogwartsUser newUser) {
        return this.userRepository.save(newUser);
    }

    public HogwartsUser findById(Integer userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(HogwartsUser.class.getSimpleName(), userId));
    }

    public List<HogwartsUser> findAll() {
        return this.userRepository.findAll();
    }

    public HogwartsUser update(Integer userId, HogwartsUser update) {
        var oldUser = findById(userId);
        oldUser.setUsername(update.getUsername());
        oldUser.setEnable(update.isEnable());
        oldUser.setRoles(update.getRoles());
        return this.userRepository.save(oldUser);
    }

    public void delete(Integer userId) {
        findById(userId);
        this.userRepository.deleteById(userId);
    }
}