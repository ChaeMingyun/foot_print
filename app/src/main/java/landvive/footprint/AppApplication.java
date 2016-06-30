package landvive.footprint;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by chaemingyun on 2016. 6. 30..
 */
public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
