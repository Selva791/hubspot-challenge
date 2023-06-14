package com.hubspot.client;

import com.hubspot.model.Country;
import com.hubspot.model.CountryWrapper;
import com.hubspot.model.Partner;
import com.hubspot.model.PartnerWrapper;
import com.hubspot.util.HubspotEventException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HubspotEventClientImplementation implements HubspotEventClient {
    @Value("${hubspot.partner.url}")
    private String partnersUrl;

    @Value("${hubspot.invite.url}")
    private String inviteUrl;
    @Override
    public String sendInvite(List<Country> countryInviteList) throws HubspotEventException {
        String response;
        try {
            CountryWrapper countryWrapper =new CountryWrapper();
            countryWrapper.setCountries(countryInviteList);

            HttpEntity<CountryWrapper> request = new HttpEntity<>(countryWrapper);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Country> result = restTemplate.postForEntity(inviteUrl, request, Country.class);
            response = result.getStatusCode().toString();

        } catch (HttpClientErrorException ex) {
            System.out.println("Exception occurred while sending invitation: "+ex.getMessage());
            System.out.println("Status Code: "+ex.getStatusCode());
            throw new HubspotEventException(ex.getMessage());
        }
        return response;
    }

    @Override
    public List<Partner> getAvailability() {
        RestTemplate restTemplate = new RestTemplate();
        PartnerWrapper result = restTemplate.getForObject(partnersUrl, PartnerWrapper.class);
        return result.getPartners();
    }
}
