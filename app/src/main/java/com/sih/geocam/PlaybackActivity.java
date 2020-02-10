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
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

public class PlaybackActivity extends AppCompatActivity implements VideoProgressCallback {

    private MapView osmmap;
    private JSONArray data;
    private TextView debugo;
    private BetterVideoPlayer player;
    private IMapController iMapController;
    private Double latitude[], longitude[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        String filepath = getIntent().getStringExtra("filepath");
        String jsonpath = getIntent().getStringExtra("jsonpath");
        player = findViewById(R.id.vidplayer);
        osmmap = findViewById(R.id.mapper);
        Storage storage = new Storage(getApplicationContext());
        osmmap.setTileSource(TileSourceFactory.MAPNIK);
        osmmap.setMultiTouchControls(true);
        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());
        debugo = findViewById(R.id.debugo);
        player.setSource(Uri.fromFile(storage.getFile(filepath)));
        player.enableControls();
        player.enableSwipeGestures();
        player.setProgressCallback(this);
        try {
            data = new JSONArray(storage.readTextFile(jsonpath));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        iMapController = osmmap.getController();
        iMapController.setZoom(20);
        try {
            createDataArray();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("onCreate: ", data.toString());
    }

    private void createDataArray() throws JSONException {
        int time = player.getDuration() / 1000;
        int in = 0;
        latitude = new Double[time];
        longitude = new Double[time];
        for (int i = 0; i <= time; i++) {
            JSONObject temp = data.getJSONObject(in);
            if (temp.getInt("time") == i) {
                latitude[i] = temp.getDouble("latitude");
                longitude[i] = temp.getDouble("longitude");
                in++;
                try {
                    while (data.getJSONObject(in).getInt("time") == temp.getInt("time")) {
                        in++;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setOnMap(GeoPoint geoPoint, boolean animate) {
        if (animate) {
            iMapController.animateTo(geoPoint);
        } else {
            iMapController.setCenter(geoPoint);
        }
        Marker startMarker = new Marker(osmmap);
        startMarker.setPosition(geoPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        osmmap.getOverlays().clear();
        osmmap.getOverlays().add(startMarker);
        osmmap.postInvalidate();
    }
    @Override
    public void onProgressUpdate(int i, int i1) {
        int playerpos = i / 1000;
        if (latitude[playerpos] != null) {
            debugo.setText((latitude[playerpos] + "," + longitude[playerpos]));
            GeoPoint startPoint = new GeoPoint(latitude[playerpos], longitude[playerpos]);
            setOnMap(startPoint, true);
        }
//        try {
//            JSONObject temp = data.getJSONObject(index);
//            if ((playerpos + "").equals(temp.getString("time"))) {
//                debugo.setText((temp.getString("latitude") + "," + temp.getString("longitude")));
//                index++;
//                GeoPoint startPoint = new GeoPoint(temp.getDouble("latitude"), temp.getDouble("longitude"));
//                if (index == 1) {
//                    iMapController.setCenter(startPoint);
//                } else {
//                    iMapController.animateTo(startPoint);
//                }
//                Marker startMarker = new Marker(osmmap);
//                startMarker.setPosition(startPoint);
//                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                osmmap.getOverlays().clear();
//                osmmap.getOverlays().add(startMarker);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }
}
