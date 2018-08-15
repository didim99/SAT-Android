package com.didim99.sat.sbxeditor.ui;

import android.content.Context;
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
import com.didim99.sat.Utils;
import com.didim99.sat.sbxeditor.Sandbox;
import com.didim99.sat.sbxeditor.Station;

/**
 * Created by didim99 on 09.05.18.
 */

class StationListAdapter extends MultiSelectAdapter<Station, StationListAdapter.ViewHolder>
  implements View.OnCreateContextMenuListener {

  private final UIManager uiManager;
  private final LayoutInflater inflater;
  private final MenuInflater menuInflater;
  private Sandbox sandbox;

  StationListAdapter(Context context, MenuInflater menuInflater,
                     EventListener<Station> listener) {
    super(context, listener);
    this.uiManager = UIManager.getInstance();
    this.inflater = LayoutInflater.from(context);
    this.menuInflater = menuInflater;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_station, parent, false);
    ViewHolder holder = new ViewHolder(view);
    holder.itemView.setOnCreateContextMenuListener(this);
    return holder;
  }

  @Override
  public void onBindViewHolder(StationListAdapter.ViewHolder holder, int position) {
    Station.Info info = getItemAt(position).getInfo();
    super.onBindViewHolder(holder, position);

    int visibleId = 0;
    int modulesCount = info.getSize();
    int relativeSize = modulesCount * 500 / sandbox.getInfo().getLargestStationSize();

    switch (info.getVisibility()) {
      case Station.VISIBLE:
        visibleId = R.drawable.ic_visibility_24dp;
        break;
      case Station.INVISIBLE:
        visibleId = R.drawable.ic_visibility_off_24dp;
        break;
      case Station.VISIBILITY_UNKNOWN:
        visibleId = R.drawable.ic_unknown_24dp;
        break;
    }

    switch (info.getObjType()) {
      case Station.Type.GROUP:
        holder.ivStatusType.setVisibility(View.VISIBLE);
        holder.ivStatusType.setImageResource(R.drawable.ic_group_24dp);
        break;
      case Station.Type.TEXT:
        holder.ivStatusType.setVisibility(View.VISIBLE);
        holder.ivStatusType.setImageResource(R.drawable.ic_text_24dp);
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

  @Override
  public int getItemCount() {
    return sandbox == null ? 0 : sandbox.getInfo().getStationCount();
  }

  @Override
  Station getItemAt(int position) {
    return sandbox.getStation(position);
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
    this.sandbox = sandbox;
    super.initSelection(state);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ProgressBar pbModCount;
    final ImageView
      ivStatusMovement, ivStatusVisible,
      ivStatusType, ivNavDirection;
    final TextView
      tvModCount, tvDistance,
      tvCenter, tvName;

    ViewHolder(View view) {
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
