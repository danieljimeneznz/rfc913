import java.util.Base64;

/**
 * Classes used for deserialization of JSON Objects.
 */
class Users {
    private User[] users;

    Users(User[] users) {
        this.users = users;
    }

    User getUser(String id) {
        for (User user : users) {
            if (user.id.equals(id)) {
                return user;
            }
        }
        return null;
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
