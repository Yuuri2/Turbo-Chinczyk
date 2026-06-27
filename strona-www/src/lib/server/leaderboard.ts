import { pool } from '$lib/server/pool';

export type LeaderboardRow = {
	place: number;
	player_id: number;
	name: string;
	wins: number;
	losses: number;
};

export async function getTopPlayers(limit = 10): Promise<LeaderboardRow[]> {
	const safeLimit = Number.isFinite(limit) && limit > 0 ? Math.floor(limit) : 10;

	const { rows } = await pool.query<LeaderboardRow>(
		`SELECT * FROM public.get_top_players($1)`,
		[safeLimit]
	);

	return rows;
}