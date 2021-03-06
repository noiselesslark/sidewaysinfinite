
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wrapper.spotify.models.Track;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.application.Platform;

public class MainScene extends Application implements NewMessageListener {
	static MediaView mediaView;
	static MediaController controller;
	static List<Track> playlistTracks; 
	private CajonOSCInPort inPort = new CajonOSCInPort(this);

	@Override
	public void start(Stage primaryStage) throws IOException {
		controller = MediaController.getInstance();
		playlistTracks = TrackLister.getTracksFromPlaylist("dealerpriest", "7jRNmBtd06qUw4sOstXyfT");

		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("MainScene.fxml")
				);
		loader.setController(MediaController.getInstance());
		Parent root = loader.load();

		Scene scene = new Scene(root, 800, 564);
		primaryStage.setTitle("BeatRamble");
		primaryStage.setScene(scene);
		primaryStage.show();


		mediaView = new MediaView(null);
		((AnchorPane)scene.getRoot()).getChildren().add(mediaView);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
		//OSCLoop.loopetyLoop();

		//IO.downloadMP3("http://d318706lgtcm8e.cloudfront.net/mp3-preview/f454c8224828e21fa146af84916fd22cb89cedc6", fileName);
	}

	private static void playNextTrack() {

		MediaPlayer nextTrack = controller.getNextTrack();

		if(nextTrack!= null) {
			nextTrack.setOnEndOfMedia(

					new Runnable() {

						public void run() {
							playNextTrack();
						}
					});
		}
		if(mediaView.getMediaPlayer() != null) mediaView.getMediaPlayer().stop();
		mediaView.setMediaPlayer(nextTrack);
		nextTrack.setAutoPlay(true);
	}

	public void onNewMessage(final ArrayList<MessageObject> l) {
		Platform.runLater(new Runnable() {

			public void run() {
				String tmp = l.get(0).getMessageContent().getArguments().toString();


				int tempo = Integer.parseInt(tmp.substring(1, tmp.indexOf(".")));

				MediaController.getInstance().getDetectedTempo().setText(tempo + "");

				Track track = TrackLister.getClosestTempoTrack(playlistTracks, tempo);
				MediaController.getInstance().getSongName().setText(track.getName());
				MediaController.getInstance().getArtistName().setText(track.getArtists().get(0).getName());

				MediaController.getInstance().queueNewTrack(track);

				playNextTrack();
			}
		});

	}
}
