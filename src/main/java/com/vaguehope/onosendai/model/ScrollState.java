package com.vaguehope.onosendai.model;

import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.vaguehope.onosendai.util.LogWrapper;
import com.vaguehope.onosendai.widget.ScrollIndicator;

public class ScrollState {

	public enum ScrollDirection {
		UNKNOWN(0),
		UP(1),
		DOWN(2);

		private final int value;

		private ScrollDirection (final int value) {
			this.value = value;
		}

		public int getValue () {
			return this.value;
		}

		public static ScrollDirection parseValue (final int value) {
			switch (value) {
				case 1:
					return UP;
				case 2:
					return DOWN;
				default:
					return UNKNOWN;
			}
		}
	}

	private static final String KEY_ITEM_ID = "list_view_item_id";
	private static final String KEY_TOP = "list_view_top";
	private static final String KEY_ITEM_TIME = "list_view_item_time";
	private static final String KEY_UNREAD_TIME = "list_view_unread_time";
	private static final String KEY_SCROLL_DIRECTION = "list_view_scroll_direction";

	private static final LogWrapper LOG = new LogWrapper("ST");

	private final long itemId;
	private final int top;
	private final long itemTime;
	private final long unreadTime;
	private final ScrollDirection scrollDirection;

	public ScrollState (final long itemId, final int top, final long itemTime, final long unreadTime, final ScrollDirection scrollDirection) {
		this.itemId = itemId;
		this.top = top;
		this.itemTime = itemTime;
		this.unreadTime = unreadTime;
		this.scrollDirection = scrollDirection;
	}

	@Override
	public String toString () {
		return new StringBuilder()
				.append("ScrollState{").append(this.itemId)
				.append(',').append(this.top)
				.append(',').append(this.itemTime)
				.append(',').append(this.unreadTime)
				.append(',').append(this.scrollDirection)
				.append('}')
				.toString();
	}

	public long getItemId () {
		return this.itemId;
	}

	public int getTop () {
		return this.top;
	}

	public long getItemTime () {
		return this.itemTime;
	}

	public long getUnreadTime () {
		return this.unreadTime;
	}

	public ScrollDirection getScrollDirection () {
		if (this.scrollDirection == null) return ScrollDirection.UNKNOWN;
		return this.scrollDirection;
	}

	public void applyTo (final ListView lv, final ScrollIndicator scrollIndicator) {
		scrollIndicator.setUnreadTime(this.unreadTime);
		applyToListView(lv);
	}

	public void applyToIfNewer (final ScrollIndicator scrollIndicator) {
		scrollIndicator.setUnreadTimeIfNewer(this.unreadTime);
	}

	public void applyToIfNewer (final ListView lv, final ScrollDirection lastScrollDirection) {
		if (lastScrollDirection == ScrollDirection.DOWN) return;
		final long listViewTime = ((TweetListCursorAdapter) lv.getAdapter()).getItemTime(lv.getFirstVisiblePosition());
		if (this.itemTime > listViewTime) applyToListView(lv);
	}

	private void applyToListView (final ListView lv) {
		// NOTE if this seems unreliable try wrapping setSelection*() calls in lv.post(...).
		final ListAdapter adapter = lv.getAdapter();

		if (this.itemId >= 0L) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItemId(i) == this.itemId) {
					lv.setSelectionFromTop(i, this.top);
					return;
				}
			}
		}

		// Also search by time before giving up.
		if (this.itemTime > 0L && adapter instanceof TweetListCursorAdapter) {
			final TweetListCursorAdapter tlca = (TweetListCursorAdapter) adapter;
			for (int i = 0; i < tlca.getCount(); i++) {
				final long itime = tlca.getItemTime(i);
				if (itime > 0L && itime <= this.itemTime) {
					lv.setSelectionFromTop(i, 0);
					return;
				}
			}
			LOG.w("Failed to restore scroll state %s to list of %s items.", this, tlca.getCount());
		}
		else {
			LOG.w("Failed to restore scroll state %s.", this);
		}

		lv.setSelection(lv.getCount() - 1);
	}

	public void addToBundle (final Bundle bundle) {
		if (bundle == null) return;
		bundle.putLong(KEY_ITEM_ID, this.itemId);
		bundle.putInt(KEY_TOP, this.top);
		bundle.putLong(KEY_ITEM_TIME, this.itemTime);
		bundle.putLong(KEY_UNREAD_TIME, this.unreadTime);
		if (this.scrollDirection != null) bundle.putInt(KEY_SCROLL_DIRECTION, this.scrollDirection.getValue());
	}

	public static ScrollState from (final ListView lv, final ScrollIndicator scrollIndicator, final ScrollDirection scrollDirection) {
		final int index = lv.getFirstVisiblePosition();
		final View v = lv.getChildAt(0);
		final int top = (v == null) ? 0 : v.getTop();

		final long itemId = lv.getAdapter().getItemId(index);
		if (itemId <= 0) return null;

		long time = ((TweetListCursorAdapter) lv.getAdapter()).getItemTime(index);
		if (time < 0L) time = 0L;

		return new ScrollState(itemId, top, time, scrollIndicator.getUnreadTime(), scrollDirection);
	}

	public static ScrollState fromBundle (final Bundle bundle) {
		if (bundle == null) return null;
		if (bundle.containsKey(KEY_ITEM_ID) && bundle.containsKey(KEY_TOP)) {
			final long itemId = bundle.getLong(KEY_ITEM_ID);
			final int top = bundle.getInt(KEY_TOP);
			final long itemTime = bundle.getLong(KEY_ITEM_TIME);
			final long unreadTime = bundle.getLong(KEY_UNREAD_TIME);
			return new ScrollState(itemId, top, itemTime, unreadTime, ScrollDirection.parseValue(bundle.getInt(KEY_SCROLL_DIRECTION, -1)));
		}
		return null;
	}

}
