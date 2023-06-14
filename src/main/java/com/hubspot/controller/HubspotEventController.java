package com.hubspot.controller;

import com.hubspot.model.Country;
import com.hubspot.model.Partner;
import com.hubspot.service.HubspotService;
import com.hubspot.util.HubspotEventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/hubspot/event/post/")
public class HubspotEventController {

    private  HubspotService hubspotService;
    private List<Partner> partnersList;

    private List<Country> invitationsList;
    @Autowired
    public HubspotEventController(HubspotService hubspotService){
       this.hubspotService = hubspotService;
    }

    @RequestMapping(value="/invite/partners",method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String getPartnersAndSendInvitations() throws HubspotEventException {

        try{
            partnersList = hubspotService.getPartnersAvailability();

            if (partnersList==null || partnersList.size()==0) {
                return "Empty Partners List";
            }

            invitationsList = hubspotService.getCountryInviteList(partnersList);
            return hubspotService.sendInvitations(invitationsList);
        }catch (HubspotEventException ex){
            System.out.println("Exception while proccesing invitation to Partners");
            throw new HubspotEventException(ex.getMessage());
        } catch (ParseException ex) {
            throw new HubspotEventException("Date format parsing error: "+ex.getMessage());
        }

    }
}

