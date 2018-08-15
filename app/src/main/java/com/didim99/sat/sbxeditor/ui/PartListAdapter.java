package com.didim99.sat.sbxeditor.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.didim99.sat.R;
import com.didim99.sat.SAT;
import com.didim99.sat.sbxeditor.Storage;
import com.didim99.sat.sbxeditor.model.Part;
import com.didim99.sat.sbxeditor.model.PartComparator;
import com.didim99.sat.sbxeditor.model.SBML;
import com.didim99.sat.settings.Settings;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by didim99 on 09.05.18.
 */

class PartListAdapter extends RecyclerView.Adapter<PartListAdapter.ViewHolder> {

  private Context appContext;
  private LayoutInflater inflater;
  private ArrayList<Part> partList;
  private OnItemClickListener listener;
  private boolean screenLarge;

  PartListAdapter(Context context, SparseArray<Part> partStorage,
                  boolean screenLarge, OnItemClickListener listener) {
    this.appContext = context.getApplicationContext();
    this.inflater = LayoutInflater.from(context);
    this.screenLarge = screenLarge;
    this.listener = listener;
    this.partList = new ArrayList<>(partStorage.size());
    for (int i = 0; i < partStorage.size(); i++)
      partList.add(partStorage.valueAt(i));
    int sortMain = Settings.PartInfo.getSortMain();
    int sortSecond = Settings.PartInfo.getSortSecond();
    boolean reverse  = Settings.PartInfo.isSortReverse();
    if (sortMain != PartComparator.Method.PART_ID)
      Collections.sort(partList, PartComparator.create(sortMain, sortSecond, reverse));
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_part, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.itemView.setOnClickListener(v -> listener.onItemClick(getItemAt(position)));
    Part part = getItemAt(position);

    //Enable top divider for first item
    if (position == 0 || screenLarge && position == 1)
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    else
      holder.topDivider.setVisibility(ImageView.INVISIBLE);

    int colorId = 0;
    switch (part.getPartSize()) {
      case SBML.SIZE_SMALL:
        colorId = R.color.sizeSmall;
        break;
      case SBML.SIZE_MEDIUM:
        colorId = R.color.sizeMedium;
        break;
      case SBML.SIZE_LARGE:
        colorId = R.color.sizeLarge;
        break;
    }

    if (part.getSaveCargo() == SBML.SAVE_CARGO_REGULAR) {
      holder.tvCargo.setTextColor(
        appContext.getResources().getColor(R.color.colorFull));
    } else {
      holder.tvCargo.setTextColor(
        appContext.getResources().getColor(R.color.partInfo_textColorMain));
    }

    if (part.isHasNaviComp())
      holder.ivNav.setImageResource(R.drawable.ic_nav_yellow_24dp);
    else
      holder.ivNav.setImageResource(R.drawable.ic_nav_off_yellow_24dp);

    int iconId = part.getPartId();
    if (iconId == SBML.PART_ID_SOYUZ_SERVICE)
      iconId = SBML.PART_ID_LOK_SERVICE;
    Drawable icon = Drawable.createFromPath(String.format(SAT.ICONS_PATH, iconId));

    if (icon != null)
      holder.ivIcon.setImageDrawable(icon);
    else
      holder.ivIcon.setImageResource(colorId);
    holder.tvPartId.setText(String.valueOf(part.getPartId()));
    holder.tvName.setText(part.getPartName());
    holder.tvVersion.setText(Storage.getSAVerInfo().get(part.getMinVer()));
    holder.tvPowerGen.setText(appContext.getString(R.string.powerGen, part.getPowerGen()));
    holder.tvPowerUse.setText(appContext.getString(R.string.powerUse, part.getPowerUse()));
    holder.tvFuel.setText(appContext.getString(
      R.string.fuelLevels, part.getFuelMain(), part.getFuelThr()));
    holder.tvCargo.setText(String.valueOf(part.getCargoCount()));
  }

  @Override
  public int getItemCount() {
    return partList.size();
  }

  private Part getItemAt(int position) {
    return partList.get(position);
  }

  void updateSortMethod(int sortMain, int sortSecond, boolean reverse) {
    Collections.sort(partList, PartComparator.create(sortMain, sortSecond, reverse));
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider, ivIcon, ivNav;
    final TextView tvPartId, tvName, tvVersion;
    final TextView tvPowerGen, tvPowerUse;
    final TextView tvCargo, tvFuel;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      ivIcon = itemView.findViewById(R.id.ivIcon);
      ivNav = itemView.findViewById(R.id.ivNaviComp);
      tvPartId = itemView.findViewById(R.id.tvPartId);
      tvName = itemView.findViewById(R.id.tvName);
      tvVersion = itemView.findViewById(R.id.tvVersion);
      tvPowerGen = itemView.findViewById(R.id.tvPowerGen);
      tvPowerUse = itemView.findViewById(R.id.tvPowerUse);
      tvCargo = itemView.findViewById(R.id.tvCargo);
      tvFuel = itemView.findViewById(R.id.tvFuel);
    }
  }

  interface OnItemClickListener {
    void onItemClick(Part part);
  }
}