package com.hubspot.client;

import com.hubspot.model.Country;
import com.hubspot.model.Partner;
import com.hubspot.util.HubspotEventException;

import java.util.List;

public interface HubspotEventClient {
   String sendInvite(List<Country> countryInviteList) throws HubspotEventException;
   List<Partner> getAvailability();
}
