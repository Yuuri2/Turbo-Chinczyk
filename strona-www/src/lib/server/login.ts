import { pool } from "$lib/server/pool";

const check_user_querry = `
    select id from users where name = $1 and password = crypt($2, password);
`;

export async function passCheck(login: string, password: string): Promise<number | null> {
    let res = await pool.query(check_user_querry, [login, password]);

    if (res.rows.length === 0)
        return null;
    
    return res.rows[0].id;
}