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
import android.widget.ProgressBar;
import android.widget.TextView;
import com.didim99.sat.R;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.core.sbxeditor.Sandbox;
import com.didim99.sat.core.sbxeditor.Station;

/**
 * Created by didim99 on 09.05.18.
 */

class StationListAdapter extends MultiSelectAdapter<Station, RecyclerView.ViewHolder>
  implements View.OnCreateContextMenuListener {

  private static final class ViewType {
    static final int HEADER = 1;
    static final int STATION = 2;
  }

  private final LayoutInflater inflater;
  private final MenuInflater menuInflater;
  private Sandbox sandbox;

  StationListAdapter(Context context, MenuInflater menuInflater,
                     EventListener<Station> listener) {
    super(context, listener);
    this.inflater = LayoutInflater.from(context);
    this.menuInflater = menuInflater;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0)
      return ViewType.HEADER;
    else
      return ViewType.STATION;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == ViewType.HEADER) {
      return new HeaderViewHolder(
        inflater.inflate(R.layout.list_category_header, parent, false));
    } else {
      RecyclerView.ViewHolder holder = new StationViewHolder(
        inflater.inflate(R.layout.item_station, parent, false));
      holder.itemView.setOnCreateContextMenuListener(this);
      return holder;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    switch (getItemViewType(position)) {
      case ViewType.HEADER:
        bindHeaderViewHolder((HeaderViewHolder) holder, position);
        break;
      case ViewType.STATION:
        bindStationViewHolder((StationViewHolder) holder, position);
        break;
    }
  }

  @Override
  public int getItemCount() {
    if (sandbox == null) return 0;
    int stationCount = sandbox.getInfo().getStationCount();
    return stationCount == 0 ? 0 : stationCount + 1;
  }

  @Override
  protected int getSelectableItemCount() {
    return getItemCount() > 0 ? getItemCount() - 1 : 0;
  }

  @Override
  Station getItemAt(int position) {
    if (position == 0)
      return null;
    else
      return sandbox.getStation(position - 1);
  }

  @Override
  protected boolean isItemSelectable(int position) {
    return getItemViewType(position) != ViewType.HEADER;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view,
                                  ContextMenu.ContextMenuInfo menuInfo) {
    menuInflater.inflate(R.menu.ctx_menu_station, menu);
  }

  void refreshData(Sandbox sandbox) {
    this.sandbox = sandbox;
    super.refreshData();
  }

  void initSelection(Sandbox sandbox, State state) {
    refreshData(sandbox);
    super.initSelection(state);
  }

  private void bindHeaderViewHolder(HeaderViewHolder holder, int position) {
    if (position == 0) {
      holder.sectionName.setText(R.string.sectionHeader_stations);
      holder.itemCount.setText(String.valueOf(sandbox.getInfo().getStationCount()));
    }
  }

  private void bindStationViewHolder(StationViewHolder holder, int position) {
    Station.Info info = getItemAt(position).getInfo();

    int visibleId = 0;
    int modulesCount = info.getSize();
    int relativeSize = modulesCount * 500 / sandbox.getInfo().getLargestStationSize();

    switch (info.getVisibility()) {
      case Station.VISIBLE:
        visibleId = uiManager.resolveAttr(R.attr.ic_visibility);
        break;
      case Station.INVISIBLE:
        visibleId = uiManager.resolveAttr(R.attr.ic_visibility_off);
        break;
      case Station.VISIBILITY_UNKNOWN:
        visibleId = uiManager.resolveAttr(R.attr.ic_unknown);
        break;
    }

    switch (info.getObjType()) {
      case Station.Type.COLONY:
        holder.ivStatusType.setVisibility(View.VISIBLE);
        holder.ivStatusType.setImageResource(uiManager.resolveAttr(R.attr.ic_colony));
        break;
      case Station.Type.GROUP:
        holder.ivStatusType.setVisibility(View.VISIBLE);
        holder.ivStatusType.setImageResource(uiManager.resolveAttr(R.attr.ic_group));
        break;
      case Station.Type.TEXT:
        holder.ivStatusType.setVisibility(View.VISIBLE);
        holder.ivStatusType.setImageResource(uiManager.resolveAttr(R.attr.ic_text));
        break;
      default:
        holder.ivStatusType.setVisibility(View.GONE);
        break;
    }

    if (holder.tvDistance != null) {
      holder.tvName.setText(info.getStationName());
      holder.tvDistance.setText(info.getDistanceStr(inflater.getContext()));
      uiManager.setNavDistanceIcon(holder.ivNavDirection, info);
    } else {
      if (info.hasName()) {
        holder.tvName.setText(info.getStationName());
        holder.ivNavDirection.setVisibility(View.GONE);
      } else {
        holder.tvName.setText(info.getDistanceStr(inflater.getContext()));
        uiManager.setNavDistanceIcon(holder.ivNavDirection, info);
      }
    }

    if (info.hasMovement())
      uiManager.setMovementIcon(holder.ivStatusMovement, info);
    else
      uiManager.setRotationIcon(holder.ivStatusMovement, info);
    holder.ivStatusVisible.setImageResource(visibleId);
    holder.tvModCount.setText(Utils.intToString(modulesCount));
    holder.tvCenter.setText(info.getCenterPosStr(0));
    holder.pbModCount.setProgress(relativeSize);
  }

  static class HeaderViewHolder extends RecyclerView.ViewHolder {
    final TextView sectionName, itemCount;

    HeaderViewHolder(View view) {
      super(view);
      sectionName = view.findViewById(R.id.sectionName);
      itemCount = view.findViewById(R.id.itemCount);
    }
  }

  static class StationViewHolder extends RecyclerView.ViewHolder {
    final ProgressBar pbModCount;
    final ImageView
      ivStatusMovement, ivStatusVisible,
      ivStatusType, ivNavDirection;
    final TextView
      tvModCount, tvDistance,
      tvCenter, tvName;

    StationViewHolder(View view) {
      super(view);
      ivStatusMovement = view.findViewById(R.id.ivStatusMovement);
      ivStatusVisible = view.findViewById(R.id.ivStatusVisible);
      ivStatusType = view.findViewById(R.id.ivStatusType);
      ivNavDirection = view.findViewById(R.id.ivNavDirection);
      pbModCount = view.findViewById(R.id.pbModulesCount);
      tvModCount = view.findViewById(R.id.modCount);
      tvDistance = view.findViewById(R.id.distance);
      tvCenter = view.findViewById(R.id.center);
      tvName = view.findViewById(R.id.name);
    }
  }
}
