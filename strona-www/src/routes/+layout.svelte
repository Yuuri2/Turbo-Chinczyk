<script lang="ts">
	import favicon from '$lib/assets/favicon.svg';
	import LogountButton from '$lib/components/LogountButton.svelte';

	let { data, children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div id="navbar">
	<h4>chińczyk</h4>
	<div>
		{#if data.user}
			<p>zalogowano jako: <b>{data.user.name}</b></p><LogountButton></LogountButton>
		{:else}
			<a href="/login" id="login">login</a>
			<br>
			<a href="/register">register</a>
		{/if}
	</div>
</div>

{@render children()}

<style>
	#navbar {
		height: 4rem;

		border-bottom-style: solid;
		border-bottom-width: 1px;

		display: flex;

		align-items: center;

		justify-content: space-between;

		padding: 0.5rem, 1rem;
	}
	h4{
		margin: 0;
	}
	a#login {
		float: right;
	}
</style>