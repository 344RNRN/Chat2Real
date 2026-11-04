
package bean;

import java.io.Serializable;

import org.mindrot.jbcrypt.BCrypt;


public class AdminLogin implements Serializable {

    private static final long serialVersionUID = 1L;

    private long accountId;
    private String loginId;
    private String accountName;

    public AdminLogin() {
    }

    public AdminLogin(long accountId, String loginId, String accountName) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.accountName = accountName;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    //test
    public static void main(String[] args) {
        String password = "Admin@12345";

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
    }
}
