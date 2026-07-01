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
    try {
        await pool.query(insertUserToLobbyQuery, [userId, lobbyId]);
    } catch (err: any) {
        if (err.code === '23505') {
            throw new Error("już jesteś w tym lobby");
        }
        throw err;
    }
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

export async function isPasswordCorrect(lobbyId: number, password: string): Promise<boolean> {
    const query = `
        select password from rooms where id = $1 and password = $2;
    `;
    const res = await pool.query(query, [lobbyId, password]);
    
    if (res.rows.length === 0) return false;

    const resPassword = res.rows[0].password;
    if(resPassword === null) return true;

    return resPassword === password;
}

// zapomniałem że ta funkcja już istnieje
// export async function JoinUserToLobby(userId: number, lobbyId: number) {
//     const joinQuery = `
//         insert into room_players (userid, room_id) values ($1, $2);
//     `;

//     await pool.query(joinQuery, [userId, lobbyId]);
// }

export async function DoesLobbyHavePassword(lobbyId: number): Promise<boolean> {
    const query = `
        select password from rooms where id = $1;
    `;

    let res = await pool.query(query, [lobbyId]);

    if( res.rows.length === 0 ) return true

    return !!res.rows[0].password;
}

export async function isUserInLobby(userId: number, lobbyId: number): Promise<boolean> {
    const query = `
        select 1 from room_players where userid = $1 and room_id = $2;
    `;

    let res = await pool.query(query, [userId, lobbyId]);

    if(res.rows.length > 0) return true;

    return false;
}

export async function usersInLobby(lobbyId: number): Promise<string[]> {
    const query = `
        SELECT u.name 
        FROM room_players rp
        JOIN users u ON rp.userid = u.id
        WHERE rp.room_id = $1
        ORDER BY rp.joined_at ASC;
    `;

    try {
        const res = await pool.query(query, [lobbyId]);
        
        return res.rows.map(row => row.name);
    } catch (err) {
        console.error("Błąd podczas pobierania graczy z lobby:", err);
        throw err;
    }
}

export async function lobbyInformation(lobbyId: number): Promise<{lobbyId: number, name: string, users: string[]}> {
    try {
        // 1. Pobieramy nazwę pokoju z tabeli rooms
        const roomRes = await pool.query("SELECT name FROM rooms WHERE id = $1;", [lobbyId]);
        
        if (roomRes.rows.length === 0) {
            throw new Error(`Lobby o ID ${lobbyId} nie istnieje.`);
        }

        const roomName = roomRes.rows[0].name;

        const users = await usersInLobby(lobbyId);

        return {
            lobbyId,
            name: roomName,
            users
        };
    } catch (err) {
        console.error(`Błąd podczas pobierania pełnych informacji o lobby ${lobbyId}:`, err);
        throw err;
    }
}