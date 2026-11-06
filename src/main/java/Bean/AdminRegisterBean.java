package Bean;

import java.io.Serializable;

/**
 * 運営者アカウント作成画面（adminRegister.jsp）の入力内容
 * AdminRegisterAction で作り、AdminRegisterDAO に渡して t001_account に登録する
 *
 * ※平文のパスワードは持たない（Action で BCrypt に変換してから入れる）
 */
public class AdminRegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long accountId;        // 登録後に入る
    private String accountName;    // 運営者名（団体名）→ account_name
    private String loginId;        // メールアドレス   → login_id
    private String passwordHash;   // BCrypt のハッシュ → password_hash

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}