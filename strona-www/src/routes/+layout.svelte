<script lang="ts">
	import favicon from '$lib/assets/favicon.svg';

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div id="navbar">
	<h4>chińczyk</h4>
	<a href="/login" id="login">login</a>
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