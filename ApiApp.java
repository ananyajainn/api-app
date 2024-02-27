package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.*;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();


    /** Google {@code Gson} object for parsing JSON strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private Stage stage;
    private Scene scene;
    private VBox root;
    // searchPane
    private HBox searchPane;
    private Label song;
    private TextField songQuery;
    private Label artist;
    private TextField artistQuery;
    private Button artify;
    private String artistTerm;
    private String songTerm;
    // infoPane
    private HBox infoPane;
    private Label info;
    // songPane
    private VBox songPane;
    private ImageView albumFrame;
    private Image defImage;
    private Label songInfo;

    // artPane
    private TilePane artPane;
    private ImageView[] frames;
    private Tooltip[] title;

    // loadingPane
    private HBox loadingPane;
    private ProgressBar progressBar;
    private Label sources;

    public String infoResponseBody;
    public HttpResponse<String> infoResponse;
    public FMResponse fmResponse;

    public ArrayList<String> localTags;
    public ArrayList<String> objects;
    public ArrayList<String> images;
    public ArrayList<String> titles;
    public Random rand;
    private Alert alert;
    public EventHandler<ActionEvent> handler;
    public Thread t;

    // CONSTANTS
    private static final String DEF_IMAGE = "file:resources/fishy.JPG";
    private static final String NOCOVER = "file:resources/nocover.png";
    private static final String SOURCE = " Last.fm & Metropolitan Museum of Art Collection";
    private static final String INSTR = "Enter a music artist and one of their songs. " +
        "Click the button to visualize \nthat song as art. Hover over the art to see the title.";
    private static final String FMLINK = "http://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "8920a0b9dfe2ec06b2b5b04289869703";
    private static final String METQ = "https://collectionapi.metmuseum.org/"
        + "public/collection/v1/search";
    private static final String METOBJ = "https://collectionapi.metmuseum.org/public/"
        + "collection/v1/objects/";

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();

        // search Pane
        searchPane = new HBox();
        song = new Label ( " Song: " );
        songQuery = new TextField("Super Rich Kids");
        artist = new Label ( " Artist: " );
        artistQuery = new TextField("Frank Ocean");
        artify = new Button( "Artify" );

        // instructions
        infoPane = new HBox();
        info = new Label(INSTR);

        //songPane
        songPane = new VBox();
        defImage = new Image ( DEF_IMAGE );
        albumFrame = new ImageView( defImage );
        songInfo = new Label( "nothing here yet..." );

        // artPane
        artPane = new TilePane( 30, 30 );
        frames = new ImageView[3];
        title = new Tooltip[3];
        for ( int i = 0; i < frames.length; i++ ) {
            frames[i] = new ImageView ( defImage );
            title[i] = new Tooltip ( "nothing here yet...");
            Tooltip.install( frames[i], title[i] );
        } // for

        // loadingPane
        loadingPane = new HBox();
        progressBar = new ProgressBar(0);
        sources = new Label( SOURCE );

        rand = new Random();
        alert = new Alert ( AlertType.ERROR );

    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        root.getChildren().addAll( searchPane, infoPane, songPane, artPane, loadingPane );

        searchPane.setAlignment(Pos.BASELINE_CENTER);
        searchPane.setPadding(new Insets( 5,5,5,5) );
        searchPane.getChildren().addAll( artist, artistQuery, song, songQuery, artify );

        infoPane.setPadding(new Insets( 0, 0, 5, 5));
        infoPane.getChildren().addAll( info );

        songPane.setAlignment(Pos.BASELINE_CENTER);
        songPane.getChildren().addAll( albumFrame, songInfo );

        artPane.setAlignment(Pos.BASELINE_CENTER);
        artPane.setPrefColumns(3);
        artPane.setPadding(new Insets(10,10,10,10));
        artPane.getChildren().addAll ( frames[0], frames[1], frames[2] );

        loadingPane.setPadding(new Insets( 5,5,5,5) );
        loadingPane.getChildren().addAll ( progressBar, sources );

        //button
        Runnable task = () -> artifyTask();

        artify.setOnAction( e -> runNow ( task ) );

    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // demonstrate how to load local asset using "file:resources/"
//        Image bannerImage = new Image("file:resources/fishy.JPG");
//        ImageView banner = new ImageView(bannerImage);
//        banner.setPreserveRatio(true);
//        banner.setFitWidth(640);

        // some labels to display information
//        Label notice = new Label("Modify the starter code to suit your needs.");

        // setup scene
//        root.getChildren().addAll(banner, notice);
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /** Task for artify button. */
    public void artifyTask() {
        System.out.println( "artify button clicked!" ); // remove
        Platform.runLater( () -> {
            progressBar.setProgress (0);
            artify.setDisable( true );
        } );

        buildInfo();
        Platform.runLater( () -> progressBar.setProgress (.25) );

        storeTags();
        Platform.runLater( () -> progressBar.setProgress (.5) );

        buildObject();

        Platform.runLater( () -> {
            progressBar.setProgress(.75);
            songInfo.setText( fmResponse.track.name + " by " + fmResponse.track.artist.name );
            albumFrame.setImage ( defImage );
            for ( int i = 0; i < frames.length; i++ ) {
                frames[i].setImage( defImage );
            } // for
            setImages();
            progressBar.setProgress(1);
            artify.setDisable( false );
        });

    } // artifyTask

    // build for last fm and get those vlaues
    /** Build http for song info. */
    public void buildInfo() {
        artistTerm = artistQuery.getText();
        songTerm = songQuery.getText();
        String artist = URLEncoder.encode ( artistTerm, StandardCharsets.UTF_8 );
        String track = URLEncoder.encode ( songTerm, StandardCharsets.UTF_8 );
        String key = URLEncoder.encode ( API_KEY, StandardCharsets.UTF_8 );
        String format = URLEncoder.encode( "json", StandardCharsets.UTF_8 );

        String method = URLEncoder.encode ( "track.getInfo", StandardCharsets.UTF_8 );
        String query = String.format("?method=%s&api_key=%s&artist=%s&track=%s&" +
            "format=%s", method, key, artist, track, format);

        HttpRequest request = HttpRequest.newBuilder()
            .uri( URI.create ( FMLINK + query ))
            .build();
        System.out.println( FMLINK + query );

        try {
            infoResponse = HTTP_CLIENT.send( request, BodyHandlers.ofString() );
            if ( infoResponse.statusCode() != 200 ) {
                throw new IOException ( infoResponse.toString() );
            } // if
            infoResponseBody = infoResponse.body();
            System.out.println( infoResponse.statusCode() ); // remove
            fmResponse = GSON
            .fromJson( infoResponseBody, FMResponse.class );
            printFMResponse ( fmResponse );
            if ( fmResponse.track == null ) {
                throw new NullPointerException ( "Can't find song. Try something else!" );
            } // if

        } catch ( IOException | InterruptedException | NullPointerException ioe ) {
            System.out.println( "uh oh! 278");
            alertError ( ioe );
        } // try

    } // buildInfo

    /** Store tags. */
    public void storeTags() {
        localTags = new ArrayList<String>();

        for ( int i = 0; i < fmResponse.track.toptags.tag.length; i++ ) { // copy all tags
            localTags.add( fmResponse.track.toptags.tag[i].name );
        } // for

        buildArtist();

    } // storeTags

        /** Build http for artist info. */
    public void buildArtist() {
        System.out.println( "buildArtist() called" );
        artistTerm = artistQuery.getText();
        String artist = URLEncoder.encode ( artistTerm, StandardCharsets.UTF_8 );
        String key = URLEncoder.encode ( API_KEY, StandardCharsets.UTF_8 );
        String format = URLEncoder.encode( "json", StandardCharsets.UTF_8 );

        String method = URLEncoder.encode ( "artist.gettoptags", StandardCharsets.UTF_8 );
        String query = String.format("?method=%s&artist=%s&api_key=%s&" +
            "format=%s", method, artist, key, format);

        HttpRequest request = HttpRequest.newBuilder()
            .uri( URI.create ( FMLINK + query ))
            .build();
        System.out.println( FMLINK + query );

        try {
            HttpResponse<String> aResponse = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if ( aResponse.statusCode() != 200 ) {
                throw new IOException ( aResponse.toString() );
            } // if
            String aResponseBody = aResponse.body();
            TagsResponse tagResponse = GSON
                .fromJson( aResponseBody, TagsResponse.class );
            printTagsResponse ( tagResponse );

            // add tags to list
            for ( int i = 0; i < tagResponse.toptags.tag.length; i++ ) {
                if ( !localTags.contains( tagResponse.toptags.tag[i].name ) ) {
                    localTags.add( tagResponse.toptags.tag[i].name );
                } // if
            } // for

            // check if enough tags now
            if ( localTags.size() < 3 ) {
                throw new NullPointerException
                ( "Song is too obscure! Not able to generate enough art." +
                    " Try a different song!");
            } // if

        } catch ( IOException | NullPointerException | InterruptedException ioe ) {
            System.out.println( "uh oh! buildartist");
            alertError( ioe );
        } // try

    } // buildInfo

    // build request for met api
    /** Request for query that retreives an array of object IDs. */
    public void buildObject() {
        objects = new ArrayList<String>();
        images = new ArrayList<String>();
        titles = new ArrayList<String>();
        for ( int i = 0; i < localTags.size() && objects.size() < 3; i++ ) {
            String term = URLEncoder.encode ( localTags.get(i), StandardCharsets.UTF_8 );
            String query = String.format("?q=%s", term );
            HttpRequest request = HttpRequest.newBuilder()
                .uri ( URI.create ( METQ + query ) )
                .build();
            System.out.println( METQ + query );
            try {
                HttpResponse<String> response = HTTP_CLIENT
                    .send( request, BodyHandlers.ofString() );
                if ( response.statusCode() != 200  && i == localTags.size() - 1) {
                    throw new IOException( response.toString() );
                } // if
                String responseBody = response.body();
                MetQResponse metResponse = GSON
                    .fromJson ( responseBody, MetQResponse.class );
                // check if there are any objects
                if ( metResponse.objectIDs != null ) {
                    boolean added = false;
                    // iterate through ids until find one with all params
                    for ( int j = 0; j < metResponse.objectIDs.length && !added; j++ ) {
                        int num = rand.nextInt ( metResponse.objectIDs.length );
                        String id = "" + metResponse.objectIDs[ num ];
                        // check that that object isn't already there
                        if ( !objects.contains ( id ) ) {
                        // check if has title and img
                            if ( buildInfo2( id, i ) ) {
                            // add to list of objects if it does and end loop for this tag
                                objects.add( "" + metResponse.objectIDs[j]);
                                System.out.println ( "" + metResponse.objectIDs[j] ); // remove
                                added = true;
                            } // if
                        } // if
                    } // for
                } // if
                System.out.println ( objects.size() );
            } catch ( IOException | InterruptedException e ) {
                System.out.println ( "uh oh!" );
                //alert error
            } // try
        } // for
        try {
            // check size
            if ( objects.size() < 3 ) {
                System.out.println( "uh oh! 395" );
                throw new NullPointerException
                ( "Song is too obscure! Not able to generate enough art." +
                    " Try a different song!");
            } // if
        } catch ( NullPointerException  e ) {
            alertError ( e );
        } // try
        System.out.println( objects.toString() ); // remove
        System.out.println( images.toString() ); // remove
        System.out.println( titles.toString() ); // remove
    } // buildObject


    /** Get info for singular id.
     * @param objectID object id to get info on
     * @param i index of tag list
     * @return whether image and title added
     */
    public boolean buildInfo2( String objectID, int i ) {
        boolean result = false;
        String term = URLEncoder.encode ( objectID , StandardCharsets.UTF_8 );
        HttpRequest request = HttpRequest.newBuilder()
            .uri ( URI.create ( METOBJ + term ) )
            .build();
        System.out.println( METOBJ + term );

        try {
            HttpResponse<String> response = HTTP_CLIENT
                .send( request, BodyHandlers.ofString() );
            if ( response.statusCode() != 200 ) {
                return result;
            } // if
            String responseBody = response.body();
            MetObject metObject = GSON
                .fromJson ( responseBody, MetObject.class );

            if ( !metObject.primaryImage.equals("") && !metObject.title.equals("") ) {
                images.add ( metObject.primaryImage );
                titles.add ( metObject.title );
                result = true;
            } // if

        } catch ( IOException | InterruptedException e ) {
            System.out.println( "uh oh!");
            alertError ( e );
        } // try

        return result;
    } // buildImage


    /** Set images into frames. */
    public void setImages() {
        if ( fmResponse.track.album != null && fmResponse.track.album.image[2].text != null ) {
            String image = fmResponse.track.album.image[2].text;
            albumFrame.setImage( new Image ( image ));
            albumFrame.setPreserveRatio( true );
            albumFrame.setFitHeight( 120 );
        } else {
            albumFrame.setImage( new Image ( NOCOVER ));
            albumFrame.setPreserveRatio( true );
            albumFrame.setFitHeight( 120 );
        } // if

        for ( int i = 0; i < images.size(); i++ ) {
            Image img = new Image ( images.get(i) );
            frames[i].setImage( img );
            frames[i].setFitWidth ( 120 );
            frames[i].setFitHeight ( 120 );
            title[i].setText( titles.get(i) );
        } // for
    } // setImages

    /** Prints FM Json.
     * @param fmResponse object from API request
     */
    private static void printFMResponse ( FMResponse fmResponse ) {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(fmResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("url = %s\n", fmResponse.track.url );
        System.out.printf("name = %s\n", fmResponse.track.name );
        System.out.printf("artist = %s\n", fmResponse.track.artist.name );
        for ( int i = 0; i < fmResponse.track.toptags.tag.length; i++ ) {
            System.out.printf("tag = %s\n", fmResponse.track.toptags.tag[i].name );
        } // for
    } // parseItunesResponse

    /** Prints tags Json.
     * @param tagsResponse object from API request
     */
    private static void printTagsResponse ( TagsResponse tagsResponse) {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(tagsResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        for ( int i = 0; i < tagsResponse.toptags.tag.length; i++ ) {
            System.out.printf("tag = %s\n", tagsResponse.toptags.tag[i].name );
        } // for
    } // parseItunesResponse

    /** This method executes an error dialog.
     * @param e exception
     */
    public void alertError ( Exception e ) {
        Platform.runLater( () -> {
            alert.setContentText( "Error: " + e );
            alert.show();
            artify.setDisable ( false );
            progressBar.setProgress(0);
        } );
        t.stop();
    } // alertError

    /**
       * Creates and immediately starts a new daemon thread that executes
       * {@code target.run()}. This method, which may be called from any thread,
       * will return immediately its the caller.
       * @param target the object whose {@code run} method is invoked when this
       *               thread is started
       */
    public void runNow(Runnable target) {
        t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } // runNow

} // ApiApp
