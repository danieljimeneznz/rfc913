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
}

class User {
    String id;
    String acct;
    private String pass;

    String decryptPassword() {
        return new String(Base64.getDecoder().decode(this.pass));
    }
}
