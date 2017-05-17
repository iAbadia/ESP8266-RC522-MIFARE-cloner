package edu.labemp.inaki.rfidcloner.View;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

import edu.labemp.inaki.rfidcloner.Controller.ESP8266Connector;
import edu.labemp.inaki.rfidcloner.Model.Card;
import edu.labemp.inaki.rfidcloner.Model.CardsDataSource;
import edu.labemp.inaki.rfidcloner.R;

public class CardsList extends AppCompatActivity {

    private List<Card> cardsList;
    ListView listView;
    private CardListAdapter mAdapter;
    private CardsDataSource mCardsDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView=(ListView)findViewById(R.id.cards_listview);
        /*cardsList = new ArrayList<>();

        cardsList.add(new Card("Card1"));
        cardsList.add(new Card("Card2"));
        cardsList.add(new Card("Card3"));
        cardsList.add(new Card("Card4"));
        cardsList.add(new Card("Card5"));
        cardsList.add(new Card("Card1"));
        cardsList.add(new Card("Card2"));
        cardsList.add(new Card("Card3"));
        cardsList.add(new Card("Card4"));
        cardsList.add(new Card("Card5"));
        cardsList.add(new Card("Card1"));
        cardsList.add(new Card("Card2"));
        cardsList.add(new Card("Card3"));
        cardsList.add(new Card("Card4"));
        cardsList.add(new Card("Card5"));
        cardsList.add(new Card("Card1"));
        cardsList.add(new Card("Card2"));
        cardsList.add(new Card("Card3"));
        cardsList.add(new Card("Card4"));
        cardsList.add(new Card("Card5"));
        cardsList.add(new Card("Card1"));
        cardsList.add(new Card("Card2"));
        cardsList.add(new Card("Card3"));
        cardsList.add(new Card("Card4"));
        cardsList.add(new Card("Card5"));*/

        mCardsDataSource = new CardsDataSource(this);

        //mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card_list_item, cardsList);
        mAdapter = new CardListAdapter(getApplicationContext(), R.layout.card_list_item, mCardsDataSource.getCards());

        listView.setAdapter(mAdapter);

        //com.getbase.floatingactionbutton.FloatingActionButton actionC = new com.getbase.floatingactionbutton.FloatingActionButton(getBaseContext());
        //actionC.setTitle("Hide/Show Action above");
        /*actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });*/

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab);
        //menuMultipleActions.addButton(actionC);

        final com.getbase.floatingactionbutton.FloatingActionButton actionReadCard =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_read_card);
        final com.getbase.floatingactionbutton.FloatingActionButton actionWriteCard =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_write_card);
        final com.getbase.floatingactionbutton.FloatingActionButton actionNewCard =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_new_card);

        /*actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //actionA.setTitle("Action A clicked");
            }
        });*/
        ESP8266Connector connector = new ESP8266Connector(this);
        connector.getNewCards();
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
                viewHolder.cardUid = (TextView) convertView.findViewById(R.id.card_uid);

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
