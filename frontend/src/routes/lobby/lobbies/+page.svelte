<script lang="ts">
    import { enhance } from "$app/forms";
    import { goto } from "$app/navigation";
    let { data, form } = $props();

    function join(lobbyId: number){
        goto(`/lobby/${lobbyId}`);
    }
</script>
<h1>lobbies</h1>
<div>
    <form action="?/createLobby" method="POST" use:enhance>
        <input type="text" name="name" id="formName">
        <button type="submit">stwórz</button>
    </form>
    {#if form?.error}
        <h3>{form.message}</h3>
    {/if}
</div>
<br>
<div>
    {#each data.lobbies as lobby}
        <div class="lobby">
            {lobby.name} 
            {lobby.id} 
            <button on:click={() => join(lobby.id)}>join</button>
        </div>
    {/each}
</div>
<style>
    .lobby {
        border: 1px solid;
    }
</style>