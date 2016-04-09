package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.utility.Hash;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Chad on 20-Mar-16.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    //===== INSTANCE VARIABLES
    private Context context;
    private Hash<String, List<String>> content;
    private List<String> list;

    //===== CONSTRUCTOR
    public ExpandableListAdapter(Context context, Hash<String, List<String>> content, List<String> list){

        this.context = context;
        this.content = content;
        this.list = list;
    }

    //===== OVERRIDES
    @Override
    public int getGroupCount() {

        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return content.get(list.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {

        return list.get(groupPosition);
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {

        return content.get(list.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {

        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {

        return childPosition;
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }

    @Override
    public View getGroupView(int groupPosition,
                             boolean isExpanded, View convertView, ViewGroup parent) {

        String groupTitle = getGroup(groupPosition);

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.parent_layout, parent, false);
        }

        TextView groupTextView = (TextView) convertView.findViewById(R.id.parent_txt);
        groupTextView.setTypeface(null, Typeface.BOLD);

        groupTextView.setText(groupTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        String childTitle = getChild(groupPosition, childPosition);

        if(convertView == null){

            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.child_layout, parent, false);
        }

        TextView childTextView = (TextView) convertView.findViewById(R.id.child_txt);

        childTextView.setText(childTitle);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

} // ExpandableListAdapter
