<script lang="ts">
    let { data } = $props();
</script>

<h1>{data.response.name}</h1><br>
{data.response.password}

<h1>Welcome to SvelteKit</h1>
<p>Visit <a href="https://svelte.dev/docs/kit">svelte.dev/docs/kit</a> to read the documentation</p>
