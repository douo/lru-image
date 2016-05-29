package info.dourok.lruimage.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import info.dourok.lruimage.LruImage;
import info.dourok.lruimage.LruImageView;
import info.dourok.lruimage.LruTaskBuilder;
import info.dourok.lruimage.WebImage;
import info.dourok.lruimage.progress.CircleProgressDrawable;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LruImageView content = (LruImageView) findViewById(R.id.content);
        LruImageView progress = (LruImageView) findViewById(R.id.progress_demo);
        final LruImageView avatar = (LruImageView) findViewById(R.id.avatar);
        content.setImage(new WebImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(200,200).create());
        progress.setTaskBuilder(new LruTaskBuilder(this).setCacheLevel(LruImage.CACHE_LEVEL_MEMORY_CACHE));
        progress.setImageUrl("http://breadedcat.com/wp-content/gallery/in-bread-cats/breadedgary-de6ead1589f573b4d667461467a6c90480396974.jpg");

        final CircleProgressDrawable drawable = new CircleProgressDrawable(this);
        avatar.setImageDrawable(drawable);
        new LruTaskBuilder(this).
                success(new LruTaskBuilder.SuccessCallback() {
                    @Override
                    public void call(Bitmap bitmap) {
                        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), Bitmap.createScaledBitmap(bitmap, size, size, false));
                        drawable.setAntiAlias(true);
                        drawable.setCornerRadius(drawable.getIntrinsicWidth() / 2);
                        avatar.setImageDrawable(drawable);
//                        bitmap.recycle();
                    }
                }).progress(new LruImage.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(LruImage image, int total, int position) {
                avatar.setImageLevel((int) (1.f * position / total * 10000));
//                Log.d("LruImage", "progress:" + position + "/" + total);
            }
        }).execute(new WebImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(4096, 4096).create());

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
