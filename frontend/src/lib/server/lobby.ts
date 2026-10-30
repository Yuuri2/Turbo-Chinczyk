import { env } from "$env/dynamic/private";

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
    status: "WAITING" | "PLAYING" | "FINISHED";
    maxPlayers: number;
    passwordProtected: boolean;
    players: LobbyPlayer[];
}

const backendUrl = env.BACKEND_URL ?? "http://backend:8080";

async function backendRequest<T>(
    fetchFunction: typeof fetch,
    path: string,
    session: string | undefined,
    init: RequestInit = {}
): Promise<T> {
    const headers = new Headers(init.headers);
    if (session) {
        headers.set("Cookie", `session=${session}`);
    }

    const response = await fetchFunction(`${backendUrl}${path}`, {
        ...init,
        headers
    });

    if (!response.ok) {
        throw new LobbyApiError(response.status, await response.text());
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

export function listLobbies(
    fetchFunction: typeof fetch,
    session: string | undefined
): Promise<LobbySnapshot[]> {
    return backendRequest<LobbySnapshot[]>(
        fetchFunction,
        "/api/lobbies",
        session
    );
}

export function createLobby(
    fetchFunction: typeof fetch,
    session: string | undefined,
    name: string,
    password: string | null
): Promise<LobbySnapshot> {
    return backendRequest<LobbySnapshot>(
        fetchFunction,
        "/api/lobbies",
        session,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name,
                password,
                maxPlayers: 4
            })
        }
    );
}

export function getLobby(
    fetchFunction: typeof fetch,
    session: string | undefined,
    lobbyId: number
): Promise<LobbySnapshot> {
    return backendRequest<LobbySnapshot>(
        fetchFunction,
        `/api/lobbies/${lobbyId}`,
        session
    );
}

export function joinLobby(
    fetchFunction: typeof fetch,
    session: string | undefined,
    lobbyId: number,
    password: string | null
): Promise<LobbySnapshot> {
    return backendRequest<LobbySnapshot>(
        fetchFunction,
        `/api/lobbies/${lobbyId}/join`,
        session,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ password })
        }
    );
}

export function leaveLobby(
    fetchFunction: typeof fetch,
    session: string | undefined,
    lobbyId: number
): Promise<void> {
    return backendRequest<void>(
        fetchFunction,
        `/api/lobbies/${lobbyId}/leave`,
        session,
        { method: "POST" }
    );
}

export function deleteLobby(
    fetchFunction: typeof fetch,
    session: string | undefined,
    lobbyId: number
): Promise<void> {
    return backendRequest<void>(
        fetchFunction,
        `/api/lobbies/${lobbyId}`,
        session,
        { method: "DELETE" }
    );
}

export class LobbyApiError extends Error {
    constructor(
        public readonly status: number,
        details: string
    ) {
        super(details || `Lobby API request failed with status ${status}`);
    }
}
