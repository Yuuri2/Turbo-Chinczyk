<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { connectToLobby } from "$lib/game/websocket";
  import { page } from "$app/stores";

    const { lobbyId } = $page.params;
    if(!lobbyId) goto("/lobby/lobbies");
    const Id = Number(lobbyId);


    onMount(() => {
        connectToLobby(Id);
    });
</script>

<h1>LOBBY</h1>
<h2>{lobbyId}</h2>
