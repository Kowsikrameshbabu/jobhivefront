package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.TwilioService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.ValidationRequest;
import com.twilio.type.PhoneNumber;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

@RestController
@CrossOrigin(origins = "http://localhost:3000") 
@RequestMapping("/api")
public class TwilioController {

    @Autowired
    private TwilioService twilioService;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.verify-service-sid}")
    private String verifyServiceSid;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/make-call")
    public String makeCall(@RequestBody CallRequest callRequest) {
        String callSid = twilioService.makeCall(
                callRequest.getToPhoneNumber(),
                callRequest.getUsername(),
                callRequest.getCompany(),
                callRequest.getStatus()

        );
        return "Call initiated with SID: " + callSid;
    }
    
    

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String phoneNumber) {
        Twilio.init(accountSid, authToken);

        Verification verification = Verification.creator(
                verifyServiceSid,
                phoneNumber,
                "sms"
        ).create();

        return "OTP sent successfully!";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String phoneNumber, @RequestParam String otp, @RequestParam String friendlyName) {
        Twilio.init(accountSid, authToken);

        VerificationCheck verificationCheck = VerificationCheck.creator(
                verifyServiceSid,
                otp
        ).setTo(phoneNumber).create();

        if ("approved".equals(verificationCheck.getStatus())) {
            PhoneNumber twilioPhoneNumber = new PhoneNumber(phoneNumber);
            ValidationRequest validationRequest = ValidationRequest.creator(twilioPhoneNumber)
                    .setFriendlyName(friendlyName)
                    .create();

            return "OTP verified successfully! Caller ID added with Validation Code: " + validationRequest.getValidationCode();
        } else {
            return "Invalid OTP. Please try again.";
        }
    }

    @PostMapping("/incoming-call")
    public String handleIncomingCall() {
        Say say = new Say.Builder("Thank you for calling. Please press 1 to leave a message or press 2 to speak with an operator.").build();
        Gather gather = new Gather.Builder()
                .say(say)
                .numDigits(1)
                .action("/api/handle-gather")
                .method(com.twilio.http.HttpMethod.POST)
                .build();

        VoiceResponse response = new VoiceResponse.Builder()
                .gather(gather)
                .build();

        try {
            return response.toXml();
        } catch (TwiMLException e) {
            throw new RuntimeException("Error generating TwiML", e);
        }
    }

    @PostMapping("/gather")
    public String gatherResponse(@RequestParam("Digits") String digits) {
        String message;
        if ("1".equals(digits)) {
            message = "You have confirmed the action.";
            System.out.println("User pressed 1");
        } else if ("2".equals(digits)) {
            message = "You have canceled the action.";
            System.out.println("User pressed 2");
        } else {
            message = "Invalid input.";
            System.out.println("User pressed an invalid input: " + digits);
        }
        Say say = new Say.Builder(message).build();
        VoiceResponse response = new VoiceResponse.Builder().say(say).build();
        try {
            return response.toXml();
        } catch (TwiMLException e) {
            throw new RuntimeException("Error generating TwiML", e);
        }
    }

    @PostMapping("/add-caller-id")
    public String addCallerId(@RequestParam String phoneNumber, @RequestParam String friendlyName) {
        Twilio.init(accountSid, authToken);
        PhoneNumber twilioPhoneNumber = new PhoneNumber(phoneNumber);
        ValidationRequest validationRequest = ValidationRequest.creator(twilioPhoneNumber)
                .setFriendlyName(friendlyName)
                .create();
        return "Caller ID added successfully with Validation Code: " + validationRequest.getValidationCode();
    }
}

 class CallRequest {
    private String ToPhoneNumber;
    private String Username;
    private String Status;
    private String Company;

    // Getters and Setters
    public String getToPhoneNumber() {
        return ToPhoneNumber;
    }

    public void setToPhoneNumber(String toPhoneNumber) {
        this.ToPhoneNumber = ToPhoneNumber;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = Username;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String Company) {
        this.Company = Company;
    }


}
