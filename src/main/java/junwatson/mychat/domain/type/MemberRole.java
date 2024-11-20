package junwatson.mychat.domain.type;

public enum MemberRole {
    USER("ROLE_USER"), ADMIN("ROLE_ADMIN");

    private final String key;

    MemberRole(String key) {
        this.key = key;
    }
}
