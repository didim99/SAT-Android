package com.didim99.sat.ui.resconverter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.didim99.sat.R;
import java.util.ArrayList;

/**
 * Created by didim99 on 23.08.18.
 */
class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

  private ArrayList<String> fileList;

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    return new ViewHolder(inflater.inflate(R.layout.item_sound, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.setupDivider(position);
    holder.fileName.setText(fileList.get(position));
  }

  @Override
  public int getItemCount() {
    return fileList == null ? 0 : fileList.size();
  }

  void refreshData(ArrayList<String> fileList) {
    this.fileList = fileList;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final TextView fileName;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      fileName = itemView.findViewById(R.id.tvName);
    }

    void setupDivider(int position) {
      topDivider.setVisibility(position == 0
        ? View.VISIBLE : View.INVISIBLE);
    }
  }
}
