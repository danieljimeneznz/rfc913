import java.util.Base64;

/**
 * Class used for deserialization of JSON Objects.
 */
class User {
    String id;
    String acct;
    String pass;

    /**
     * Decrypts the password from its BASE64 encoding.
     * @return  the decrypted password.
     */
    String decryptPassword() {
        return new String(Base64.getDecoder().decode(this.pass));
    }
}
