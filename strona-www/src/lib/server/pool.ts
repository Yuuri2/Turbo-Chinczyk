import { Pool } from 'pg';
import { env } from '$env/dynamic/private';

export const pool = new Pool({
    host: env.PGHOST,
    user: env.PGUSER,
    database: env.PGDATABASE,
    port: Number(env.PGPORT),
    password: String(env.PGPASSWORD)
});