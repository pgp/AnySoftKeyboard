package it.pgp.uhu.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

import java.util.List;

import it.pgp.uhu.utils.ClipboardMRU;
import it.pgp.uhu.utils.SimpleMRU;
import it.pgp.uhu.visualization.Animations;

public class ClipboardAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;
    public final List<String> objects;
    public final ClipboardMRU mru;

    public static class ClipboardViewHolder {
        private final TextView tv;
        private final ImageButton delBtn;

        ClipboardViewHolder(TextView tv, ImageButton delBtn) {
            this.tv = tv;
            this.delBtn = delBtn;
        }
    }

    private ClipboardAdapter(@NonNull Context context, ClipboardMRU mru) {
        super(context, android.R.layout.simple_list_item_1, mru.keys);
        this.mru = mru;
        this.objects = mru.keys;
        inflater = LayoutInflater.from(context);
        refresh();
    }

    public static ClipboardAdapter create(@NonNull Context context, ClipboardManager clipboardManager) {
        return new ClipboardAdapter(context, new ClipboardMRU(SimpleMRU.DEFAULT_SIZE, clipboardManager));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv;
        ImageButton delBtn;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.clipboard_item, null);

            tv = convertView.findViewById(R.id.clipboard_item_tv);
            delBtn = convertView.findViewById(R.id.clipboard_item_delBtn);

            convertView.setTag(new ClipboardViewHolder(tv,delBtn));
        }
        else {
            ClipboardViewHolder viewHolder = (ClipboardViewHolder) convertView.getTag();
            tv = viewHolder.tv;
            delBtn = viewHolder.delBtn;
        }

        String s = objects.get(position);

        tv.setText(s);
        tv.setOnClickListener(w -> {
            Toast.makeText(getContext(), "Ready to be pasted: " + s, Toast.LENGTH_SHORT).show();
            Animations.highlightListViewItem(position, (AbsListView)parent, () -> {
                ClipData clip = ClipData.newPlainText(s, s);
                mru.manager.setPrimaryClip(clip);
            });
        });

        delBtn.setOnClickListener(w -> {
            remove(position);
            Toast.makeText(parent.getContext(), "Item deleted: " + s, Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }

    public void refresh() {
        if(mru.addClipboardItemFromSystem()) notifyDataSetChanged();
    }

    public void remove(int position) {
        mru.removeItem(position);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mru.clear();
        super.clear();
    }
}
