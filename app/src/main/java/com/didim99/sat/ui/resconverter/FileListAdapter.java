package com.didim99.sat.ui.resconverter;

import android.content.Context;
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

  private final LayoutInflater inflater;
  private ArrayList<String> fileList;

  FileListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_sound, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    //Enable top divider for first item
    if (position == 0)
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    else
      holder.topDivider.setVisibility(ImageView.INVISIBLE);

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
  }
}
