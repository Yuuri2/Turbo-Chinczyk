import { get, writable } from "svelte/store";
import type {
    LobbySnapshot,
    LobbyMessage,
    SetReadyCommand,
    StartGameCommand,
    CommandAcknowledgedMessage
} from "./message";

export const socket = writable<WebSocket | null>(null);
export const lobby = writable<LobbySnapshot | null>(null);
export const lastCommandError = writable<string | null>(null);
export const connectionStatus = writable<
    "disconnected" | "connecting" | "connected"
>("disconnected");
export const pendingCommands = writable<Set<string>>(new Set());
export const lastCommandAcknowledgement = writable<CommandAcknowledgedMessage | null>(
    null
);

export function connectToLobby(lobbyId: number) {
    disconnect();

    const proto = location.protocol === "https:" ? "wss" : "ws";
    const serverUrl = `${proto}://${location.host}/game/${encodeURIComponent(lobbyId)}`;

    const ws = new WebSocket(serverUrl);
    connectionStatus.set("connecting");
    lastCommandError.set(null);
    lastCommandAcknowledgement.set(null);
    pendingCommands.set(new Set());

    ws.onopen = () => {
        connectionStatus.set("connected");
    };
    ws.onclose = () => {
        if (get(socket) === ws) {
            socket.set(null);
            connectionStatus.set("disconnected");
        }
    };
    ws.onerror = () => {
        if (get(socket) === ws) {
            lastCommandError.set("WebSocket connection failed");
        }
    };

    ws.onmessage = (event) => {
        try {
            handleMessage(JSON.parse(event.data) as LobbyMessage);
        } catch {
            lastCommandError.set("Received an invalid message from the server");
        }
    };

    socket.set(ws);
}

export function disconnect() {
    const activeSocket = get(socket);
    if (activeSocket) {
        activeSocket.close();
    }
    socket.set(null);
    connectionStatus.set("disconnected");
    pendingCommands.set(new Set());
    lobby.set(null);
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
    pendingCommands.update((pending) => new Set(pending).add(command.requestId));
    lastCommandError.set(null);
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
        lastCommandError.set(null);
        return;
    }

    if (message.type === "PLAYER_JOINED") {
        lobby.update((current) => {
            if (!current || current.players.some((player) => player.userId === message.userId)) {
                return current;
            }
            return {
                ...current,
                players: [
                    ...current.players,
                    {
                        userId: message.userId,
                        username: message.username,
                        online: true,
                        ready: false
                    }
                ]
            };
        });
        return;
    }

    if (message.type === "PLAYER_LEFT") {
        lobby.update((current) => current && {
            ...current,
            hostId: current.hostId === message.userId
                ? (current.players.find((player) => player.userId !== message.userId)?.userId
                    ?? current.hostId)
                : current.hostId,
            players: current.players.filter((player) => player.userId !== message.userId)
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
        if (message.requestId) {
            pendingCommands.update((pending) => {
                const next = new Set(pending);
                next.delete(message.requestId as string);
                return next;
            });
        }
        lastCommandError.set(message.message);
        return;
    }

    if (message.type === "COMMAND_ACK") {
        if (message.requestId) {
            pendingCommands.update((pending) => {
                const next = new Set(pending);
                next.delete(message.requestId as string);
                return next;
            });
        }
        lastCommandAcknowledgement.set(message);
    }
}