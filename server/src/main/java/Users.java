/**
 * Class used for deserialization of JSON Objects.
 */
class Users {
    private User[] users;

    Users(User[] users) {
        this.users = users;
    }

    /**
     * Find the user in the current list of users that matches an id given.
     * @param id    the id of the user to find.
     * @return      the user or null if not found.
     */
    User getUserByID(String id) {
        for (User user : users) {
            if (user.id.equals(id)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Find the user in the current list of users that matches an account given.
     * @param acct  the acct of the user to find.
     * @return      the user or null if not found.
     */
    User getUserByAcct(String acct) {
        for (User user : users) {
            if (user.acct.equals(acct)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Checks to see if the password is a valid users password.
     * @param pass  the password to check.
     * @return      whether the password matches any in the users file.
     */
    boolean checkPass(String pass) {
        for (User user : users) {
            if (user.decryptPassword().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a user based on their id, account and password.
     *
     * @param id    the id of the user to find.
     * @param acct  the account of the user to find
     * @param pass  the password of the user to find.
     * @return      the user that matches all three criteria or null if not found.
     */
    User getUser(String id, String acct, String pass) {
        if (id == null || acct == null || pass == null) {
            return null;
        }

        for (User user: users) {
            if (user.id.equals(id) && user.acct.equals(acct) && user.decryptPassword().equals(pass)) {
                return user;
            }
        }
        return null;
    }
}