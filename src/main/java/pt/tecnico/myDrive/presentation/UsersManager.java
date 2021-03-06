package pt.tecnico.myDrive.presentation;

import pt.tecnico.myDrive.exception.UserManagerPresentationException;

import java.util.HashMap;

public class UsersManager {

    private HashMap<String,Long> users;
    private String currentUsername;
    private Long currentToken;

    private static final String GET_TOKEN_MSG = "Presentation : getTokenByUsername - ";
    private static final String SET_CURRENT_USERNAME_MSG  = "Presentation : setCurrentUsername - ";

    private static final String NOT_LOGGED_SYSTEM_MSG = " - has not been previously logged into the system.";

    private static final String GET_TOKEN_HELP_MSG = "\t\t- First add username and its token : addUser(";

    public UsersManager(){
        users = new HashMap<String, Long>();
    }

    private void checkUserExistence(String username, String exMsg) throws UserManagerPresentationException {
        if (!users.containsKey(username))
            throw new UserManagerPresentationException(exMsg);
    }

    public void addUser(String username,Long token) {
        users.put(username,token);
        currentUsername = username;
        currentToken = token;
    }


    public Long getTokenByUsername(String username) throws UserManagerPresentationException {
        checkUserExistence(username,GET_TOKEN_MSG + username + NOT_LOGGED_SYSTEM_MSG + GET_TOKEN_HELP_MSG
                + username + ",token[Long])\n");

        return users.get(username);
    }


    public void setCurrentUsername(String username) throws UserManagerPresentationException{
        checkUserExistence(username,SET_CURRENT_USERNAME_MSG + username + NOT_LOGGED_SYSTEM_MSG + "\n");

        currentUsername = username;
        currentToken = users.get(username);
    }

    public String getCurrentUsername(){
        return currentUsername;
    }

    public Long getCurrentToken(){
        return currentToken;
    }

}
