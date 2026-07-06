import { addUserToLobby, DoesLobbyHavePassword, isPasswordCorrect, isUserInLobby, lobbyInformation, usersInLobby } from "$lib/server/lobby";
import { fail, redirect, type Actions, type ServerLoad } from "@sveltejs/kit";

export const load: ServerLoad = async ({ params, locals }) => {
    const lobbyId = Number(params.lobbyId);

    if (Number.isNaN(lobbyId) || lobbyId <= 0){
        throw redirect(307, "/lobby/lobbies");
    }

    if(!await isUserInLobby(locals.user?.id!, lobbyId)){
        throw redirect(307, "/lobby/lobbies");
    }

    const users = await usersInLobby(lobbyId);

    return {
        LobbyInfo: await lobbyInformation(lobbyId)
    }
}

export const actions: Actions = {
    join: async ({ request, params, locals }) => {
        const formData = await request.formData();
        const password = formData.get("password")?.toString().trim() || null;

        const lobbyId = Number(params.lobbyId);
        if(Number.isNaN(lobbyId) || lobbyId <= 0){
            throw redirect(307, "/lobby/lobbies");
        }
        
        if(await DoesLobbyHavePassword(lobbyId)){
            if(!password) {
                return fail(400, {success: false,  message: "podaj hasło"});
            }
            
            if(!await isPasswordCorrect(lobbyId, password)){
                return fail(400, {success: false,  message: "złe hasło"});
            }
        }
        
        if(!await isUserInLobby(locals.user?.id!, lobbyId)){
            console.log("nie jest w lobby")
            await addUserToLobby(locals.user?.id!, lobbyId);
        }
        
        throw redirect(303, `/lobby/${lobbyId}`);
    }
}