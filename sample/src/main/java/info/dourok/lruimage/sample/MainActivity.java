package info.dourok.lruimage.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import info.dourok.lruimage.LruImage;
import info.dourok.lruimage.LruImageView;
import info.dourok.lruimage.LruTaskBuilder;
import info.dourok.lruimage.image.UrlImage;
import info.dourok.lruimage.progress.CircleProgressDrawable;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 直接显示 url
        LruImageView content = (LruImageView) findViewById(R.id.content);
        content.setImageUrl("http://breadedcat.com/wp-content/gallery/in-bread-cats/20c.jpg");

        content.setShowProgress(true); // 显示 progress
        // 设置 progress drawble
        // LruImageView 通过 imageLevel 来实现加载动画
        // 默认加载动画是 CircleProgressDrawable
        content.setProgressDrawable(new CircleProgressDrawable(this));
        // 设置加载失败显示的 drawable
//        content.setFallbackResource(R.drawable.fail);
        // 这些属性都支持通过 xml 配置

        // 通过 UrlImage.Builder 来设置图片缩放大小
        content.setImage(new UrlImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(200, 200).create()); // LruImageView 可支持直接设置 LruImage

        LruImageView progress = (LruImageView) findViewById(R.id.progress_demo);
        final LruImageView avatar = (LruImageView) findViewById(R.id.avatar);

        progress.setTaskBuilder(new LruTaskBuilder(this).setCacheLevel(LruImage.CACHE_LEVEL_NO_CACHE));

        progress.setImageUrl("http://breadedcat.com/wp-content/gallery/in-bread-cats/breadedgary-de6ead1589f573b4d667461467a6c90480396974.jpg");
        final CircleProgressDrawable drawable = new CircleProgressDrawable(this);
        avatar.setImageDrawable(drawable);
        // 通过 LruImageTask 来管理图片请求和自定义回调
        // 通过 LruTaskBuilder 来创建 LruImageTask
        new LruTaskBuilder(this)
                .success(new LruTaskBuilder.SuccessCallback() {
                    @Override
                    public void call(Bitmap bitmap) {
                        Log.d("LruImage", bitmap.getWidth() + " " + bitmap.getHeight());
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
                Log.d("LruImage", "progress:" + position + "/" + total);
            }
        }).execute(new UrlImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(200, 200).create());

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
