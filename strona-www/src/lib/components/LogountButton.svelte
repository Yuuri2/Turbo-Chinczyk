<script lang="ts">
    import { enhance } from "$app/forms";
</script>
<form action="/logout" method="POST" use:enhance>
    <button type="submit">wyloguj</button>
</form>
<style>
    button {
        padding: 0;
        margin: 0;
    }
</style>