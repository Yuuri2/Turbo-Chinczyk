import { requireUser } from "$lib/server/auth";
import { getLobby, joinLobby, LobbyApiError } from "$lib/server/lobby";
import { fail, redirect, type Actions, type ServerLoad } from "@sveltejs/kit";

export const load: ServerLoad = async ({ params, locals, cookies, fetch }) => {
    const lobbyId = Number(params.lobbyId);
    const user = requireUser(locals);

    if (Number.isNaN(lobbyId) || lobbyId <= 0){
        throw redirect(307, "/lobby/lobbies");
    }

    let lobby;
    try {
        lobby = await getLobby(fetch, cookies.get("session"), lobbyId);
    } catch (error) {
        if (error instanceof LobbyApiError && error.status === 404) {
            throw redirect(307, "/lobby/lobbies");
        }
        throw error;
    }

    if (!lobby.players.some((player) => player.userId === user.id)
        && lobby.status !== "WAITING") {
        throw redirect(307, "/lobby/lobbies");
    }

    return {
        LobbyInfo: lobby,
        session: cookies.get("session"),
        isMember: lobby.players.some((player) => player.userId === user.id),
        user: {
            id: user.id,
            name: user.name
        }
    }
}

export const actions: Actions = {
    join: async ({ request, params, locals, cookies, fetch }) => {
        const formData = await request.formData();
        const password = formData.get("password")?.toString().trim() || null;
        const user = requireUser(locals);

        const lobbyId = Number(params.lobbyId);
        if(Number.isNaN(lobbyId) || lobbyId <= 0){
            throw redirect(307, "/lobby/lobbies");
        }
        
        try {
            await joinLobby(
                fetch,
                cookies.get("session"),
                lobbyId,
                password);
        } catch (error) {
            if (error instanceof LobbyApiError) {
                if (error.status === 403) {
                    return fail(400, {
                        success: false,
                        message: "złe hasło"
                    });
                }
                if (error.status === 404) {
                    throw redirect(307, "/lobby/lobbies");
                }
                if (error.status === 409) {
                    return fail(409, {
                        success: false,
                        message: "nie można dołączyć do lobby"
                    });
                }
            }
            throw error;
        }
        
        throw redirect(303, `/lobby/${lobbyId}`);
    }
}