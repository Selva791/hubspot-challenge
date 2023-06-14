package com.hubspot.service;

import com.hubspot.model.Country;
import com.hubspot.model.Partner;
import com.hubspot.util.HubspotEventException;

import java.text.ParseException;
import java.util.List;

public interface HubspotService {

    List<Partner> getPartnersAvailability();

    List<Country> getCountryInviteList(List<Partner> partnersList) throws ParseException;

    String sendInvitations(List<Country> countryInviteList) throws HubspotEventException;
}
