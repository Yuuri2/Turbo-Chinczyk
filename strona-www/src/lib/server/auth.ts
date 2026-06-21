import { pool } from "$lib/server/pool";

const createSessionQuery = `
    insert into tokens (userid, expiry)
    values ($1, NOW() + interval '7 days')
    returning sessionid;
`;

export async function createSessionForId(userId: number): Promise<string> {
    let res = await pool.query(createSessionQuery, [userId]);

    return res.rows[0].sessionid;
}