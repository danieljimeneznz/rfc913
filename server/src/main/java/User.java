import java.util.Base64;

/**
 * Classes used for deserialization of JSON Objects.
 */
class Users {
    private User[] users;

    Users(User[] users) {
        this.users = users;
    }

    User getUserByID(String id) {
        for (User user : users) {
            if (user.id.equals(id)) {
                return user;
            }
        }
        return null;
    }

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

class User {
    String id;
    String acct;
    String pass;

    String decryptPassword() {
        return new String(Base64.getDecoder().decode(this.pass));
    }
}
