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
    @JsonSubTypes.Type(value = PlayerDisconnectedMessage.class, name = "PLAYER_DISCONNECTED"),
    @JsonSubTypes.Type(value = PlayerLeftMessage.class, name = "PLAYER_LEFT"),
    @JsonSubTypes.Type(value = LobbySnapshotMessage.class, name = "LOBBY_SNAPSHOT"),
    @JsonSubTypes.Type(value = PlayerReadyMessage.class, name = "PLAYER_READY"),
    @JsonSubTypes.Type(value = CommandAcknowledgedMessage.class, name = "COMMAND_ACK"),
    @JsonSubTypes.Type(value = CommandRejectedMessage.class, name = "COMMAND_REJECTED"),
    @JsonSubTypes.Type(value = StartGameCommand.class, name = "START_GAME"),
    @JsonSubTypes.Type(value = SetReadyCommand.class, name = "SET_READY")
})
public interface LobbyMessage {
}