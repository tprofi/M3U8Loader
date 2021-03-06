package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import go.m3u8.Item;
import go.m3u8.List;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class ListEditActivity extends AppCompatActivity {
    private Loader loader;
    private List list;
    private AdaptorEditList listviewadapter;
    private ArrayList<Integer> path = new ArrayList<>();
    private boolean isSelectLeftBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_list_edit);
        findViewById(R.id.loaderListProgressBar).setVisibility(View.GONE);
        findViewById(R.id.loaderListMenu).setVisibility(View.VISIBLE);
        findViewById(R.id.btnLoaderListNavUp).setVisibility(View.GONE);
        final ListView listView = (ListView) findViewById(R.id.loaderEditList);
        listviewadapter = new AdaptorEditList();
        listView.setAdapter(listviewadapter);
        Intent intent = getIntent();
        int id = intent.getIntExtra("LoaderID", -1);
        if (id == -1)
            return;

        loader = LoaderServiceHandler.GetLoader(id);
        if (loader == null)
            return;

        loader.Stop();
        list = loader.GetList();
        if (list == null) {
            findViewById(R.id.loaderListProgressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.loaderListMenu).setVisibility(View.GONE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String ret = loader.LoadListOpts(ListEditActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ret.isEmpty()) {
                                Toast.makeText(ListEditActivity.this, ret, Toast.LENGTH_SHORT).show();
                                ListEditActivity.this.finish();
                            } else {
                                list = loader.GetList();
                                findViewById(R.id.loaderListProgressBar).setVisibility(View.GONE);
                                findViewById(R.id.loaderListMenu).setVisibility(View.VISIBLE);
                                updateUI();
                            }
                        }
                    });
                }
            }).start();
        }

        isSelectLeftBar = false;

        updateUI();
        final SeekBar rangeSeekbar = (SeekBar) findViewById(R.id.rangeSeekbar);
        rangeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            Number minValue = 0;
            Number maxValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minValue = 0;
                maxValue = list.itemsSize();

                if (isSelectLeftBar)
                    maxValue = progress;
                else
                    minValue = progress;
                ((TextView) findViewById(R.id.textViewRangeLeft)).setText(minValue.toString());
                ((TextView) findViewById(R.id.textViewRangeRight)).setText(maxValue.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (list != null)
                    for (int i = 0; i < list.itemsSize(); i++)
                        if (list.getItem(i) != null) {
                            if (i >= minValue.intValue() && i <= maxValue.intValue())
                                list.getItem(i).setIsLoad(true);
                            else
                                list.getItem(i).setIsLoad(false);
                        }
                loader.SaveList();
                listviewadapter.notifyDataSetChanged();
            }
        });
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final SeekBar rangeSeekbar = (SeekBar) findViewById(R.id.rangeSeekbar);

                if (list == null || list.itemsSize() == 0)
                    findViewById(R.id.rangeUI).setVisibility(View.GONE);
                else {
                    findViewById(R.id.rangeUI).setVisibility(View.VISIBLE);
                    rangeSeekbar.setMax((int) list.itemsSize());
                }
                listviewadapter.notifyDataSetChanged();
            }
        });
    }

    private void findList() {
        list = loader.GetList();
        updateUI();
        if (list == null)
            return;
        if (path.size() == 0) {
            findViewById(R.id.btnLoaderListNavUp).setVisibility(View.GONE);
            return;
        } else
            findViewById(R.id.btnLoaderListNavUp).setVisibility(View.VISIBLE);
        for (int i : path)
            list = list.getList(i);
        updateUI();
    }

    public void upBtnClick(View view) {
        if (path.size() > 0)
            path.remove(path.size() - 1);
        findList();
    }

    public void allSelBtnClick(View view) {
        if (list == null)
            return;
        for (int i = 0; i < list.itemsSize(); i++)
            list.getItem(i).setIsLoad(true);
        for (int i = 0; i < list.listsSize(); i++)
            list.getList(i).setLoadList(true);
        loader.SaveList();
        listviewadapter.notifyDataSetChanged();
        isSelectLeftBar = false;
    }

    public void noneSelBtnClick(View view) {
        if (list == null)
            return;
        for (int i = 0; i < list.itemsSize(); i++)
            list.getItem(i).setIsLoad(false);
        for (int i = 0; i < list.listsSize(); i++)
            list.getList(i).setLoadList(false);
        loader.SaveList();
        listviewadapter.notifyDataSetChanged();
        isSelectLeftBar = true;
    }

    public void reversSelBtnClick(View view) {
        if (list == null)
            return;
        for (int i = 0; i < list.itemsSize(); i++)
            list.getItem(i).setIsLoad(!list.getItem(i).getIsLoad());
        for (int i = 0; i < list.listsSize(); i++)
            list.getList(i).setLoadList(!list.getList(i).isLoadList());
        loader.SaveList();
        listviewadapter.notifyDataSetChanged();
    }

    public class AdaptorEditList extends BaseAdapter {

        @Override
        public int getCount() {
            if (list == null)
                return 0;
            return (int) (list.listsSize() + list.itemsSize());
        }

        @Override
        public Object getItem(int position) {
            if (position < list.listsSize())
                return list.getList(position);
            else
                return list.getItem(position - list.listsSize());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) ListEditActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listview_item_loaderlist, null);
            }
            if (position < list.listsSize()) {
                //List
                List subList = list.getList(position);
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setText(subList.getName());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setChecked(subList.isLoadList());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (list != null && list.getList(position) != null)
                            list.getList(position).setLoadList(b);
                        loader.SaveList();
                    }
                });
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        path.add(position);
                        findList();
                    }
                });

            } else {
                //Item
                Item item = list.getItem(position - list.listsSize());
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setImageResource(R.drawable.ic_play_arrow_black_24dp);
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setText(item.getUrl());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setChecked(item.getIsLoad());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        long pos = position - list.listsSize();
                        if (list != null && list.getItem(pos) != null)
                            list.getItem(pos).setIsLoad(b);
                    }
                });
            }

            return v;
        }
    }
}
