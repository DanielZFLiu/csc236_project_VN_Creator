public class AdminUser extends User {
    private String password;

    public AdminUser(String username, String password) {
        super(username);
        this.password = password;
    }
    public String getPassworc() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
