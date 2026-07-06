<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { connectToLobby } from "$lib/game/websocket";

    let { data } = $props();

    onMount(() => {
        connectToLobby(data.LobbyInfo.lobbyId);
    });
</script>

<h1>LOBBY: {data.LobbyInfo.name}</h1>
<div>
    <h2>gracze</h2>
    <ol>
        {#each data.LobbyInfo.users as user}
            <li>{user}</li>
        {/each}
    </ol>
</div>
