import { writable } from "svelte/store";

export const socket = writable<WebSocket | null>(null);

export function connectToLobby(lobbyId: number, sessionToken: string) {
    const serverUrl = `ws://localhost:8080/game?lobbyId=${encodeURIComponent(lobbyId)}&token=${encodeURIComponent(sessionToken)}`;


    const ws = new WebSocket(serverUrl);
    ws.onopen = () => console.log("W in the chat");
    ws.onclose = () => console.log("L in the chat");

    ws.onmessage = (event) => {
        console.log("Wiadomość z serwera gier:", event.data);
    };

    socket.set(ws);
}

export function disconnect() {
    socket.update((ws) => {
        if (ws) ws.close();
        return null;
    });
}