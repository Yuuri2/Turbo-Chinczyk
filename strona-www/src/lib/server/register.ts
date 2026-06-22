import { pool } from "$lib/server/pool";

const createAccountQuery = `
    insert into users (name, password)
    values ($1, $2)
    returning id;
`;

export async function createAccount(login: string, password: string): Promise<number | null> {
    try {
        const res = await pool.query(createAccountQuery, [login, password]);

        return res.rows[0].id;
    } catch (err) {
        return null;
    }
}