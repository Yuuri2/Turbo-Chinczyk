import { pool } from "$lib/server/pool";

const createLobbyQuery = `
    insert into rooms (name, password, host_id)
    values ($1, $2, $3)
    returning id;
`;

const insertUserToLobbyQuery = `
    insert into room_players (userid, room_id)
    values ($1, $2);
`;


export async function createLobby(name: string, hostId: number, password: string | null): Promise<number> {
    const client = await pool.connect();

    try {
        await client.query("BEGIN"); // z tego co wiem to to jest początek "tranzakcji" jak się nie uda coś po drodze to można to wszystko cofnąć

        const res = await client.query(createLobbyQuery, [name, password, hostId]);
        const lobbyId = res.rows[0].id;

        await client.query(insertUserToLobbyQuery, [hostId, lobbyId]);

        await client.query('COMMIT');

        return lobbyId;
    } catch(err) {
        await client.query('ROLLBACK');
        throw err;
    } finally {
        client.release();
    }
}

export async function addUserToLobby(userId: number, lobbyId: number) {
    pool.query(insertUserToLobbyQuery, [userId, lobbyId]);
}

export async function currentWaitingLobbies() {
    const waitingLobbiesQuery = `
        SELECT 
        r.id, 
        r.name, 
        r.host_id,
        u.name AS host_name,
        (r.password IS NOT NULL) AS is_private,
        COUNT(rp.userid) AS current_players
        FROM rooms r
        JOIN users u ON r.host_id = u.id
        LEFT JOIN room_players rp ON r.id = rp.room_id
        WHERE r.status = 'waiting'
        GROUP BY r.id, r.name, r.host_id, u.name;
    `;

    const res = await pool.query(waitingLobbiesQuery);

    return res.rows as {id: number, name: string, host_id: number, host_name: string, is_private: boolean, current_players: number}[]
}