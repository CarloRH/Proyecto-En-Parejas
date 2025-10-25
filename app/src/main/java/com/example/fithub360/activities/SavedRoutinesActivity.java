package com.example.fithub360.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SavedRoutinesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private RoutinesAdapter adapter;
    private List<JSONObject> items = new ArrayList<>();

    private static final String PREFS_NAME = "fithub_saved_routines";
    private static final String KEY_SAVED = "saved_routines";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_routines);
        recyclerView = findViewById(R.id.savedRecyclerView);
        emptyView = findViewById(R.id.emptyTextView);

        adapter = new RoutinesAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas para cuadrícula
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        loadItems();
    }

    private void loadItems() {
        items.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String raw = prefs.getString(KEY_SAVED, null);
        if (raw == null) {
            updateEmpty();
            return;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                items.add(o);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error cargando historial", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        updateEmpty();
    }

    private void updateEmpty() {
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView subtitle;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.savedTitle);
                subtitle = v.findViewById(R.id.savedSubtitle);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_routine, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject obj = items.get(position);
            String title = obj.optString("title", "-");
            long ts = obj.optLong("timestamp", 0);
            String when = ts == 0 ? "" : DateFormat.getDateTimeInstance().format(new Date(ts));
            holder.title.setText(title);
            holder.subtitle.setText(when);

            holder.itemView.setOnClickListener(v -> {
                // mostrar contenido en diálogo
                String content = obj.optString("content", "");
                new AlertDialog.Builder(SavedRoutinesActivity.this)
                        .setTitle(title)
                        .setMessage(content)
                        .setPositiveButton("Cerrar", null)
                        .show();
            });

            holder.itemView.setOnLongClickListener(v -> {
                // eliminar
                new AlertDialog.Builder(SavedRoutinesActivity.this)
                        .setTitle("Eliminar")
                        .setMessage("¿Eliminar esta rutina?")
                        .setPositiveButton("Eliminar", (d, which) -> {
                            items.remove(position);
                            saveListToPrefs();
                            notifyDataSetChanged();
                            updateEmpty();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void saveListToPrefs() {
            JSONArray arr = new JSONArray();
            for (JSONObject o : items) arr.put(o);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SAVED, arr.toString()).apply();
            Toast.makeText(SavedRoutinesActivity.this, "Historial actualizado", Toast.LENGTH_SHORT).show();
        }
    }
}
