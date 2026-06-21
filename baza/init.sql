-- 1. Włączenie rozszerzenia pgcrypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. Tworzenie tabeli użytkowników
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Długość 255 idealna dla hashu BCrypt
    wins INT DEFAULT 0 NOT NULL,
    losses INT DEFAULT 0 NOT NULL
);

-- 3. Funkcja haszująca hasło przed zapisem
CREATE OR REPLACE FUNCTION hash_user_password()
RETURNS TRIGGER AS $$
BEGIN
    -- Hashujemy hasło tylko jeśli jest wprowadzane (INSERT) lub zostało zmienione (UPDATE)
    IF TG_OP = 'INSERT' OR NEW.password IS DISTINCT FROM OLD.password THEN
        NEW.password := crypt(NEW.password, gen_salt('bf', 10)); -- 'bf' to Blowfish/BCrypt, 10 to poziom trudności (work factor)
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Powiązanie funkcji z tabelą za pomocą Triggera
CREATE TRIGGER trigger_hash_password
BEFORE INSERT OR UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION hash_user_password();

-- 5. Tworzenie tabeli tokenów (z użyciem UUID)
CREATE TABLE tokens (
    sessionid UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- pgcrypto automatycznie wygeneruje losowy UUID
    userid INT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expiry TIMESTAMP NOT NULL,
    
    -- Klucz obcy z automatycznym usuwaniem sesji po usunięciu użytkownika
    CONSTRAINT fk_user FOREIGN KEY (userid) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);