import { type ServerLoad, type Actions, fail, redirect, isRedirect } from "@sveltejs/kit";
import { createLobby, currentWaitingLobbies } from "$lib/server/lobby";
import { requireUser } from "$lib/server/auth";


export const actions: Actions = {
    createLobby: async ({ request, locals }) => {
        const formData = await request.formData();
        const name = formData.get("name")?.toString().trim() || null;
        const password = formData.get("password")?.toString().trim() || null;
        const user = requireUser(locals);

        if(!name) {
            return fail(400, {
                error: true,
                message: "nie podałeś nazwy"
            });
        }

        try {
            const lobbyId = await createLobby(name, user.id, password);
            
            throw redirect(303, `/lobby/${lobbyId}`);
        } catch(err) {
            if(isRedirect(err)){
                throw err;
            }

            console.error(err);
            return fail(500, {
                error: true,
                message: "server napotkał błąd"
            });
        }
    }
}

export const load: ServerLoad = async ({}) => {
    return{
        lobbies: await currentWaitingLobbies()
    }
}