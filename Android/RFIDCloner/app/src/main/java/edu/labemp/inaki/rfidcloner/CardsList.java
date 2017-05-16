package edu.labemp.inaki.rfidcloner;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class CardsList extends AppCompatActivity {

    private ArrayList<Card> cardsList;
    ListView listView;
    private CardListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView=(ListView)findViewById(R.id.cards_listview);
        cardsList = new ArrayList<>();

        cardsList.add(new Card("Card1", false));
        cardsList.add(new Card("Card2", true));
        cardsList.add(new Card("Card3", false));
        cardsList.add(new Card("Card4", true));
        cardsList.add(new Card("Card5", true));

        mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card_list_item, cardsList);

        listView.setAdapter(mAdapter);


        /*setContentView(R.layout.activity_cards_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cards_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CardListAdapter extends ArrayAdapter<Card> {

        private List<Card> cardsList;
        Context mContext;

        public CardListAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
            this.mContext = context;
        }

        public CardListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Card> objects) {
            super(context, resource, objects);
            this.cardsList = objects;
            this.mContext = context;
        }

        private int lastPosition = -1;

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            Card card = getItem(position);

            final View result;

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.card_list_item, parent, false);
                viewHolder.cardName = (TextView) convertView.findViewById(R.id.card_name);
                viewHolder.syncCard = (TextView) convertView.findViewById(R.id.card_sync);

                result = convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.cardName.setText(card.getName());
            viewHolder.syncCard.setText(card.isSync() ? "Yes" : "No");

            return convertView;
        }

        // View lookup cache
        private class ViewHolder {
            TextView cardName;
            TextView syncCard;
        }
    }
}
