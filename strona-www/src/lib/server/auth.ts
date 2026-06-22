import { pool } from "$lib/server/pool";
import type { Cookies } from "@sveltejs/kit";

const createSessionQuery = `
    insert into tokens (userid, expiry)
    values ($1, NOW() + interval '7 days')
    returning sessionid;
`;

const validateSessionQuery = `
    select u.id, u.name
    from tokens t
    join users u on t.userid = u.id
    where t.sessionid = $1 and t.expiry > NOW()
`;

const maxCookieAge = 60*60*24*7; // 7 dni

interface SessionUser {
    id: number;
    name: string;
}

export async function createSessionForId(userId: number): Promise<string> {
    let res = await pool.query(createSessionQuery, [userId]);

    return res.rows[0].sessionid;
}

export async function validateSession(sessionId: string | undefined): Promise<SessionUser | null> {
    if(!sessionId) return null;

    const res = await pool.query(validateSessionQuery, [sessionId]);

    if(res.rows.length > 0) {
        return {
            id: res.rows[0].id,
            name: res.rows[0].name
        }
    }

    return null;
}

export async function invalidateSession(sessionId: string) {
    await pool.query("delete from tokens where sessionid = $1", [sessionId]);
}

export function setCookieSession(cookies: Cookies, sessionId: string) {
    cookies.set("session", sessionId, {
            path: '/',
            httpOnly: true,
            secure: true,
            sameSite: "lax",
            maxAge: maxCookieAge
    });
}