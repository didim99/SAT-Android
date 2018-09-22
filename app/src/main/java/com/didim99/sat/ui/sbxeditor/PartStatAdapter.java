package com.didim99.sat.ui.sbxeditor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.didim99.sat.R;
import com.didim99.sat.core.sbxeditor.Station;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by didim99 on 21.09.18.
 */
class PartStatAdapter extends RecyclerView.Adapter<PartStatAdapter.ViewHolder> {

  private Context appContext;
  private LayoutInflater inflater;
  private ArrayList<Station.PartStatEntry> partStat;

  PartStatAdapter(Context context, ArrayList<Station.PartStatEntry> partStat) {
    this.appContext = context.getApplicationContext();
    this.inflater = LayoutInflater.from(context);
    this.partStat = partStat;

    Collections.sort(partStat, (e1, e2) -> {
      int equals = Integer.compare(e2.getCount(), e1.getCount());
      return equals == 0 ? e1.getPart().getPartName().compareTo(
        e2.getPart().getPartName()) : equals;
    });
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_part_stat, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Station.PartStatEntry entry = partStat.get(position);
    holder.tvCount.setText(String.valueOf(entry.getCount()));
    holder.tvName.setText(entry.getPart().getPartName());

    int colorId = 0;
    switch (entry.getPart().getPartSize()) {
      case SBML.Size.SMALL:
        colorId = R.color.sizeSmallBg;
        break;
      case SBML.Size.MEDIUM:
        colorId = R.color.sizeMediumBg;
        break;
      case SBML.Size.LARGE:
        colorId = R.color.sizeLargeBg;
        break;
    }

    holder.itemView.setBackgroundResource(colorId);
  }

  @Override
  public int getItemCount() {
    return partStat.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView tvName, tvCount;

    ViewHolder(View itemView) {
      super(itemView);
      tvName = itemView.findViewById(R.id.tvName);
      tvCount = itemView.findViewById(R.id.tvCount);
    }
  }
}
