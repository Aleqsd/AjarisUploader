package com.orkis.ajarisuploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadHolder> {
    private ArrayList<Upload> uploadList;
    private Context mContext;
    private FragmentActivity activity;

    public UploadAdapter(Context context, ArrayList<Upload> uploadList) {
        this.uploadList = uploadList;
        this.mContext = context;
    }

    @Override
    public UploadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.item_dialog_history, parent, false);
        return new UploadHolder(view);
    }

    @Override
    public int getItemCount() {
        return this.uploadList == null ? 0 : this.uploadList.size();
    }

    public ArrayList<Upload> getData() {
        return this.uploadList;
    }

    @Override
    public void onBindViewHolder(@NonNull UploadHolder holder, final int position) {
        final Upload upload = uploadList.get(position);
        holder.setFileName(upload.getFile().split("/")[upload.getFile().split("/").length - 1]);
        DateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        holder.setDate(date.format(upload.getDate()));
        holder.setProfile(upload.getProfile().getName());
        holder.setComment(upload.getComment());
        holder.setImage(upload.getFile());
    }

    public class UploadHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView date;
        private TextView profile;
        private ImageView image;
        private TextView comment;

        public UploadHolder(View itemView) {
            super(itemView);

            fileName = itemView.findViewById(R.id.dialog_upload_filename);
            date = itemView.findViewById(R.id.dialog_upload_date);
            profile = itemView.findViewById(R.id.dialog_upload_profile);
            image = itemView.findViewById(R.id.dialog_upload_file);
            comment = itemView.findViewById(R.id.dialog_upload_comment);
        }

        public void setFileName(String f) {
            fileName.setText(f);
        }

        public void setDate(String d) {
            date.setText(d);
        }

        public void setProfile(String p) {
            profile.setText(p);
        }

        public void setComment(String p) {
            comment.setText(p);
        }

        public void setImage(String imagePath) {
            File testFile = new File(imagePath);
            if (!imagePath.equals("") && testFile.exists()) {
                String mimeType = URLConnection.guessContentTypeFromName(imagePath);
                if (mimeType != null && mimeType.startsWith("image"))
                {
                    Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), 64, 64);
                    image.setImageBitmap(thumbImage);
                }
                else
                {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                    Matrix matrix = new Matrix();
                    Bitmap bitmap = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
                    image.setImageBitmap(bitmap);
                }

            }
        }

    }
}