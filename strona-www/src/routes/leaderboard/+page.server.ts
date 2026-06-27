import type { PageServerLoad } from './$types';
import { getTopPlayers } from '$lib/server/leaderboard';

export const load: PageServerLoad = async () => {
	const ranking = await getTopPlayers(10);

	return {
		ranking
	};
};