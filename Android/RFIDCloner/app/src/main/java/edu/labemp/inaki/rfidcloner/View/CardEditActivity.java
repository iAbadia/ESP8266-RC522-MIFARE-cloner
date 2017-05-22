package edu.labemp.inaki.rfidcloner.View;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import org.w3c.dom.Text;

import edu.labemp.inaki.rfidcloner.Controller.ESP8266Connector;
import edu.labemp.inaki.rfidcloner.Model.Card;
import edu.labemp.inaki.rfidcloner.Model.CardsDataSource;
import edu.labemp.inaki.rfidcloner.R;

public class CardEditActivity extends AppCompatActivity {

    private CardsDataSource mCardsDataSource;
    private Card editCard;
    private EditText nameEditText, uidEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);

        Intent editIntent = getIntent();
        int cardId = editIntent.getIntExtra(CardsList.EDIT_EXTRA, 0);
        mCardsDataSource = new CardsDataSource(this);

        editCard = mCardsDataSource.getCard(cardId);

        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        uidEditText = (EditText) findViewById(R.id.uid_edit_text);
        nameEditText.setText(editCard.getName());
        uidEditText.setText(editCard.getUid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        editCard.setName(nameEditText.getText().toString());
        editCard.setUid(uidEditText.getText().toString());
        mCardsDataSource.updateCard(editCard);
        mCardsDataSource.notifyCardsList(this, ESP8266Connector.UPDATE_ACTION);
    }
}
