package com.hubspot.service;

import com.hubspot.client.HubspotEventClient;
import com.hubspot.model.Country;
import com.hubspot.model.Partner;
import com.hubspot.util.HubspotEventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HubspotEventServiceImplementation implements HubspotService{
    @Autowired
    HubspotEventClient hubspotEventClient;
    @Override
    public List<Partner> getPartnersAvailability() {
        return hubspotEventClient.getAvailability();
    }

    @Override
    public List<Country> getCountryInviteList(List<Partner> partnersList) throws ParseException {

        Map<String, Map<String, List<String>>> countryToDateAndNameList= filterCountryToDateAndNameList(partnersList);
       return filterCountriesFromMap(countryToDateAndNameList);
    }

    /**
     * This method constructs s map object from partnersList. The map object hold each country and its related available dates
     * and each date it will have list of email address of partners
     * @param partnersList
     * @return countryMap-Map<String, Map<String, List<String>>>
     * @throws ParseException
     */
    public static Map<String, Map<String, List<String>>> filterCountryToDateAndNameList(List<Partner> partnersList) throws ParseException {
        Map<String, Map<String, List<String>>> countryMap = new HashMap<>();
        //FOr each partner fetch the data and group it using map
        for (Partner partner : partnersList) {

            String participantCountry = partner.getCountry();
            String participantEmail = partner.getEmail();
            List<String> participantDatesInfo = new ArrayList<>(partner.getAvailableDates());
            //Sort dates based on earliest one
            Collections.sort(participantDatesInfo, (dateAInfo, dateBInfo) -> {
                Date dateA = null;
                Date dateB = null;
                try {
                    dateA = parseDate(dateAInfo);
                    dateB = parseDate(dateBInfo);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                return dateA.compareTo(dateB);
            });
            //Checking if saw the county already
            if (!countryMap.containsKey(participantCountry)) {
                countryMap.put(participantCountry, new HashMap<>());
            }

            if (participantDatesInfo.size() > 1) {
                Map<String, List<String>> dateMap = countryMap.get(participantCountry);
                String previousDateInfo = null;

                for (String participantDateInfo : participantDatesInfo) {
                    if (previousDateInfo == null) {
                        previousDateInfo = participantDateInfo;
                        continue;
                    }
                    //Fetching two dates for each partener

                    Date previousDate = parseDate(previousDateInfo);
                    Date currentDate = parseDate(participantDateInfo);
                    previousDate.setDate(previousDate.getDate() + 1);
                    //Checking if the dates are consecutive two days

                    if (previousDate.toString().equals(currentDate.toString())) {
                        if (!dateMap.containsKey(previousDateInfo)) {
                            dateMap.put(previousDateInfo, new ArrayList<>());
                        }
                        dateMap.get(previousDateInfo).add(participantEmail);
                    }

                    previousDateInfo = participantDateInfo;
                }
            }
        }

        return countryMap;
    }

    /**
     * This method takes each country's available date and its mapped partners email addresses and then sort all the dates
     * based on the count maximum count of attendees
     * @param map
     * @return
     */
    public static List<Country>  filterCountriesFromMap(Map<String, Map<String, List<String>>> map) {
        List<Country> countryInviteList = new ArrayList<>();
        map.forEach((key, value) -> {
            String name = key;
            List<Map.Entry<String, List<String>>> sortedData = new ArrayList<>(value.entrySet());
            Collections.sort(sortedData, (dateAInfo, dateBInfo) -> {
                //Sorting based on count of attendees
                int numOfParticipantDateA = dateAInfo.getValue().size();
                int numOfParticipantDateB = dateBInfo.getValue().size();

                //in case the count of attendees are equal then sort it base don date
                if (numOfParticipantDateA == numOfParticipantDateB) {
                    Date dateA = null;
                    Date dateB = null;
                    try {
                        dateA = parseDate(dateAInfo.getKey());
                        dateB = parseDate(dateBInfo.getKey());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    return dateA.compareTo(dateB);
                } else {
                    return numOfParticipantDateB - numOfParticipantDateA;
                }
            });

            String startDate = null;
            int attendeeCount = 0;
            List<String> attendees = new ArrayList<>();

            if (!sortedData.isEmpty()) {
                Map.Entry<String, List<String>> startDateEntry = sortedData.get(0);
                startDate = startDateEntry.getKey();
                attendeeCount = startDateEntry.getValue().size();
                attendees = new ArrayList<>(startDateEntry.getValue());
            }
            // construct invitation

            Country countryInvite =new Country();
            countryInvite.setName(name);
            countryInvite.setAttendees(attendees);
            countryInvite.setAttendeeCount(attendeeCount);
            countryInvite.setStartDate(startDate);
            countryInviteList.add(countryInvite);
        });

        return countryInviteList;
    }

    /**
     * Parse string date into java Date
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String dateString) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);
        return date;

    }
    @Override
    public String sendInvitations(List<Country> countryInviteList) throws HubspotEventException {
        return hubspotEventClient.sendInvite(countryInviteList);
    }
}
