package com.fuyo.jumplogger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Yohei FUJII on 11/23/2014.
 */
public class JumpListAdapter extends RecyclerView.Adapter<JumpListAdapter.ViewHolder> {
    private ArrayList<JumpRecord> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mAvatar;
        public TextView mView;
        public TextView mView2;
        public ImageButton mButton;
        public ViewHolder(View v) {
            super(v);
            mAvatar = (TextView)v.findViewById(R.id.text_avatar);
            mView = (TextView)v.findViewById(R.id.info_text1);
            mView2 = (TextView)v.findViewById(R.id.info_text2);
            mButton = (ImageButton)v.findViewById(R.id.imageButton);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public JumpListAdapter(ArrayList<JumpRecord> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JumpListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.jump_record, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BigDecimal bi = new BigDecimal(String.valueOf(mDataset.get(position).duration));
        String sec = bi.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor("#263238"));

        holder.mAvatar.setText(sec);
        holder.mAvatar.setTextColor(Color.WHITE);
        holder.mAvatar.setBackgroundDrawable(drawable);

        holder.mView.setText(mDataset.get(position).location);
        holder.mView2.setText("" + mDataset.get(position).date);

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(view.getContext(), view);
                menu.getMenuInflater().inflate(R.menu.popup, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return false;
                    }
                });
                menu.show();
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}