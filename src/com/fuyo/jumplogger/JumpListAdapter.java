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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
        JumpRecord record = mDataset.get(position);
        BigDecimal bi = new BigDecimal(String.valueOf(mDataset.get(position).duration));
        String sec = bi.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);

        if (record.duration >= 1) {
            drawable.setColor(Color.parseColor("#263238"));
        } else {
            drawable.setColor(Color.parseColor("#4C646F"));
        }

        holder.mAvatar.setText(sec + "s");
        holder.mAvatar.setTextColor(Color.WHITE);
        holder.mAvatar.setBackgroundDrawable(drawable);

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN); //SimpleDataFormat is not thread-safe. Don't make it static.
        String dateStr = "";
        try {
            Date d = sdf.parse(mDataset.get(position).date);
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(d);
            Calendar now = Calendar.getInstance();

            if (cal1.get(Calendar.DAY_OF_YEAR ) == now.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.JAPAN);
                dateStr = sdf2.format(cal1.getTime());
            } else if (cal1.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd", Locale.JAPAN);
                dateStr = sdf2.format(cal1.getTime());
            } else {
                SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd", Locale.JAPAN);
                dateStr = sdf2.format(cal1.getTime());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.mView.setText((new BigDecimal(String.valueOf(mDataset.get(position).distance))).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + " m    " + dateStr);
        holder.mView2.setText(mDataset.get(position).location);

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

    public void setData(ArrayList<JumpRecord> records) {
        mDataset = records;
    }


}