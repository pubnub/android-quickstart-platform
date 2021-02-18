package com.pubnub.android.pnquickstart;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Arrays;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private EditText entryUpdateText;
    private TextView messagesText;

    private PubNub pubnub;
    final private String clientUUID = "ReplaceWithYourClientIdentifier";
    private String theChannel = "the_guide";
    private String theEntry = "Earth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey("demo-36");
        pnConfiguration.setSubscribeKey("demo-36");
        pnConfiguration.setUuid(clientUUID);

        pubnub = new PubNub(pnConfiguration);

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void message(PubNub pubnub, PNMessageResult event) {
                JsonObject message = event.getMessage().getAsJsonObject();
                String entryVal = message.get("entry").getAsString();
                String updateVal = message.get("update").getAsString();

                displayMessage("[MESSAGE: received]",entryVal + ": " + updateVal);
            }

            @Override
            public void status(PubNub pubnub, PNStatus event) {
                displayMessage("[STATUS: " + event.getCategory() + "]",
                    "connected to channels: " + event.getAffectedChannels());

                if (event.getCategory().equals(PNStatusCategory.PNConnectedCategory)){
                    submitUpdate(theEntry, "Harmless.");
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult event) {
                displayMessage("[PRESENCE: " + event.getEvent() + ']',
                    "uuid: " + event.getUuid() + ", channel: " + event.getChannel());
            }

            // even if you don't need these callbacks, you still have include them
            // because we are extending an Abstract class
            @Override
            public void signal(PubNub pubnub, PNSignalResult event) { }

            @Override
            public void uuid(PubNub pubnub, PNUUIDMetadataResult pnUUIDMetadataResult) { }

            @Override
            public void channel(PubNub pubnub, PNChannelMetadataResult pnChannelMetadataResult) { }

            @Override
            public void membership(PubNub pubnub, PNMembershipResult event) { }

            @Override
            public void messageAction(PubNub pubnub, PNMessageActionResult event) { }

            @Override
            public void file(PubNub pubnub, PNFileEventResult event) { }
        });

        pubnub.subscribe()
            .channels(Arrays.asList(theChannel))
            .withPresence()
            .execute();

        entryUpdateText = findViewById(R.id.entry_update_text);
        messagesText = findViewById(R.id.messages_text);
    }

    protected void submitUpdate(String anEntry, String anUpdate) {
        JsonObject entryUpdate = new JsonObject();
        entryUpdate.addProperty("entry", anEntry);
        entryUpdate.addProperty("update", anUpdate);

        pubnub.publish().channel(theChannel).message(entryUpdate).async(
            new PNCallback<PNPublishResult>() {
                @Override
                public void onResponse(PNPublishResult result, PNStatus status) {
                    if (status.isError()) {
                        status.getErrorData().getThrowable().printStackTrace();
                    }
                    else {
                        displayMessage("[PUBLISH: sent]",
                            "timetoken: " + result.getTimetoken());
                    }
                }
            });
    }

    protected void displayMessage(String messageType, String aMessage) {
        String newLine = "\n";

        final StringBuilder textBuilder = new StringBuilder()
            .append(messageType)
            .append(newLine)
            .append(aMessage)
            .append(newLine).append(newLine)
            .append(messagesText.getText().toString());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesText.setText(textBuilder.toString());
            }
        });
    }

    @Override
    public void onClick(View view) {
        submitUpdate(theEntry, entryUpdateText.getText().toString());
        entryUpdateText.setText("");
    }
}
