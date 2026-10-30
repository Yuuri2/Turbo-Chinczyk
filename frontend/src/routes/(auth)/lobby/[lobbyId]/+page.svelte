<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import { enhance } from "$app/forms";
    import {
        connectToLobby,
        lobby,
        lastCommandError,
        lastCommandAcknowledgement,
        connectionStatus,
        pendingCommands,
        setReady,
        startGame,
        disconnect
    } from "$lib/game/websocket";

    let { data } = $props();
    let leaving = $state(false);
    let deleting = $state(false);

    onMount(() => {
        connectToLobby(data.LobbyInfo.lobbyId);
    });

    onDestroy(disconnect);

    function currentPlayer() {
        return $lobby?.players.find((player) => player.userId === data.user.id);
    }

    function hasPendingCommand() {
        return [...$pendingCommands].length > 0;
    }

    function confirmLeave(event: SubmitEvent) {
        if (leaving || !window.confirm("Leave this lobby?")) {
            event.preventDefault();
            return;
        }
        leaving = true;
        disconnect();
    }

    function confirmDelete(event: SubmitEvent) {
        if (deleting || !window.confirm("Delete this lobby?")) {
            event.preventDefault();
            return;
        }
        deleting = true;
        disconnect();
    }
</script>

<h1>LOBBY: {$lobby?.name ?? data.LobbyInfo.name}</h1>
<p>Connection: {$connectionStatus}</p>
{#if $lastCommandError}
    <p>{$lastCommandError}</p>
{/if}
{#if $lastCommandAcknowledgement}
    <p>Command accepted: {$lastCommandAcknowledgement.command}</p>
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
    <button
        onclick={() => setReady(!currentPlayer()?.ready)}
        disabled={$connectionStatus !== "connected" || hasPendingCommand()}
    >
            {currentPlayer()?.ready ? "not ready" : "ready"}
    </button>
{/if}
{#if data.LobbyInfo.hostId === data.user.id}
    <button
        onclick={() => startGame()}
        disabled={$connectionStatus !== "connected" || hasPendingCommand()}
    >start game</button>
{/if}
<form action="?/leave" method="POST" use:enhance onsubmit={confirmLeave}>
    <button type="submit" disabled={leaving}>{leaving ? "leaving..." : "leave lobby"}</button>
</form>
{#if data.LobbyInfo.hostId === data.user.id}
    <form action="?/delete" method="POST" use:enhance onsubmit={confirmDelete}>
        <button type="submit" disabled={deleting}>{deleting ? "deleting..." : "delete lobby"}</button>
    </form>
{/if}
