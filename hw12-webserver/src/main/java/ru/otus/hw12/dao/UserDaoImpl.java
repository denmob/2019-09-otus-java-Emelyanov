package ru.otus.hw12.dao;

import com.mongodb.client.MongoCollection;
import ru.otus.hw12.dbmanager.DBManager;
import ru.otus.hw12.model.User;

import java.util.List;
import java.util.Optional;


public class UserDaoImpl implements UserDao {

    private final MongoCollection<User> usersCollection;

    public UserDaoImpl(DBManager userDBManager) {
        this.usersCollection = userDBManager.getMongoDatabase().getCollection("user",User.class);
    }

    @Override
    public Optional<User> findByUserLogin(String userLogin) {
      //  return Optional.ofNullable(mongoDatabase.findByUserLogin(userLogin));
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return null;
       // return mongoDatabase.findAll();
    }

    @Override
    public void saveUser(User user) {
       // if (checkUserData(user)) {
         //   prepareUserToSave(user);
         //   mongoDatabase. save(user);
       // }

        usersCollection.insertOne(user);
    }

    private boolean checkUserData(User user) {
        Optional<User> foundUser = findByUserLogin(user.getLogin());
        return foundUser.isEmpty();
    }

    private void prepareUserToSave(User user) {
        if (user.getId() == 0) {
           // user.setUserId(SequenceGeneratorService.getNextSequence(mongoOperations, "customSequences"));
        }
    }


}
