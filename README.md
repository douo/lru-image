### LruImage

又一个异步图片加载库。

### LruImageView

直接显示 url

        LruImageView content = (LruImageView) findViewById(R.id.content);
        //支持`http/https`,`file`,`content`
        content.setImageUrl("http://breadedcat.com/wp-content/gallery/in-bread-cats/20c.jpg");

LruImageView 支持显示加载进度

        // 显示 progress
        // 打开后 LruImageView 会使用默认的加载动画
        // 默认加载动画是 CircleProgressDrawable
        content.setShowProgress(true);
        // 也可以自定义动画
        content.setProgressDrawable(new CircleProgressDrawable(this));
        // 设置加载失败显示的 drawable
        content.setFallbackResource(R.drawable.fail);

这些属性都支持通过 xml 配置

        <info.dourok.lruimage.LruImageView
            custom:showProgress="true"
            android:id="@+id/progress_demo"
            custom:fallbackDrawable="@drawable/fail"
            custom:progressDrawable="@drawable/fail"
            custom:showProgress="true"
            android:layout_width="144dp"
            android:layout_height="144dp"/>

可通过 UrlImage.Builder 来设置图片缩放大小

        content.setImage(new UrlImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(200, 200).create()); // LruImageView 可支持直接设置 LruImage

### LruImageTask

LruImageTask 是实际加载图片的类，它通过在后台线程中调用 LruImage#loadBitmap 来实现异步加载。
通过 LruImageTask.OnCompleteListener 来实现回调。
同时提供了取消当前请求的方法

用 LruTaskBuilder 来创建 LruImageTask

       final LruImageView avatar = (LruImageView) findViewById(R.id.avatar);
       LruImageTask task = new LruTaskBuilder(this)
                .success(new LruTaskBuilder.SuccessCallback() {
                    @Override
                    public void call(Bitmap bitmap) {
                        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), Bitmap.createScaledBitmap(bitmap, size, size, false));
                        drawable.setAntiAlias(true);
                        drawable.setCornerRadius(drawable.getIntrinsicWidth() / 2);
                        avatar.setImageDrawable(drawable);
                    }
                })
                .execute(new UrlImage.Builder("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg")
                .setMaxSize(200, 200).create());


### UrlImage

UrlImage 是 LruImage 的子类，支持加载图片后对图片进行缩放，缩放功能搬运只 Volley。同时，它也是一个抽象类。

`WebImage`,`FileImage`,`ContentImage` 是其不同协议的实现。

通过 `UrlImage.Builder` 来创建不同协议的 LruImage，目前支持`http/https`,`file`,`content`。

