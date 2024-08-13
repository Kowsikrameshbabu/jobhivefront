package com.example.demo.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

@Service
public class TwilioService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public String makeCall(String ToPhoneNumber, String Username, String Company,String Status) {
        String message = String.format("Hello %s, You have beeen selected for  %s .You will be informed shortly, about the cover letter. Thank You", 
                                       Username, Company);
    
        Gather gather = new Gather.Builder()
                .say(new Say.Builder(message).build())
                .numDigits(1)
                .action("/api/gather")
                .method(com.twilio.http.HttpMethod.POST)
                .build();
    
        VoiceResponse response = new VoiceResponse.Builder()
                .gather(gather)
                .build();
    
        String twimlString;
        try {
            twimlString = response.toXml();
        } catch (TwiMLException e) {
            throw new RuntimeException("Error generating TwiML", e);
        }
    
        Call call = Call.creator(
                new PhoneNumber(ToPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                new com.twilio.type.Twiml(twimlString)
        ).create();
    
        return call.getSid();
    }
    
    
}
