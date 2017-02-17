package cback;

public enum OfficialRoles {
    STAFF("STAFF", "266651534635433985"),
    ADMIN("ADMIN", "266650410373218305"),
    HOST("HOST", "279962204135227393"),
    MOD("MOD", "266651027665584128");

    public String name;
    public String id;

    OfficialRoles(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static OfficialRoles getRole(String name) {
        for (OfficialRoles role : values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}