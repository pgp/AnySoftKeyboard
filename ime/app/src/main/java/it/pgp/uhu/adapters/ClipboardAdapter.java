package it.pgp.uhu.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

import java.util.LinkedList;

import it.pgp.uhu.utils.ClipboardMRU;
import it.pgp.uhu.utils.SimpleMRU;
import it.pgp.uhu.visualization.Animations;

public class ClipboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LayoutInflater inflater;
    public final Context context;
    public final LinkedList<String> objects;
    public final ClipboardMRU mru;

    public static final int HEADERVIEW =0;
    public static final int LISTVIEW =1;

    private ClipboardAdapter(@NonNull Context context, ClipboardMRU mru, LinkedList<String> objects) {
        this.mru = mru;
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
        refresh();
    }

    public static ClipboardAdapter create(@NonNull Context context, ClipboardManager clipboardManager) {
        ClipboardMRU mru = new ClipboardMRU(SimpleMRU.DEFAULT_SIZE, clipboardManager);
        return new ClipboardAdapter(context, mru, mru.keys);
    }

    public void refresh() {
        if(mru.addClipboardItemFromSystem()) notifyDataSetChanged();
    }

    public void remove(int position) {
        mru.removeItem(position);
        notifyDataSetChanged();
    }

    public void clear() {
        mru.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;
        if(getItemViewType(i) == HEADERVIEW) {
            v = inflater.inflate(R.layout.clipboard_header_view,viewGroup,false);
            return new HeaderViewHolder(v);
        }
        v = LayoutInflater.from(context).inflate(R.layout.clipboard_item,viewGroup,false);
        return new ContentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder.getItemViewType() == HEADERVIEW ){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder)viewHolder;
        }
        else {
            ContentHolder myHolder = (ContentHolder) viewHolder;
            String s = objects.get(i - 1);
            myHolder.name.setText(s);
            myHolder.name.setOnClickListener(v -> {
                Toast.makeText(context, "Ready to be pasted: " + s, Toast.LENGTH_SHORT).show();
                Animations.highlightListViewItem(myHolder.name, () -> {
                    ClipData clip = ClipData.newPlainText(s, s);
                    mru.manager.setPrimaryClip(clip);
                });
            });
            myHolder.delBtn.setOnClickListener(w -> {
                remove(i-1);
                Toast.makeText(context, "Item deleted: " + s, Toast.LENGTH_SHORT).show();
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return HEADERVIEW;
        }
        return LISTVIEW;
    }

    @Override
    public int getItemCount() {
        return objects.size()+1;
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton delBtn;
        public ContentHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.clipboard_item_tv);
            delBtn = itemView.findViewById(R.id.clipboard_item_delBtn);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headertextview;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
//            headertextview = itemView.findViewById(R.id.header);
        }
    }
}
