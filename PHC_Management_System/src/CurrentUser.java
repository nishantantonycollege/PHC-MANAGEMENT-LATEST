public class CurrentUser {
    private static String username = "";
    private static String role = "Employee";

    public static void set(String user, String userRole) {
        username = user;
        role = userRole;
    }

    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static boolean isManager() { return "Manager".equals(role); }
    public static void clear() { username = ""; role = "Employee"; }
}
