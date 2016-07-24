package landvive.footprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;

public class MapActivity extends Activity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.accessToken));
        setContentView(R.layout.activity_map);

        // Create a mapView
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl("mapbox://styles/songwongeun/ciqj3kwat0016bfnjl7skwc42");

        Button btn_plus = (Button) findViewById(R.id.btn_plus);

        btn_plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MapActivity.this, v);
                getMenuInflater().inflate(R.menu.minimenu, popup.getMenu());
                popup.setOnMenuItemClickListener(listener);
                popup.show();
            }
        });
    }

    PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {

                case R.id.myalbum:
                    Intent intent1 = new Intent(MapActivity.this, MyAlbumActivity.class);
                    startActivity(intent1);
                    finish();
                    break;

                case R.id.makealbum:
                    Intent intent2 = new Intent(MapActivity.this, MakeAlbumActivity.class);
                    startActivity(intent2);
                    finish();
                    break;

                case R.id.logout:
                    Toast.makeText(MapActivity.this, "아직 안됨", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
