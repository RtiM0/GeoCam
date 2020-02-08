package com.sih.geocam;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.halilibo.bvpkotlin.BetterVideoPlayer;
import com.halilibo.bvpkotlin.VideoProgressCallback;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

public class PlaybackActivity extends AppCompatActivity implements VideoProgressCallback {

    private MapView osmmap;
    private JSONArray data;
    private int temptime = 0;
    private int index = 0;
    private TextView debugo;
    private IMapController iMapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        BetterVideoPlayer player = findViewById(R.id.vidplayer);
        osmmap = findViewById(R.id.mapper);
        Storage storage = new Storage(getApplicationContext());
        osmmap.setTileSource(TileSourceFactory.MAPNIK);
        osmmap.setMultiTouchControls(true);
        debugo = findViewById(R.id.debugo);
        player.setSource(Uri.fromFile(storage.getFile(storage.getExternalStorageDirectory() + File.separator + "GeoCam" + File.separator + "6.mp4")));
        player.enableControls();
        player.enableSwipeGestures();
        player.setProgressCallback(this);
        try {
            data = new JSONArray(storage.readTextFile(storage.getExternalStorageDirectory() + File.separator + "GeoCam" + File.separator + "6.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        iMapController = osmmap.getController();
        Log.d("onCreate: ", data.toString());
    }

    @Override
    public void onProgressUpdate(int i, int i1) {
        int playerpos = i / 1000;
        try {
            JSONObject temp = data.getJSONObject(index);
            if ((playerpos + "").equals(temp.getString("time"))) {
                debugo.setText((temp.getString("latitude") + "," + temp.getString("longitude")));
                index++;
                GeoPoint startPoint = new GeoPoint(temp.getDouble("latitude"), temp.getDouble("longitude"));
                iMapController.setCenter(startPoint);
                iMapController.setZoom(15.0);
                Marker startMarker = new Marker(osmmap);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                osmmap.getOverlays().add(startMarker);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
