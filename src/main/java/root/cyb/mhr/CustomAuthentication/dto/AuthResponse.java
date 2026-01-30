package root.cyb.mhr.CustomAuthentication.dto;

public class AuthResponse {

    private String message;
    private String token;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String token, String email) {
        this.message = message;
        this.token = token;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
