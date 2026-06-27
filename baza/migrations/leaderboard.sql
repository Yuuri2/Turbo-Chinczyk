CREATE OR REPLACE FUNCTION public.get_top_players(limit_count integer DEFAULT 10)
RETURNS TABLE (
  place integer,
  player_id integer,
  name varchar(100),
  wins integer,
  losses integer
)
LANGUAGE sql
STABLE
AS $$
  SELECT
    ROW_NUMBER() OVER (ORDER BY u.wins DESC, u.losses ASC, u.name ASC)::int AS place,
    u.id AS player_id,
    u.name,
    u.wins,
    u.losses
  FROM public.users u
  ORDER BY u.wins DESC, u.losses ASC, u.name ASC
  LIMIT GREATEST(limit_count, 1);
$$;