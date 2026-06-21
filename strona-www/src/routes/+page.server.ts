import { type ServerLoad } from "@sveltejs/kit";
import { pool } from "$lib/server/pool";
import { env } from "$env/dynamic/private";

export const load: ServerLoad = async () => {
    let res = await pool.query("SELECT * FROM users ORDER BY id ASC");

    return {
        response: res.rows[0]
    }
}