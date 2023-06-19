package ru.practicum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EndpointStatsList {
    private List<EndpointStats> endpointStats;

    public EndpointStatsList() {
        endpointStats = new ArrayList<>();
    }
}
