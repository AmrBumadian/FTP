public class AuthenticationToken {
	boolean isAuthenticated;
	String user;

	public AuthenticationToken(boolean isAuthenticated, String userPath) {
		this.isAuthenticated = isAuthenticated;
		this.user = userPath;
	}
}
