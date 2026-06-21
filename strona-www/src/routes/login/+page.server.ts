import { error, type Actions } from "@sveltejs/kit";
import { passCheck } from "$lib/server/login";
import { fail } from "@sveltejs/kit";
import { createSessionForId } from "$lib/server/auth";

const maxAge = 60*60*24*7; // 7 dni

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

        cookies.set("session", sessionId, {
            path: '/',
            httpOnly: true,
            secure: true,
            sameSite: "lax",
            maxAge
        });

        return {
            success: true
        }
    }
}