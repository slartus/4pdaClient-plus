//package org.softeg.slartus.forpdaplus.devdb.adapters;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.squareup.picasso.Picasso;
//
//import org.softeg.slartus.forpdaplus.R;
//import org.softeg.slartus.forpdaplus.controls.imageview.SquareImageView;
//import org.softeg.slartus.forpdaplus.devdb.adapters.base.BaseRecyclerViewHolder;
//
//import java.util.List;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
///**
// * Created by isanechek on 17.01.16.
// */
//public class SpecGalleryAdapter extends RecyclerView.Adapter<SpecGalleryAdapter.ViewHolder> {
////    private static final int LAYOUT = R.layout.image_viewer_item;
//
//    private Context context;
//    private List<String> urls;
//
//    public SpecGalleryAdapter(Context context, List<String> urls) {
//        this.context = context;
//        this.urls = urls;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
////        View view = LayoutInflater.from(parent.getContext()).inflate(LAYOUT, null);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        String link = urls.get(position);
//        Picasso.with(context)
//                .load(link)
//                .error(R.drawable.no_image)
//                .into(holder.imageView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return (null != urls ? urls.size() : 0);
//    }
//
//    public static class ViewHolder extends BaseRecyclerViewHolder {
//
////        @Bind(R.id.spec_gallery_item)
////        SquareImageView imageView;
//
//        public ViewHolder(View view) {
//            super(view);
////            imageView = ButterKnife.findById(view, R.id.spec_gallery_item);
//        }
//
//    }
//}
