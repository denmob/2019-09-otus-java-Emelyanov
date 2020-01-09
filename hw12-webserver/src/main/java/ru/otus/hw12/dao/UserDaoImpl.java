package ru.otus.hw12.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw12.model.User;
import ru.otus.hw12.repository.UserRepository;
import ru.otus.hw12.services.SequenceGeneratorService;

import java.util.List;
import java.util.Optional;


@Service
public class UserDaoImpl implements UserDao {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoOperations mongoOperations;


    @Override
    public Optional<User> findByUserId(Long userId) {
       return Optional.ofNullable(userRepository.findByUserId(userId));
    }

    @Override
    public Optional<User> findByUserLogin(String userLogin) {
        return Optional.ofNullable(userRepository.findByUserLogin(userLogin));
    }

    @Override
    public List<User> getAllUsers(Long id) {
        return userRepository.getAllByIdExists();
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        long id  = user.getUserId();
        if (id==0) {
           user.setUserId(SequenceGeneratorService.getNextSequence(mongoOperations, "customSequences"));
           userRepository.save(user);
        } else {
            Optional<User> foundUser = findByUserId(id);
            if (foundUser.isEmpty())
                userRepository.save(user);
        }
    }
}