package com.ghstudios.android.features.wishlist.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.ghstudios.android.data.database.DataManager;

public class WishlistDeleteDialogFragment extends DialogFragment {
    public static final String EXTRA_DELETE =
            "com.daviancorp.android.ui.general.wishlist_delete";
    private static final String ARG_WISHLIST_ID = "WISHLIST_ID";
    private static final String ARG_WISHLIST_NAME = "WISHLIST_NAME";

    public static WishlistDeleteDialogFragment newInstance(long id, String name) {
        Bundle args = new Bundle();
        args.putLong(ARG_WISHLIST_ID, id);
        args.putString(ARG_WISHLIST_NAME, name);
        WishlistDeleteDialogFragment f = new WishlistDeleteDialogFragment();
        f.setArguments(args);
        return f;
    }
    
    private void sendResult(int resultCode, boolean delete) {
        if (getTargetFragment() == null)
            return;
        
        Intent i = new Intent();
        i.putExtra(EXTRA_DELETE, delete);
        
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {    
        final String name = getArguments().getString(ARG_WISHLIST_NAME);
        return new AlertDialog.Builder(getActivity())
            .setTitle("Delete '" + name + "' wishlist?")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                long wishlistId = getArguments().getLong(ARG_WISHLIST_ID);
                DataManager.get().queryDeleteWishlist(wishlistId);

                Toast.makeText(getActivity(), "Deleted '" + name + "'", Toast.LENGTH_SHORT).show();
                sendResult(Activity.RESULT_OK, true);
            })
            .create();
    }
}
