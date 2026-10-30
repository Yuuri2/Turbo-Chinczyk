import { type ServerLoad, type Actions, fail, redirect, isRedirect } from "@sveltejs/kit";
import { createLobby, listLobbies } from "$lib/server/lobby";
import { requireUser } from "$lib/server/auth";


export const actions: Actions = {
    createLobby: async ({ request, locals, cookies, fetch }) => {
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
            const lobby = await createLobby(
                fetch,
                cookies.get("session"),
                name,
                password);
            
            throw redirect(303, `/lobby/${lobby.lobbyId}`);
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

export const load: ServerLoad = async ({ fetch, cookies }) => {
    return{
        lobbies: await listLobbies(fetch, cookies.get("session"))
    }
}