<script lang="ts">
	type RankingRow = {
		place: number;
		player_id: number;
		name: string;
		wins: number;
		losses: number;
	};

	const { data } = $props<{
		data: { ranking: RankingRow[] };
	}>();
</script>

<h1>Leaderboard TOP 10</h1>

{#if data.ranking.length === 0}
	<p>Brak graczy w rankingu.</p>
{:else}
	<table>
		<thead>
			<tr>
				<th>Miejsce</th>
				<th>Gracz</th>
				<th>Wygrane</th>
				<th>Przegrane</th>
			</tr>
		</thead>
		<tbody>
			{#each data.ranking as player}
				<tr>
					<td>{player.place}</td>
					<td>{player.name}</td>
					<td>{player.wins}</td>
					<td>{player.losses}</td>
				</tr>
			{/each}
		</tbody>
	</table>
{/if}