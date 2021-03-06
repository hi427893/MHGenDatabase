package com.ghstudios.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ghstudios.android.AssetRegistry;
import com.ghstudios.android.data.classes.ElementStatus;
import com.ghstudios.android.data.classes.Weapon;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.components.WeaponListEntry;

/**
 * Adapter used to create weapons that show element information.
 * Created by Mark on 3/3/2015.
 */
public abstract class WeaponExpandableListElementAdapter extends WeaponExpandableListGeneralAdapter {

    public WeaponExpandableListElementAdapter(Context context, View.OnLongClickListener listener) {
        super(context, listener);
    }

    protected static class WeaponElementViewHolder extends WeaponViewHolder {

        // Element
        public TextView elementView;
        public TextView elementView2;
        public TextView awakenView;
        
        public ImageView elementIconView;
        public ImageView elementIconView2;

        public WeaponElementViewHolder(View weaponView) {
            super(weaponView);

            //
            // ELEMENT VIEWS
            //
            elementView = weaponView.findViewById(R.id.element_text);
            elementView2 = weaponView.findViewById(R.id.element_text2);
            awakenView = weaponView.findViewById(R.id.awaken_text);
            elementIconView = weaponView.findViewById(R.id.element_image);
            elementIconView2 = weaponView.findViewById(R.id.element_image2);
        }

        @Override
        public void bindView(Context context, WeaponListEntry entry) {
            super.bindView(context, entry);

            Weapon weapon = entry.weapon;

            // Set the element to view
            ElementStatus element = weapon.getElementEnum();
            ElementStatus awakenedElement = weapon.getAwakenElementEnum();
            ElementStatus element2 = weapon.getElement2Enum();

            long element_attack = weapon.getElementAttack();
            long element_2_attack = weapon.getElement2Attack();
            long awaken_attack = weapon.getAwakenAttack();

            String awakenText = "";


            elementView.setText("");
            elementView2.setText("");
            elementIconView.setVisibility(View.INVISIBLE);
            elementIconView2.setVisibility(View.INVISIBLE);

            if (element != ElementStatus.NONE || awakenedElement != ElementStatus.NONE) {
                if (awakenedElement != ElementStatus.NONE) {
                    element = awakenedElement;
                    element_attack = awaken_attack;
                    awakenText = "(";
                    elementView.setText(Long.toString(element_attack) + ")");
                } else {
                    elementView.setText(Long.toString(element_attack));
                }

                elementIconView.setTag(weapon.getId());
                elementIconView.setVisibility(View.VISIBLE);

                int elementIconId = AssetRegistry.getElementRegistry().get(element, R.color.transparent);
                Drawable icon = ContextCompat.getDrawable(context, elementIconId);
                elementIconView.setImageDrawable(icon);
            }

            if (element2 != ElementStatus.NONE) {
                elementIconView2.setTag(weapon.getId());

                int elementIconId = AssetRegistry.getElementRegistry().get(element2, R.color.transparent);
                Drawable icon = ContextCompat.getDrawable(context, elementIconId);
                elementIconView2.setImageDrawable(icon);

                elementIconView2.setVisibility(View.VISIBLE);

                elementView2.setText("" + element_2_attack);
            }

            awakenView.setText(awakenText);

        }
    }
}
