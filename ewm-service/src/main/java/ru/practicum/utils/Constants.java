package ru.practicum.utils;

import java.time.format.DateTimeFormatter;

public class Constants {
    private Constants() {
    }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String INCORRECTLY_MADE_REQUEST_MESSAGE = "Incorrectly made request.";
    public static final String UNKNOWN_ERROR_MESSAGE = "Unknown error happened.";
    public static final String METHOD_NOT_SUPPORTED_MESSAGE = "Http method is not supported.";
    public static final String DATA_INTEGRITY_VIOLATION_MESSAGE = "Integrity constraint has been violated.";
    public static final String NOT_FOUND_MESSAGE = "The required object was not found.";
    public static final String USER_NOT_FOUND_MESSAGE = "User with id=%d was not found.";
    public static final String USER_EVENT_NOT_FOUND_MESSAGE = "User with id=%d doesn't have the event with id=%d.";
    public static final String USER_REQUEST_NOT_FOUND_MESSAGE = "User with id=%d doesn't have the participation request with id=%d.";
    public static final String EVENT_NOT_FOUND_MESSAGE = "Event with id=%d was not found.";
    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category with id=%d was not found.";
    public static final String COMPILATION_NOT_FOUND_MESSAGE = "Compilation with id=%d was not found.";
    public static final String EVENT_DATE_SHOULD_BE_IN_FUTURE_MESSAGE = "Event date should be not earlier than %s.";
    public static final String EVENT_SHOULD_BE_LATER_THAN_ONE_HOUR_MESSAGE = "Event couldn't be published before less than one hour to start.";
    public static final String OPERATION_CONDITIONS_NOT_MET_MESSAGE = "For the requested operation the conditions are not met.";
    public static final String CANNOT_PUBLISH_NOT_PENDING_EVENT_MESSAGE = "Cannot publish the event because it's not in the right state: %s.";
    public static final String CANNOT_CANCEL_NOT_PENDING_EVENT_MESSAGE = "Cannot cancel the event because it's not in the right state: %s.";
    public static final String CANNOT_REQUEST_NOT_PUBLISHED_EVENT_MESSAGE = "Cannot create participation request because event not in the right state: %s.";
    public static final String CANNOT_MODERATE_NOT_PENDING_REQUEST_MESSAGE = "Cannot moderate participation request because request not in the right status: %s.";
    public static final String CANNOT_MODERATE_REQUEST_WITH_SUCH_STATUS_MESSAGE = "Cannot moderate participation request with such status: %s.";
    public static final String PUBLISHED_EVENT_UPDATE_PROHIBITED_MESSAGE = "Only pending or canceled events can be changed.";
    public static final String SORT_TYPE_DOES_NOT_EXIST_MESSAGE = "Sort type %s does not exist. Events could be sorted by views or event date.";
    public static final String EVENT_STATE_DOES_NOT_EXIST_MESSAGE = "Event state %s does not exist.";
    public static final String INITIATOR_CANNOT_CREATE_REQUEST_MESSAGE = "Event initiator can't create participation request in it.";
    public static final String EVENT_NOT_AVAILABLE_MESSAGE = "Event with id=%d has reached participation request limit and not available.";
    public static final String NOT_INITIATOR_CANNOT_GET_EVENT_REQUESTS_MESSAGE = "Only event initiator can get event participation requests.";
    public static final String CANNOT_MODERATE_REQUEST_IN_ALREADY_STARTED_EVENT_MESSAGE = "Cannot moderate event requests after the event has started.";
    public static final String CANNOT_CREATE_REQUEST_IN_ALREADY_STARTED_EVENT_MESSAGE = "Cannot create event requests after the event has started.";
    public static final String EVENT_DOESNT_NEED_REQUEST_MODERATION_MESSAGE = "Event with id=%d doesn't need moderation of requests.";
    public static final String EVENT_REACHED_PARTICIPANT_LIMIT_MESSAGE = "Event with id=%d has reached participant limit and not available for request moderation.";
    public static final String EVENT_WILL_REACH_PARTICIPANT_LIMIT_MESSAGE = "Event participation requests count is more than available to confirm.";
    public static final String NOT_ALL_REQUESTS_FOUND_MESSAGE = "Not all event participation requests were found.";
    public static final String NOT_ALL_EVENTS_FOUND_MESSAGE = "Not all events were found.";
    public static final String END_SHOULD_BE_AFTER_START_MESSAGE = "Incorrect range: %s - %s. End date should be after start date.";
    public static final String FRIEND_REQUEST_ALREADY_SENT_MESSAGE = "You have already sent friendship request to user with id=%d.";
    public static final String IDS_SHOULD_BE_DIFFERENT_MESSAGE = "Ids of user and friend should be different.";
    public static final String FRIEND_NOT_FOUND_MESSAGE = "You don't following user with id=%d and he isn't your friend.";
    public static final String FOLLOWER_NOT_FOUND_MESSAGE = "Follower with id=%d was not found.";
    public static final String NEED_CANCEL_FRIENDSHIP_REQUEST_FIRST = "You have to cancel friendship request from user with id=%d before removing him from followers.";
    public static final String FRIENDSHIP_REQUEST_NOT_FOUND_MESSAGE = "Friendship request from user with id=%d doesn't found.";
    public static final String NOT_FRIEND_OR_FOLLOWER_MESSAGE = "You aren't friend or follower of user with id=%d";
    public static final String FRIENDS_NOT_FOUND = "You don't have friends";
    public static final String FRIENDS_OR_FOLLOWINGS_NOT_FOUND = "You don't have friends and followings";
    public static final String EVENT_ENDPOINT = "/events/%s";
    public static final String APP_NAME = "ewm-main-service";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_START_VALUE = "0";

}
