package cs1302.api;

/** Represents artist Tag  object. */
public class TagsResponse {
    Toptags toptags;

    /** Returns top tagobject. */
    public class Toptags {
        Tags[] tag;

        /** Represents tags object. */
        public class Tags {
            String name;
        } // Tags

    } // Toptags


} // TagsResponse
