import { writable } from "svelte/store";

export const socket = writable<WebSocket | null>(null);

export function connectToLobby(lobbyId: number) {
    const ws = new WebSocket(`ws://localhost:8080/lobby/${lobbyId}`);
    ws.onopen = () => console.log("W in the chat");
    ws.onclose = () => console.log("L in the chat");

    socket.set(ws);
}

export function disconnect() {
    socket.update((ws) => {
        if (ws) ws.close();
        return null;
    });
}