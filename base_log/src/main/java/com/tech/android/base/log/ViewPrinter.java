package com.tech.android.base.log;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: xuan
 * Created on 2021/3/18 17:21.
 * <p>
 * Describe:
 */
public class ViewPrinter implements LogPrinter {
    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private ViewPrinterProvider viewProvider;

    public ViewPrinter(Activity activity) {
        FrameLayout rootView = activity.findViewById(android.R.id.content);
        recyclerView = new RecyclerView(activity);
        adapter = new LogAdapter(LayoutInflater.from(recyclerView.getContext()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        viewProvider = new ViewPrinterProvider(rootView, recyclerView);
    }

    /**
     * 获取ViewProvider 通过ViewProvider可以控制logview试图的显示和隐藏
     *
     * @return
     */
    public ViewPrinterProvider getViewProvider() {
        return viewProvider;
    }

    @Override
    public void print(@NonNull LogConfig config, int level, String tag, String printString) {
        adapter.addItem(new LogMo(System.currentTimeMillis(), level, tag, printString));
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private static class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private LayoutInflater inflater;
        private List<LogMo> logs = new ArrayList<>();

        public LogAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        void addItem(LogMo logItem) {
            logs.add(logItem);
            notifyItemInserted(logs.size() - 1);
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = inflater.inflate(R.layout.log_item, viewGroup, false);
            return new LogViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogMo logMo = logs.get(position);
            int color = getHightlightColor(logMo.level);
            holder.tagView.setTextColor(color);
            holder.messageView.setTextColor(color);

            holder.tagView.setText(logMo.getFlattened());
            holder.messageView.setText(logMo.log);
        }

        private int getHightlightColor(int logLevel) {
            int hightlight;
            switch (logLevel) {
                case LogType.V:
                    hightlight = 0xFFBBBBBB;
                    break;
                case LogType.I:
                    hightlight = 0xFF99CC00;
                    break;
                case LogType.D:
                    hightlight = 0xFF33B5E5;
                    break;
                case LogType.E:
                    hightlight = 0xFFFF4444;
                    break;
                case LogType.A:
                    hightlight = 0xFFFF70FF;
                    break;
                default:
                    hightlight = 0xFFFFBB33;
                    break;
            }
            return hightlight;
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }
    }

    private static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tagView;
        TextView messageView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tagView = itemView.findViewById(R.id.tag);
            messageView = itemView.findViewById(R.id.message);
        }
    }
}
