package cback;

public enum OfficialRoles {
    STAFF("STAFF", 266651534635433985l),
    ADMIN("ADMIN", 266650410373218305l),
    HOST("HOST", 279962204135227393l),
    MOD("MOD", 266651027665584128l);

    public String name;
    public Long id;

    OfficialRoles(String name, Long id) {
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