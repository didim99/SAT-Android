package com.didim99.sat.ui.sbxeditor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.didim99.sat.R;
import com.didim99.sat.core.sbxeditor.Sandbox;
import com.didim99.sat.core.sbxeditor.wrapper.NCMarker;
import com.didim99.sat.settings.Settings;
import java.util.ArrayList;

/**
 * Created by didim99 on 09.05.18.
 */

class NavListAdapter extends MultiSelectAdapter<NCMarker, NavListAdapter.ViewHolder>
  implements View.OnCreateContextMenuListener {

  private final LayoutInflater inflater;
  private final MenuInflater menuInflater;
  private ArrayList<NCMarker> naviComp;
  private ArrayList<String> planetNames;
  private boolean screenLarge;

  NavListAdapter(Context context, MenuInflater menuInflater, boolean screenLarge,
                 Sandbox sandbox, ArrayList<String> planetNames,
                 EventListener<NCMarker> listener) {
    super(context, listener);
    this.inflater = LayoutInflater.from(context);
    this.menuInflater = menuInflater;
    this.naviComp = sandbox.getNaviComp();
    this.screenLarge = screenLarge;
    this.planetNames = planetNames;
    super.refreshData();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ViewHolder holder = new ViewHolder(
      inflater.inflate(R.layout.item_navicomp, parent, false));
    holder.itemView.setOnCreateContextMenuListener(this);
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    NCMarker marker = getItemAt(position);
    super.onBindViewHolder(holder, position);

    //Enable top divider for first item
    if (position == 0 || position == 1 && screenLarge)
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    else
      holder.topDivider.setVisibility(ImageView.INVISIBLE);

    int colorId;
    if (Settings.isDbLoaded()) {
      if (planetNames.contains(marker.getLabel())) {
        colorId = uiManager.resolveAttr(R.attr.clr_textActive);
        holder.tvLabel.setTextColor(resources.getColor(colorId));
        holder.tvCenter.setTextColor(resources.getColor(colorId));
      } else {
        colorId = uiManager.resolveAttr(R.attr.clr_textInactive);
        holder.tvLabel.setTextColor(resources.getColor(colorId));
        holder.tvCenter.setTextColor(resources.getColor(colorId));
      }
    }

    holder.tvLabel.setText(marker.getLabel());
    holder.tvCenter.setText(marker.getCenterStr(0));
  }

  @Override
  public int getItemCount() {
    return naviComp == null ? 0 : naviComp.size();
  }

  @Override
  protected int getSelectableItemCount() {
    return getItemCount();
  }

  @Override
  NCMarker getItemAt(int position) {
    return naviComp.get(position);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view,
                                  ContextMenu.ContextMenuInfo menuInfo) {
    menuInflater.inflate(R.menu.ctx_menu_navicomp, menu);
  }

  void refreshData(Sandbox sandbox) {
    this.naviComp = sandbox.getNaviComp();
    super.refreshData();
  }

  void initSelection(Sandbox sandbox, State state) {
    this.naviComp = sandbox.getNaviComp();
    super.initSelection(state);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final TextView tvLabel, tvCenter;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      tvLabel = itemView.findViewById(R.id.tvLabel);
      tvCenter = itemView.findViewById(R.id.tvCenter);
    }
  }
}
