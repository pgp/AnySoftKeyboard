package it.pgp.uhu.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

import it.pgp.uhu.utils.ClipboardMRU;
import it.pgp.uhu.utils.SimpleMRU;
import it.pgp.uhu.visualization.Animations;

public class ClipboardAdapter extends ArrayAdapter<String> {
    public final List<String> objects;
    public final ClipboardMRU mru;

    private ClipboardAdapter(@NonNull Context context, ClipboardMRU mru) {
        super(context, android.R.layout.simple_list_item_1, mru.keys);
        this.mru = mru;
        this.objects = mru.keys;
        refresh();
    }

    public static ClipboardAdapter create(@NonNull Context context, ClipboardManager clipboardManager) {
        return new ClipboardAdapter(context, new ClipboardMRU(SimpleMRU.DEFAULT_SIZE, clipboardManager));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        v.setOnClickListener(w -> {
            String s = getItem(position);
            Toast.makeText(getContext(), "Ready to be pasted: " + s, Toast.LENGTH_SHORT).show();
            Animations.highlightListViewItem(position, (AbsListView)parent, () -> {
                ClipData clip = ClipData.newPlainText(s, s);
                mru.manager.setPrimaryClip(clip);
            });
        });
        return v;
    }

    public void refresh() {
        if(mru.addClipboardItemFromSystem()) notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mru.clear();
        super.clear();
    }
}
