package com.bluebear.cinemax.service.sms;
import com.bluebear.cinemax.config.TwilioConfig;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.bluebear.cinemax.config.EsmsConfig;
import com.twilio.rest.api.v2010.account.Message;
import org.yaml.snakeyaml.util.UriEncoder;
import com.twilio.type.PhoneNumber;
@Service
public class SmsServiceImp implements SmsService{
    private final TwilioConfig props;
    public SmsServiceImp(TwilioConfig props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        Twilio.init(props.getAccountSid(), props.getAuthToken());
    }

    @Override
    public void sendSms(String toPhone, String message) {
        Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(props.getFromPhone()),
                message
        ).create();
    }
}
