<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { connectToLobby } from "$lib/game/websocket";

    let { data } = $props();

    onMount(() => {
        connectToLobby(data.lobbyId);
    });
</script>

<h1>LOBBY</h1>
<h2>{data.lobbyId}</h2>
<h3>{data.git}</h3>
