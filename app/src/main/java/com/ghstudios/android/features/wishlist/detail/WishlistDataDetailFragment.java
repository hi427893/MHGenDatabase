package com.ghstudios.android.features.wishlist.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ghstudios.android.AssetLoader;
import com.ghstudios.android.data.classes.WishlistData;
import com.ghstudios.android.data.database.DataManager;
import com.ghstudios.android.data.cursors.WishlistDataCursor;
import com.ghstudios.android.features.wishlist.list.WishlistListActivity;
import com.ghstudios.android.features.wishlist.list.WishlistListFragment;
import com.ghstudios.android.loader.WishlistDataListCursorLoader;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.ClickListeners.ItemClickListener;

public class WishlistDataDetailFragment extends ListFragment implements
        LoaderCallbacks<Cursor> {

    public static final String EXTRA_DETAIL_REFRESH =
            "com.daviancorp.android.ui.general.wishlist_detail_refresh";
    
    private static final String ARG_ID = "ID";

    private static final String DIALOG_WISHLIST_DATA_EDIT = "wishlist_data_edit";
    private static final String DIALOG_WISHLIST_DATA_DELETE = "wishlist_data_delete";
    private static final int REQUEST_REFRESH = 0;
    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_DELETE = 2;
    private static final int REQUEST_WISHLIST_DATA_DELETE = 10;

    private static final String TAG = "WishlistDataFragment";

    private boolean started, fromOtherTab;
    
    private ListView mListView;
    
    public static WishlistDataDetailFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        WishlistDataDetailFragment f = new WishlistDataDetailFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        
        // Initialize the loader to load the list of runs
        getLoaderManager().initLoader(R.id.wishlist_data_detail_fragment, getArguments(), this);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wishlist_item_list, container, false);

        mListView = v.findViewById(android.R.id.list);
        
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FragmentManager fm = getFragmentManager();

        switch (item.getItemId()) {
            case R.id.wishlist_rename: // Launch Rename Wishlist dialog
                WishlistRenameDialogFragment dialog = WishlistRenameDialogFragment.newInstance(
                        getArguments().getLong(ARG_ID, -1),
                        (String) getActivity().getTitle());
                dialog.setTargetFragment(this, WishlistListFragment.REQUEST_RENAME);
                dialog.show(fm, WishlistListFragment.DIALOG_WISHLIST_RENAME);
                return true;
            case R.id.wishlist_delete:// Launch Delete Wishlist dialog
                WishlistDeleteDialogFragment dialogDelete = WishlistDeleteDialogFragment.newInstance(
                        getArguments().getLong(ARG_ID, -1),
                        (String) getActivity().getTitle());
                dialogDelete.setTargetFragment(this, WishlistListFragment.REQUEST_DELETE);
                dialogDelete.show(fm, WishlistListFragment.DIALOG_WISHLIST_DELETE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_wishlist_data, menu);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Return nothing if result is failed
        if(resultCode != Activity.RESULT_OK) return;

        switch (requestCode){
            case WishlistListFragment.REQUEST_RENAME: // After wishlist is renamed
                if (data.getBooleanExtra(WishlistRenameDialogFragment.EXTRA_RENAME, false)) {
                    // Cast parent activity in order to call refresh title method
                    ((WishlistDetailPagerActivity) getActivity()).refreshTitle();
                    updateUI();
                }
                return;
            case WishlistListFragment.REQUEST_DELETE: // After wishlist is deleted
                if (data.getBooleanExtra(WishlistDeleteDialogFragment.EXTRA_DELETE, false)) {
                    // Exit current activity
                    Intent intent = new Intent(getActivity(), WishlistListActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                return;
            case REQUEST_WISHLIST_DATA_DELETE:
                updateUI();
                break;
        }

    }
    
    public void updateUI() {
        if (started) {
            // Refresh wishlist data fragment
            getLoaderManager().getLoader( R.id.wishlist_data_detail_fragment ).forceLoad();
            WishlistDataListCursorAdapter adapter = (WishlistDataListCursorAdapter) getListAdapter();
            adapter.notifyDataSetChanged();

            if (!fromOtherTab) {
                sendResult(Activity.RESULT_OK, true);
            }
            else {
                fromOtherTab = false;
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // If we are becoming visible, then...
        if (isVisibleToUser) {
            // Update wishlist with items that are 'satisfied'
            long wishlistId = getArguments().getLong(ARG_ID);
            DataManager.get().helperQueryUpdateWishlistSatisfied(wishlistId);
            updateUI();
        }
    }


    private void sendResult(int resultCode, boolean refresh) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_DETAIL_REFRESH, refresh);
        
        getTargetFragment()
            .onActivityResult(getTargetRequestCode(), resultCode, i);
    }
    

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // You only ever load the runs, so assume this is the case
        long mId = -1;
        if (args != null) {
            mId = args.getLong(ARG_ID);
        }
        return new WishlistDataListCursorLoader(getActivity(), mId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Create an adapter to point at this cursor
        WishlistDataListCursorAdapter adapter = new WishlistDataListCursorAdapter(
                getActivity(), (WishlistDataCursor) cursor);
        setListAdapter(adapter);

        started = true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Stop using the cursor (via the adapter)
        setListAdapter(null);
    }



    private class WishlistDataListCursorAdapter extends CursorAdapter {

        private WishlistDataCursor mWishlistDataCursor;

        public WishlistDataListCursorAdapter(Context context, WishlistDataCursor cursor) {
            super(context, cursor, 0);
            mWishlistDataCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Use a layout inflater to get a row view
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_wishlist_item_listitem,
                    parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Get the skill for the current row
            final WishlistData data = mWishlistDataCursor.getWishlistData();

            // Set up the text view
            LinearLayout root = view.findViewById(R.id.listitem);
            ImageView itemImageView = view.findViewById(R.id.item_image);
            TextView itemTextView = view.findViewById(R.id.item);
            TextView amtTextView = view.findViewById(R.id.amt);
            TextView extraTextView = view.findViewById(R.id.extra);
            
            final long id = data.getItem().getId();
            final String nameText = data.getItem().getName();
            String amtText = "" + data.getQuantity();
            
            String extraText = data.getPath();
            int satisfied = data.getSatisfied();

            // Indicate a piece's requirements are met
            if (satisfied == 1) {
                itemTextView.setTextColor(ContextCompat.getColor(context, R.color.light_accent_color));
            } else {
                itemTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            }

            // Assign textviews
            itemTextView.setText(nameText);
            amtTextView.setText(amtText);
            extraTextView.setText(extraText);

            AssetLoader.setIcon(itemImageView,data.getItem());

            root.setOnClickListener(new ItemClickListener(context, data.getItem()));

            root.setOnLongClickListener(view1 -> {
                WishlistDataDeleteDialogFragment dialogDelete = WishlistDataDeleteDialogFragment.newInstance(data.getId(), nameText);
                dialogDelete.setTargetFragment(WishlistDataDetailFragment.this, REQUEST_WISHLIST_DATA_DELETE);
                dialogDelete.show(getFragmentManager(), DIALOG_WISHLIST_DATA_DELETE);
                return true;
            });

        }
    }

    // Define interface WishlistDetailPagerActivity must implement to refresh it's title
    public interface RefreshActivityTitle{
        void refreshTitle();
    }
}
