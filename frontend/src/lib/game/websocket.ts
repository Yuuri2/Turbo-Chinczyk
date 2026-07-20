import { writable } from "svelte/store";
import { type Pluh } from "./message";

export const socket = writable<WebSocket | null>(null);

export function connectToLobby(lobbyId: number, sessionToken: string) {
    const proto = location.protocol === "https:" ? "wss" : "ws";
    const serverUrl = `${proto}://${location.host}/game/${encodeURIComponent(lobbyId)}?token=${encodeURIComponent(sessionToken)}`;

    const ws = new WebSocket(serverUrl);
    ws.onopen = () => console.log("W in the chat");
    ws.onclose = () => console.log("L in the chat");

    ws.onmessage = messagehandler;

    socket.set(ws);
}

export function disconnect() {
    socket.update((ws) => {
        if (ws) ws.close();
        return null;
    });
}

function messagehandler(event: MessageEvent) {
    let json = JSON.parse(event.data) as Pluh;

    console.log("Wiadomość z serwera gier:", json);
    console.log("aok:", event.type);
    console.log("username: ", json.username);
}