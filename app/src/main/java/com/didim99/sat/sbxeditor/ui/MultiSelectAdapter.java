package com.didim99.sat.sbxeditor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import java.util.ArrayList;

/**
 * Base RecyclerView adapter with multi-selection support
 * Created by didim99 on 11.06.18.
 */

abstract class MultiSelectAdapter<T, VH extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<VH> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SLAdapter";

  // Multi-selection event codes
  static final int EVENT_MS_START = 1;
  static final int EVENT_MS_UPDATE = 2;
  static final int EVENT_MS_ALL_SELECTED = 3;
  static final int EVENT_MS_END = 4;

  protected final Context context;
  protected final Resources resources;
  private final EventListener<T> listener;
  private final int[] selAttrs = { android.R.attr.selectableItemBackground };
  private final int bgSelected, bgUnselected;

  private boolean multiSelectAvailable, multiSelectEnabled;
  private ArrayList<Integer> selected;

  MultiSelectAdapter(Context context, EventListener<T> listener) {
    this.resources = context.getResources();
    this.listener = listener;
    this.context = context;
    selected = new ArrayList<>();
    bgSelected = resources.getColor(R.color.stationItem_selected);
    bgUnselected = resources.getColor(R.color.stationItem_unselected);
    multiSelectAvailable = getItemCount() > 1;
    multiSelectEnabled = false;
  }

  @Override
  @CallSuper
  public void onBindViewHolder(VH holder, int position) {
    if (multiSelectEnabled)
      setSelected(holder, true, selected.contains(position));
    else
      setSelected(holder, false, false);

    holder.itemView.setOnClickListener(v -> {
      if (multiSelectEnabled) {
        if (selected.contains(position)) {
          MyLog.d(LOG_TAG, "Item unselected: " + position);
          selected.remove((Integer) position);
          setSelected(holder, true, false);
          if (selected.isEmpty()) {
            MyLog.d(LOG_TAG, "Multi-select mode disabled");
            listener.onMultiSelectionEvent(EVENT_MS_END, 0);
            multiSelectEnabled = false;
          } else {
            listener.onMultiSelectionEvent(EVENT_MS_UPDATE, selected.size());
          }
        } else {
          MyLog.d(LOG_TAG, "Item selected: " + position);
          selected.add(position);
          setSelected(holder, true, true);
          int size = selected.size();
          boolean all = size == getItemCount();
          listener.onMultiSelectionEvent(
            all ? EVENT_MS_ALL_SELECTED : EVENT_MS_UPDATE, selected.size());
          if (all) MyLog.d(LOG_TAG, "All items selected");
        }
      } else {
        MyLog.d(LOG_TAG, "Open context menu (" + position +  ")");
        listener.onItemClick(v, getItemAt(position));
      }
    });

    holder.itemView.setOnLongClickListener(v -> {
      if (multiSelectAvailable && !multiSelectEnabled) {
        MyLog.d(LOG_TAG, "Multi-select mode enabled");
        listener.onMultiSelectionEvent(EVENT_MS_START, 1);
        multiSelectEnabled = true;
        selected.add(position);
        setSelected(holder, true, true);
        MyLog.d(LOG_TAG, "Item selected: " + position);
      }
      return true;
    });
  }

  final void initSelection(State state) {
    this.selected = state.selected;
    multiSelectAvailable = true;
    multiSelectEnabled = true;
    listener.onMultiSelectionEvent(EVENT_MS_START, 1);
    listener.onMultiSelectionEvent(selected.size() == getItemCount() ?
      EVENT_MS_ALL_SELECTED : EVENT_MS_UPDATE, selected.size());
  }

  final boolean inMultiSelectionMode() {
    return multiSelectEnabled;
  }

  final State getState() {
    return new State(selected);
  }

  final ArrayList<T> getSelected() {
    ArrayList<T> items = new ArrayList<>(selected.size());
    for (int position : selected)
      items.add(getItemAt(position));
    return items;
  }

  final void refreshData() {
    multiSelectAvailable = getItemCount() > 1;
    notifyDataSetChanged();
  }

  final void selectAll() {
    selected.clear();
    for (int i = 0; i < getItemCount(); i++)
      selected.add(i);
    listener.onMultiSelectionEvent(EVENT_MS_ALL_SELECTED, selected.size());
    MyLog.d(LOG_TAG, "All items selected");
    notifyDataSetChanged();
  }

  final void multiSelectionCancel() {
    MyLog.d(LOG_TAG, "Multi-select mode enabled");
    multiSelectEnabled = false;
    selected.clear();
    notifyDataSetChanged();
  }

  private void setSelected(VH holder, boolean selectable, boolean selected) {
    if (selectable) {
      holder.itemView.setBackgroundColor(selected ? bgSelected : bgUnselected);
    } else {
      TypedArray ta = context.obtainStyledAttributes(selAttrs);
      holder.itemView.setBackgroundDrawable(ta.getDrawable(0));
      ta.recycle();
    }
  }

  abstract T getItemAt(int position);

  class State {
    private ArrayList<Integer> selected;

    private State(ArrayList<Integer> selected) {
      this.selected = selected;
    }
  }

  interface EventListener<I> {
    void onItemClick(View view, I item);
    void onMultiSelectionEvent(int event, int count);
  }
}
