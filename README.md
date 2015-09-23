
Ex.1

```
final LruImageView avatar = (LruImageView) findViewById(R.id.avatar);
content.setImageUrl("http://breadedcat.com/wp-content/uploads/2012/02/breaded-cat-tutorial-1.jpg");
```

Ex.2

```
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

                    }

                    @Override
                    public void cancel() {
                        System.out.println("cancel");
                    }
                }).execute();
```