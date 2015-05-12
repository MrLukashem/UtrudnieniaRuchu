package com.example.mrlukashem.utrudnieniaruchu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.zip.Inflater;

/**
 * Created by mrlukashem on 10.05.15.
 */
public class CustomMarkerContentAdapter extends ArrayAdapter<String> {

    private Context context;
    private LayoutInflater inflater;

    public CustomMarkerContentAdapter(Context __context, int __resource) {
        super(__context, __resource);
        context = __context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int __pos, View __conv_view, ViewGroup __parent) {
        if(__conv_view == null) {
 //           __conv_view = inflater.inflate(R.layout.single_comment, null);
        }

        return __conv_view;
    }
}
