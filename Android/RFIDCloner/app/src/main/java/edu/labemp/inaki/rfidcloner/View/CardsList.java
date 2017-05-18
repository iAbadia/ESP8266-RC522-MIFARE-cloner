package edu.labemp.inaki.rfidcloner.View;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.List;

import edu.labemp.inaki.rfidcloner.Controller.ESP8266Connector;
import edu.labemp.inaki.rfidcloner.Model.Card;
import edu.labemp.inaki.rfidcloner.Model.CardsDataSource;
import edu.labemp.inaki.rfidcloner.R;

public class CardsList extends AppCompatActivity {

    private static final int M_CONTEXT_EDIT = 0;
    private static final int M_CONTEXT_WRITE = 1;
    private static final int M_CONTEXT_DELETE = 2;


    private List<Card> cardsList;
    ListView listView;
    private CardListAdapter mAdapter;
    private CardsDataSource mCardsDataSource;
    private ESP8266Connector ESPConnector;
    private Context mContext = this;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Notify adapter
            Log.d("BroadcastReceiver", "Received: " + intent.getAction());
            if(intent.getAction().equals(ESP8266Connector.UPDATE_ACTION)) {
                //cardsList = mCardsDataSource.getCards();
                cardsList.clear();
                cardsList.addAll(mCardsDataSource.getCards());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mCardsDataSource = new CardsDataSource(this);
        cardsList = mCardsDataSource.getCards();

        mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card_list_item, cardsList);

        // Cards ListView
        listView = (ListView) findViewById(R.id.cards_listview);
        listView.setAdapter(mAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Activity)mContext).openContextMenu(view);
            }
        });

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab);


        final com.getbase.floatingactionbutton.FloatingActionButton actionNewCard =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_new_card);
        final com.getbase.floatingactionbutton.FloatingActionButton actionUpdateCards =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_update_cards);

        // ESPConnector
        ESPConnector = new ESP8266Connector(this);

        actionUpdateCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ESPConnector.getNewCards();
            }
        });

        // Register BroadcastReceiver for cardslist updates
        IntentFilter intentFilter = new IntentFilter(ESP8266Connector.UPDATE_ACTION);
        intentFilter.addAction(ESP8266Connector.UPDATE_ACTION_NO_NEW);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Actions");
        menu.add(Menu.NONE, M_CONTEXT_EDIT, Menu.NONE, "Edit card");
        menu.add(Menu.NONE, M_CONTEXT_WRITE, Menu.NONE, "Write card");
        menu.add(Menu.NONE, M_CONTEXT_DELETE, Menu.NONE, "Delete card");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean ret = super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case M_CONTEXT_EDIT:

                break;
            case M_CONTEXT_WRITE:

                break;
            case M_CONTEXT_DELETE:
                mAdapter.removeCard(info.position);
                break;
            default:
                Log.d("ContextMenuItem", "Something went wrong...");
        }
        Log.d("CardsListContextMenu", "item selected: " + item.getItemId());
        return ret;
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
            Intent i = new Intent(CardsList.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            if (mBroadcastReceiver != null)
                unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
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

        public void removeCard(int position) {
            mCardsDataSource.delCard(cardsList.get(position).getId());
            cardsList.remove(position);
            notifyDataSetChanged();
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
                viewHolder.cardUid = (TextView) convertView.findViewById(R.id.card_uid);

                result = convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.cardName.setText(card.getName());
            viewHolder.cardUid.setText(card.getUid());

            return convertView;
        }

        // View lookup cache
        private class ViewHolder {
            TextView cardName;
            TextView cardUid;
        }
    }
}
