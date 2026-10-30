import { get, writable } from "svelte/store";
import type {
    LobbySnapshot,
    LobbyMessage,
    SetReadyCommand,
    StartGameCommand
} from "./message";

export const socket = writable<WebSocket | null>(null);
export const lobby = writable<LobbySnapshot | null>(null);
export const lastCommandError = writable<string | null>(null);

export function connectToLobby(lobbyId: number) {
    const proto = location.protocol === "https:" ? "wss" : "ws";
    const serverUrl = `${proto}://${location.host}/game/${encodeURIComponent(lobbyId)}`;

    const ws = new WebSocket(serverUrl);
    ws.onopen = () => console.log("W in the chat");
    ws.onclose = () => {
        socket.set(null);
    };

    ws.onmessage = (event) => {
        handleMessage(JSON.parse(event.data) as LobbyMessage);
    };

    socket.set(ws);
}

export function disconnect() {
    socket.update((ws) => {
        if (ws) ws.close();
        return null;
    });
}

export function setReady(ready: boolean): string | null {
    return sendCommand({
        type: "SET_READY",
        requestId: crypto.randomUUID(),
        ready
    });
}

export function startGame(): string | null {
    return sendCommand({
        type: "START_GAME",
        requestId: crypto.randomUUID()
    });
}

function sendCommand(
    command: SetReadyCommand | StartGameCommand
): string | null {
    const activeSocket = get(socket);

    if (!activeSocket || activeSocket.readyState !== WebSocket.OPEN) {
        lastCommandError.set("WebSocket is not connected");
        return null;
    }

    activeSocket.send(JSON.stringify(command));
    return command.requestId;
}

function handleMessage(message: LobbyMessage) {
    if (message.type === "LOBBY_SNAPSHOT") {
        lobby.set({
            lobbyId: message.lobbyId,
            name: message.name,
            hostId: message.hostId,
            status: message.status,
            maxPlayers: message.maxPlayers,
            passwordProtected: message.passwordProtected,
            players: message.players
        });
        return;
    }

    if (message.type === "PLAYER_READY") {
        lobby.update((current) => current && {
            ...current,
            players: current.players.map((player) =>
                player.userId === message.userId
                    ? { ...player, ready: message.ready, online: true }
                    : player)
        });
        return;
    }

    if (message.type === "PLAYER_DISCONNECTED") {
        lobby.update((current) => current && {
            ...current,
            players: current.players.map((player) =>
                player.userId === message.userId
                    ? { ...player, online: false }
                    : player)
        });
        return;
    }

    if (message.type === "PLAYER_RECONNECTED") {
        lobby.update((current) => current && {
            ...current,
            players: current.players.map((player) =>
                player.userId === message.userId
                    ? { ...player, online: true }
                    : player)
        });
        return;
    }

    if (message.type === "COMMAND_REJECTED") {
        lastCommandError.set(message.message);
    }
}