import { redirect, type Actions } from '@sveltejs/kit';

export const actions: Actions = {
    default: async ({ cookies, locals }) => {
        cookies.delete("session", {path: "/"});

        locals.user = null;

        throw redirect(303, "/");
    }
}