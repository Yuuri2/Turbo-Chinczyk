<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import { enhance } from "$app/forms";
    import {
        connectToLobby,
        lobby,
        lastCommandError,
        setReady,
        startGame,
        disconnect
    } from "$lib/game/websocket";

    let { data } = $props();

    onMount(() => {
        connectToLobby(data.LobbyInfo.lobbyId);
    });

    onDestroy(disconnect);

    function currentPlayer() {
            return $lobby?.players.find((player) => player.userId === data.user.id);
    }
</script>

<h1>LOBBY: {$lobby?.name ?? data.LobbyInfo.name}</h1>
{#if $lastCommandError}
    <p>{$lastCommandError}</p>
{/if}
<div>
    <h2>gracze</h2>
    <ol>
            {#each ($lobby?.players ?? data.LobbyInfo.players) as player}
                <li>{player.username} {player.online ? "(online)" : "(offline)"}</li>
            {/each}
    </ol>
</div>
{#if currentPlayer()}
    <button onclick={() => setReady(!currentPlayer()?.ready)}>
            {currentPlayer()?.ready ? "not ready" : "ready"}
    </button>
{/if}
{#if data.LobbyInfo.hostId === data.user.id}
    <button onclick={() => startGame()}>start game</button>
{/if}
<form action="?/leave" method="POST" use:enhance>
    <button type="submit">leave lobby</button>
</form>
{#if data.LobbyInfo.hostId === data.user.id}
    <form action="?/delete" method="POST" use:enhance>
        <button type="submit">delete lobby</button>
    </form>
{/if}
