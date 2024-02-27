package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a response from the LastFM Search API.
 */
public class FMResponse {
    Track track;

    /** Track object. */
    public class Track {
        String url;
        String name;
        Album album;
        Artist artist;
        Toptags toptags;
    } // Track


    /** Represents album object. */
    public class Album {
        Images[] image;

        /** Images object. */
        public class Images {
            String size;
            @SerializedName("#text") String text;
//        String #text;
        } // Images

    } // Album

    /** Represnets an artist object. */
    public class Artist {
        String name;
    } // Artist


    /** Represents topTags object. */
    public class Toptags {
        Tags[] tag;

        /** Represents tags object. */
        public class Tags {
            String name;
        } // Tags

    } // Top tags



} // FMResponse
