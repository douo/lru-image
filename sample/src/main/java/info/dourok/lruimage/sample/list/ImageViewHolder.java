package info.dourok.lruimage.sample.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import info.dourok.lruimage.progress.ProgressLruImageView;
import info.dourok.lruimage.sample.R;

/**
 * Created by John on 2015/11/16.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {
    ProgressLruImageView imageView;
    TextView textView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = (ProgressLruImageView) itemView.findViewById(R.id.image);
        textView = (TextView) itemView.findViewById(R.id.index);
    }

    public void populate(Image image, int position) {
        textView.setText(Integer.toString(position));
        imageView.setImageUrl(image.imageUrl);
        // imageView.setImageUrl(image.imageUrl, 100, 100, true, LruImage.CACHE_LEVEL_MEMORY_CACHE);
    }

}
