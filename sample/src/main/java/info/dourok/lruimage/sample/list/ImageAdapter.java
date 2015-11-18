package info.dourok.lruimage.sample.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import info.dourok.lruimage.sample.R;

/**
 * Created by John on 2015/11/16.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
    Context mContext;
    List<Image> images;
    private final static String TAG = "ImageAdapter";

    public ImageAdapter(Context context, List<Image> images) {
        mContext = context;
        this.images = images;
    }


    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ImageViewHolder(inflater.inflate(R.layout.item_image, parent, false));

    }


    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {
        Log.d(TAG, position + ":" + images.get(position).imageUrl);
        viewHolder.populate(images.get(position), position);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}
