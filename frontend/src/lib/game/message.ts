export type LobbyStatus = "WAITING" | "PLAYING" | "FINISHED";

export interface LobbyPlayer {
    userId: number;
    username: string;
    online: boolean;
    ready: boolean;
}

export interface LobbySnapshot {
    lobbyId: number;
    name: string;
    hostId: number;
    status: LobbyStatus;
    maxPlayers: number;
    passwordProtected: boolean;
    players: LobbyPlayer[];
}

export interface LobbySnapshotMessage {
    type: "LOBBY_SNAPSHOT";
    lobbyId: number;
    name: string;
    hostId: number;
    status: LobbyStatus;
    maxPlayers: number;
    passwordProtected: boolean;
    players: LobbyPlayer[];
}

export interface PlayerJoinedMessage {
    type: "PLAYER_JOINED";
    userId: number;
    username: string;
}

export interface PlayerReconnectedMessage {
    type: "PLAYER_RECONNECTED";
    userId: number;
    username: string;
}

export interface PlayerDisconnectedMessage {
    type: "PLAYER_DISCONNECTED";
    userId: number;
    username: string;
}

export interface PlayerLeftMessage {
    type: "PLAYER_LEFT";
    userId: number;
    username: string;
}

export interface PlayerReadyMessage {
    type: "PLAYER_READY";
    userId: number;
    username: string;
    ready: boolean;
}

export interface CommandAcknowledgedMessage {
    type: "COMMAND_ACK";
    requestId: string | null;
    command: string;
}

export interface CommandRejectedMessage {
    type: "COMMAND_REJECTED";
    requestId: string | null;
    command: string;
    code: string;
    message: string;
}

export type LobbyMessage =
    | LobbySnapshotMessage
    | PlayerJoinedMessage
    | PlayerReconnectedMessage
    | PlayerDisconnectedMessage
    | PlayerLeftMessage
    | PlayerReadyMessage
    | CommandAcknowledgedMessage
    | CommandRejectedMessage;

export interface SetReadyCommand {
    type: "SET_READY";
    requestId: string;
    ready: boolean;
}

export interface StartGameCommand {
    type: "START_GAME";
    requestId: string;
}
