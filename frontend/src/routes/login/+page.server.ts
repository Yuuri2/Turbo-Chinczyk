import { error, type Actions, redirect } from "@sveltejs/kit";
import { passCheck } from "$lib/server/login";
import { fail } from "@sveltejs/kit";
import { createSessionForId, setCookieSession } from "$lib/server/auth";
import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = ({ locals }) => {
    if(locals.user) throw redirect(303, "/lobby/lobbies");
}

export const actions: Actions = {
    default: async ({ request, cookies }) => {
        const formData = await request.formData();
        const login = formData.get("login")?.toString();
        const password = formData.get("password")?.toString();

        if(!login || !password) {
            return fail(400, {
                error: true,
                message: "podaj login i hasło",
                login
            });
        }

        let userId = await passCheck(login, password);

        if (!userId){
            return fail(400, {
                error: true,
                message: "zły login lub hasło",
                login
            });
        }

        const sessionId = await createSessionForId(userId);

        setCookieSession(cookies, sessionId);

        throw redirect(303, "/lobby/lobbies");
    }
}