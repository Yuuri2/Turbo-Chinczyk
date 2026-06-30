import { invalidateSession } from '$lib/server/auth';
import { redirect, type Actions } from '@sveltejs/kit';

export const actions: Actions = {
    default: async ({ cookies, locals }) => {
        const sessionId = cookies.get("session");

        cookies.delete("session", {path: "/"});


        if(!sessionId) {
            throw redirect(303, "/");    
        }
        
        invalidateSession(sessionId);

        locals.user = null;

        throw redirect(303, "/");
    }
}