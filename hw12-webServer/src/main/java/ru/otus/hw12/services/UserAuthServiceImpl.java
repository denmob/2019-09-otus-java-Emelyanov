package ru.otus.hw12.services;


import ru.otus.hw12.dao.UserDao;

public class UserAuthServiceImpl implements UserAuthService {

  private final UserDao userDao;

  public UserAuthServiceImpl(UserDao userDao) {
    this.userDao = userDao;
  }

  @Override
  public boolean authenticate(String login, String password) {
    return userDao.findByUserLogin(login)
        .map(user -> user.getPassword().equals(password))
        .orElse(false);
  }
}
