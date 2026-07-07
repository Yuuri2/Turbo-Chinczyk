package com.turbochinczyk.backend.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PlayerJoinedMessage.class, name = "PLAYER_JOINED"),
    @JsonSubTypes.Type(value = PlayerReconnectedMessage.class, name = "PLAYER_RECONNECTED"),
    @JsonSubTypes.Type(value = PlayerDisconnectedMessage.class, name = "PLAYER_DISCONNECTED")
})
public interface LobbyMessage {
}