package com.didim99.sat.ui.dirpicker;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.didim99.sat.R;
import java.util.List;

/**
 * Created by didim99 on 17.06.18.
 */
class DirListAdapter extends RecyclerView.Adapter<DirListAdapter.ViewHolder> {

  private OnItemClickListener listener;
  private LayoutInflater inflater;
  private Resources resources;
  private List<DirPickerActivity.DirEntry> files;

  DirListAdapter(Context context, OnItemClickListener listener,
                 List<DirPickerActivity.DirEntry> files) {
    this.inflater = LayoutInflater.from(context);
    this.resources = context.getResources();
    this.listener = listener;
    this.files = files;
  }

  @Override
  public DirListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_dir_picker, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(DirListAdapter.ViewHolder holder, int position) {
    DirPickerActivity.DirEntry file = files.get(position);

    //Enable top divider for first item
    if (position == 0)
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    else
      holder.topDivider.setVisibility(ImageView.INVISIBLE);

    holder.itemView.setOnClickListener(view ->
      listener.onItemClick(holder.getAdapterPosition(), file.isDir()));

    holder.name.setText(file.getName());
    if (file.isDir()) {
      holder.folderIcon.setVisibility(ImageView.VISIBLE);
      holder.name.setTextColor(resources.getColor(R.color.dirPicker_textActive));
    } else {
      holder.folderIcon.setVisibility(ImageView.INVISIBLE);
      holder.name.setTextColor(resources.getColor(R.color.dirPicker_textInactive));
    }
  }

  @Override
  public int getItemCount() {
    return files.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView name;
    final ImageView topDivider;
    final ImageView folderIcon;

    ViewHolder(View view) {
      super(view);
      topDivider = view.findViewById(R.id.ivTopDivider);
      folderIcon = view.findViewById(R.id.ivFolderIcon);
      name = view.findViewById(R.id.tvFileName);
    }
  }

  interface OnItemClickListener {
    void onItemClick(int selectedId, boolean isDir);
  }
}
