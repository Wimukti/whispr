package com.example.vlc;

import static com.example.vlc.utils.Constants.VIEW_TYPE_RECEIVED;
import static com.example.vlc.utils.Constants.VIEW_TYPE_SENT;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlc.utils.Message;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.sent_message_tile, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            View view = inflater.inflate(R.layout.received_message_tile, parent, false);
            return new ReceivedMessageViewHolder(view);
        }

        // Default case (shouldn't happen)
        View view = inflater.inflate(R.layout.sent_message_tile, parent, false);
        return new SentMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.isSent()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // ViewHolder for sent messages
    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView sentTextViewContent;
        private final TextView receivedTextViewTimestamp;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentTextViewContent = itemView.findViewById(R.id.sentMessageTextView);
            receivedTextViewTimestamp = itemView.findViewById(R.id.sentTimestampTextView);
        }

        public void bind(Message message) {
            sentTextViewContent.setText(message.getContent());
            receivedTextViewTimestamp.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    // ViewHolder for received messages
    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView receivedTextViewContent;
        private final TextView receivedTextViewTimestamp;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedTextViewContent = itemView.findViewById(R.id.receivedMessageTextView);
            receivedTextViewTimestamp = itemView.findViewById(R.id.receivedTimestampTextView);
        }

        public void bind(Message message) {
            receivedTextViewContent.setText(message.getContent());
            receivedTextViewTimestamp.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    private String formatTimestamp(long timestamp) {
        // Customize the timestamp formatting according to your requirements
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


}
