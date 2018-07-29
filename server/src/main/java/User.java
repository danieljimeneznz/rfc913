import java.util.Base64;

/**
 * Class used for deserialization of JSON Objects.
 */
class User {
    String id;
    String acct;
    String pass;

    String decryptPassword() {
        return new String(Base64.getDecoder().decode(this.pass));
    }
}
