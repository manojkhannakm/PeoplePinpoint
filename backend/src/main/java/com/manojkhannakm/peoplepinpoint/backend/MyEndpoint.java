package com.manojkhannakm.peoplepinpoint.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.manojkhannakm.peoplepinpoint.backend.model.Person;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Manoj Khanna
 */

@Api(
        name = "myApi",
        version = "v1",
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.WEB_CLIENT_ID}
)
public class MyEndpoint {

    @ApiMethod(name = "signUpPerson")
    public Person signUpPerson(@Named("clientId") String clientId,
                               @Named("name") String name,
                               @Named("email") String email,
                               @Named("password") String password) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query(Constants.KIND_PERSON)
                .setKeysOnly()
                .setFilter(new Query.FilterPredicate(Constants.PROPERTY_PERSON_EMAIL, Query.FilterOperator.EQUAL, email));
        PreparedQuery preparedQuery = datastore.prepare(query);
        int count = preparedQuery.countEntities(FetchOptions.Builder.withDefaults());
        if (count != 0) {
            return null;
        }

        Entity person = new Entity(Constants.KIND_PERSON);
        person.setProperty(Constants.PROPERTY_PERSON_NAME, name);
        person.setProperty(Constants.PROPERTY_PERSON_EMAIL, email);
        person.setProperty(Constants.PROPERTY_PERSON_PASSWORD, password);
        person.setProperty(Constants.PROPERTY_PERSON_LATITUDE, 0.0);
        person.setProperty(Constants.PROPERTY_PERSON_LONGITUDE, 0.0);
        person.setProperty(Constants.PROPERTY_PERSON_LOCATION_UPDATE_TIME, 0L);
        datastore.put(person);

        return new Person(person);
    }

    @ApiMethod(name = "signInPerson")
    public Person signInPerson(@Named("clientId") String clientId,
                               @Named("email") String email,
                               @Named("password") String password) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.FilterPredicate emailFilter = new Query.FilterPredicate(Constants.PROPERTY_PERSON_EMAIL, Query.FilterOperator.EQUAL, email),
                passwordFilter = new Query.FilterPredicate(Constants.PROPERTY_PERSON_PASSWORD, Query.FilterOperator.EQUAL, password);
        Query query = new Query(Constants.KIND_PERSON)
                .setFilter(Query.CompositeFilterOperator.and(emailFilter, passwordFilter));
        PreparedQuery preparedQuery = datastore.prepare(query);
        int count = preparedQuery.countEntities(FetchOptions.Builder.withDefaults());
        if (count == 0) {
            return null;
        }

        Entity localPerson = preparedQuery.asSingleEntity();
        return new Person(localPerson);
    }

    @ApiMethod(name = "person")
    public Person person(@Named("clientId") String clientId,
                         @Named("localPersonId") long localPersonId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "sentPeople")
    public ArrayList<Person> sentPeople(@Named("clientId") String clientId,
                                        @Named("localPersonId") long localPersonId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> sentIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
            if (sentIdList == null) {
                return new ArrayList<>();
            }

            ArrayList<Key> keyList = new ArrayList<>();
            for (Long id : sentIdList) {
                keyList.add(KeyFactory.createKey(Constants.KIND_PERSON, id));
            }

            Query query = new Query(Constants.KIND_PERSON)
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_NAME, String.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_EMAIL, String.class))
                    .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, keyList))
                    .addSort(Constants.PROPERTY_PERSON_NAME);
            PreparedQuery preparedQuery = datastore.prepare(query);

            Iterator<Entity> iterator = preparedQuery.asIterator();
            ArrayList<Person> personList = new ArrayList<>();
            while (iterator.hasNext()) {
                Person person = new Person(iterator.next());
                personList.add(person);
            }

            return personList;
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "receivedPeople")
    public ArrayList<Person> receivedPeople(@Named("clientId") String clientId,
                                            @Named("localPersonId") long localPersonId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> receivedIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
            if (receivedIdList == null) {
                return new ArrayList<>();
            }

            ArrayList<Key> keyList = new ArrayList<>();
            for (Long id : receivedIdList) {
                keyList.add(KeyFactory.createKey(Constants.KIND_PERSON, id));
            }

            Query query = new Query(Constants.KIND_PERSON)
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_NAME, String.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_EMAIL, String.class))
                    .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, keyList))
                    .addSort(Constants.PROPERTY_PERSON_NAME);
            PreparedQuery preparedQuery = datastore.prepare(query);

            Iterator<Entity> iterator = preparedQuery.asIterator();
            ArrayList<Person> personList = new ArrayList<>();
            while (iterator.hasNext()) {
                Person person = new Person(iterator.next());
                personList.add(person);
            }

            return personList;
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "people")
    public ArrayList<Person> people(@Named("clientId") String clientId,
                                    @Named("localPersonId") long localPersonId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> peopleIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);
            if (peopleIdList == null) {
                return new ArrayList<>();
            }

            ArrayList<Key> keyList = new ArrayList<>();
            for (Long id : peopleIdList) {
                keyList.add(KeyFactory.createKey(Constants.KIND_PERSON, id));
            }

            Query query = new Query(Constants.KIND_PERSON)
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_NAME, String.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_EMAIL, String.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_LATITUDE, Double.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_LONGITUDE, Double.class))
                    .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_LOCATION_UPDATE_TIME, Long.class))
                    .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, keyList))
                    .addSort(Constants.PROPERTY_PERSON_NAME);
            PreparedQuery preparedQuery = datastore.prepare(query);

            Iterator<Entity> iterator = preparedQuery.asIterator();
            ArrayList<Person> personList = new ArrayList<>();
            while (iterator.hasNext()) {
                Person person = new Person(iterator.next());
                personList.add(person);
            }

            return personList;
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "addPerson")
    public Person addPerson(@Named("clientId") String clientId,
                            @Named("localPersonId") long localPersonId,
                            @Named("personId") long personId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> sentIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            sentIdList.add(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_SENT_IDS, sentIdList);
            datastore.put(localPerson);

            Entity person = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, personId));
            //noinspection unchecked
            ArrayList<Long> receivedIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            receivedIdList.add(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS, receivedIdList);
            datastore.put(person);

            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "cancelPerson")
    public Person cancelPerson(@Named("clientId") String clientId,
                               @Named("localPersonId") long localPersonId,
                               @Named("personId") long personId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> sentIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            sentIdList.remove(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_SENT_IDS, sentIdList);
            datastore.put(localPerson);

            Entity person = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, personId));
            //noinspection unchecked
            ArrayList<Long> receivedIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            receivedIdList.remove(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS, receivedIdList);
            datastore.put(person);

            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "acceptPerson")
    public Person acceptPerson(@Named("clientId") String clientId,
                               @Named("localPersonId") long localPersonId,
                               @Named("personId") long personId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));

            //noinspection unchecked
            ArrayList<Long> receivedIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            receivedIdList.remove(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS, receivedIdList);

            //noinspection unchecked
            ArrayList<Long> peopleIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            peopleIdList.add(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS, peopleIdList);

            datastore.put(localPerson);

            Entity person = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, personId));

            //noinspection unchecked
            ArrayList<Long> sentIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            sentIdList.remove(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_SENT_IDS, sentIdList);

            //noinspection unchecked
            peopleIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            peopleIdList.add(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS, peopleIdList);

            datastore.put(person);

            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "rejectPerson")
    public Person rejectPerson(@Named("clientId") String clientId,
                               @Named("localPersonId") long localPersonId,
                               @Named("personId") long personId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> receivedIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            receivedIdList.remove(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS, receivedIdList);
            datastore.put(localPerson);

            Entity person = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, personId));
            //noinspection unchecked
            ArrayList<Long> sentIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            sentIdList.remove(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_SENT_IDS, sentIdList);
            datastore.put(person);

            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "removePerson")
    public Person removePerson(@Named("clientId") String clientId,
                               @Named("localPersonId") long localPersonId,
                               @Named("personId") long personId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            //noinspection unchecked
            ArrayList<Long> peopleIdList = (ArrayList<Long>) localPerson.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            peopleIdList.remove(personId);
            localPerson.setProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS, peopleIdList);
            datastore.put(localPerson);

            Entity person = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, personId));
            //noinspection unchecked
            peopleIdList = (ArrayList<Long>) person.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            peopleIdList.remove(localPersonId);
            person.setProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS, peopleIdList);
            datastore.put(person);

            return new Person(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @ApiMethod(name = "updatePersonLocation")
    public void updatePersonLocation(@Named("clientId") String clientId,
                                     @Named("localPersonId") long localPersonId,
                                     @Named("latitude") double latitude,
                                     @Named("longitude") double longitude,
                                     @Named("locationUpdateTime") long locationUpdateTime) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity localPerson = datastore.get(KeyFactory.createKey(Constants.KIND_PERSON, localPersonId));
            localPerson.setProperty(Constants.PROPERTY_PERSON_LATITUDE, latitude);
            localPerson.setProperty(Constants.PROPERTY_PERSON_LONGITUDE, longitude);
            localPerson.setProperty(Constants.PROPERTY_PERSON_LOCATION_UPDATE_TIME, locationUpdateTime);
            datastore.put(localPerson);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @ApiMethod(name = "searchPeople")
    public ArrayList<Person> searchPeople(@Named("clientId") String clientId,
                                          @Named("queryString") String queryString,
                                          @Named("localPersonId") long localPersonId) {
        if (!clientId.equals(Constants.ANDROID_CLIENT_ID)) {
            throw new IllegalArgumentException("Invalid client id!");
        }

        queryString = queryString.toLowerCase();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query(Constants.KIND_PERSON)
                .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_NAME, String.class))
                .addProjection(new PropertyProjection(Constants.PROPERTY_PERSON_EMAIL, String.class))
                .addSort(Constants.PROPERTY_PERSON_NAME);
        PreparedQuery preparedQuery = datastore.prepare(query);

        Iterator<Entity> iterator = preparedQuery.asIterator();
        ArrayList<Person> personList = new ArrayList<>();
        while (iterator.hasNext()) {
            Person person = new Person(iterator.next());
            if (person.getId() == localPersonId) {
                continue;
            }

            if (person.getName().toLowerCase().contains(queryString)
                    || person.getEmail().toLowerCase().contains(queryString)) {
                personList.add(person);
            }
        }

        return personList;
    }

}
