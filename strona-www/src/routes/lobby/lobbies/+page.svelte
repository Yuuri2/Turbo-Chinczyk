<script lang="ts">
    import { enhance } from "$app/forms";
    let { data, form } = $props();
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
<div>
    {#each data.lobbies as lobby}
        <div>
            <p>{lobby.name}</p>
            <p>{lobby.id}</p>
        </div>
    {/each}
</div>