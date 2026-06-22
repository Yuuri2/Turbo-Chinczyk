import { type Handle } from "@sveltejs/kit";
import { validateSession } from "$lib/server/auth";

export const handle: Handle = async ({ event, resolve}) => {
    const sessionId = event.cookies.get('session');

    event.locals.user = await validateSession(sessionId);
    
    return resolve(event);
}