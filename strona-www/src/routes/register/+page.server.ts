import { type Actions, error, fail } from "@sveltejs/kit";
import { createAccount } from "$lib/server/register";
import { createSessionForId, setCookieSession } from "$lib/server/auth";

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

        const userId = await createAccount(login, password);

        if(!userId){
            return fail(400, {
                error: true,
                message: "login już zajęty",
                login: ""
            });
        }

        const sessionId = await createSessionForId(userId);
        setCookieSession(cookies, sessionId);

        return {
            success: true
        }
    }
}