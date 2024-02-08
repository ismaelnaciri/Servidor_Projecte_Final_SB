package cat.insvidreres.imp.m13projecte.utils;

public enum CollectionName {
    USER("users"),
    POST("posts"),
    LIKE("likes"),
    COMMENT("comments");

    private final String TEXT;

    CollectionName(final String TEXT) {
        this.TEXT = TEXT;
    }

    @Override
    public String toString() {
        return TEXT;
    }
}
