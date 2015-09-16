package info.dourok.lruimage.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import info.dourok.lruimage.LruImage;
import info.dourok.lruimage.LruImageException;
import info.dourok.lruimage.LruImageTask;
import info.dourok.lruimage.LruImageView;
import info.dourok.lruimage.WebImage;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LruImageView content = (LruImageView) findViewById(R.id.content);
        final LruImageView avatar = (LruImageView) findViewById(R.id.avatar);

        content.setImageUrl("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg", 200, 200, true, LruImage.CACHE_LEVEL_DISK_CACHE);

        LruImageTask task = new LruImageTask(this,
                new WebImage("http://breadedcat.com/wp-content/gallery/cat-breading/in-bread-cat-11.jpg"),
                new LruImageTask.OnCompleteListener() {
                    @Override
                    public void onSuccess(LruImage image, Bitmap bitmap) {
                        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), Bitmap.createScaledBitmap(bitmap, size, size, false));
                        drawable.setAntiAlias(true);
                        drawable.setCornerRadius(drawable.getIntrinsicWidth() / 2);
                        avatar.setImageDrawable(drawable);
                        bitmap.recycle();
                    }

                    @Override
                    public void onFailure(LruImage image, LruImageException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void cancel() {
                        System.out.println("cancel");
                    }
                }).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
