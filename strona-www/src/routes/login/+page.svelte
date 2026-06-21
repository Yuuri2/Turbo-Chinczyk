<script lang="ts">
    import { enhance } from "$app/forms";

    let { data, form } = $props();
</script>
<h1>login</h1>
<form action="/login" method="POST" use:enhance>
    <div style="width: min-content;">
        <input type="text" name="login" id="login" placeholder="login" value={form?.login ?? ""} required>
        <br>
        <input type="password" name="password" id="password" placeholder="hasło" required>
        <br>
        <button type="submit" id="submit">login</button>
    </div>
</form>
{#if form?.success}
    <h2><b>zalogowano pomyślnie</b></h2>
{/if}
{#if form?.error}
    <h2><b>{form?.message}</b></h2>
{/if}

<style>
    button#submit {
        width: 100%;
    }
</style>