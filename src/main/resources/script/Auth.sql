CREATE SCHEMA auth;
--=============================== 1. auth.users================================
CREATE TABLE auth.users (
    id SERIAL PRIMARY KEY ,
    public_id UUID NOT NULL UNIQUE ,
    username VARCHAR(50) NOT NULL UNIQUE ,
    email VARCHAR(100) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL ,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
--=============================== 2. auth.roles================================
CREATE TABLE auth.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);
--=============================== 3. auth.permissions==========================
CREATE TABLE auth.permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);
--=============================== 4. auth.user_roles================================
CREATE TABLE auth.user_roles (
    user_id INT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES auth.roles(id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id) -- A user can't have the same role twice
);
--=============================== 5. auth.role_permissions================================
CREATE TABLE auth.role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES auth.roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES auth.permissions(permission_id) ON DELETE CASCADE
);
--=============================== 6. auth.otps================================
CREATE TABLE auth.otps (
    id BIGSERIAL PRIMARY KEY ,
    email VARCHAR(100) NOT NULL ,
    otp_code VARCHAR(10) NOT NULL ,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
DELETE FROM auth.otps WHERE id = 3;
DELETE FROM auth.users WHERE id = 4;
--=============================INSERT======================================
INSERT INTO auth.roles (name, description, is_active, created_by, created_at)
VALUES
    ('SUPER_ADMIN', 'Platform administrator with full access', TRUE, 'system', NOW()),
    ('USER', 'Regular buyer/seller with limited capabilities', TRUE, 'system', NOW()),
    ('COMPANY_OWNER', 'Owns and manages a company account with plan subscriptions', TRUE, 'system', NOW()),
    ('COMPANY_EMPLOYEE', 'Employee working under a company account', TRUE, 'system', NOW());

INSERT INTO auth.permissions (permission_key, description) VALUES
    ('platform:users:create', 'Can create new platform users'),
    ('platform:users:read', 'Can read all platform user data'),
    ('platform:users:update', 'Can update all platform user data'),
    ('platform:users:delete', 'Can delete platform users');

-- 1. SUPER_ADMIN gets ALL permissions
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM auth.roles r
         CROSS JOIN auth.permissions p
WHERE r.name = 'SUPER_ADMIN';

-- 2. COMPANY_OWNER gets create, read, update permissions only
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM auth.roles r
         JOIN auth.permissions p ON p.permission_key IN (
                                                         'platform:users:create',
                                                         'platform:users:read',
                                                         'platform:users:update'
    )
WHERE r.name = 'COMPANY_OWNER';

-- 3. COMPANY_EMPLOYEE gets read permission only
INSERT INTO auth.role_permissions (role_id, permission_id)
VALUES (3, 5);
SELECT r.id, p.permission_id
FROM auth.roles r
         JOIN auth.permissions p ON p.permission_key = 'platform:users:read'
WHERE r.name = 'COMPANY_EMPLOYEE';

-- 4. USER role may not get any platform permissions (optional)
-- INSERT INTO auth.role_permissions (role_id, permission_id)
-- SELECT r.id, p.permission_id
-- FROM auth.roles r
-- JOIN auth.permissions p ON p.permission_key IN ( ... )
-- WHERE r.name = 'USER';


DELETE FROM auth.users where id = 1;
ALTER SEQUENCE auth.users_id_seq RESTART WITH 1;
DELETE FROM auth.user_roles where user_id = 1;
SELECT setval(pg_get_serial_sequence('user_roles', 'user_id'), (SELECT MAX(user_id) FROM auth.user_roles));
UPDATE auth.user_roles SET role_id = 3 WHERE user_id = 1;

-- For the user_roles table
ALTER TABLE auth.user_roles ALTER COLUMN user_id TYPE BIGINT;


-- For the roles table
ALTER TABLE auth.roles ALTER COLUMN created_by TYPE BIGINT;
ALTER TABLE auth.users ALTER COLUMN id TYPE BIGINT;

-- 1️⃣ Change the column type to BIGINT
ALTER TABLE auth.users
    ALTER COLUMN id TYPE BIGINT;

-- 2️⃣ Create a new sequence for auto-increment (if it doesn't exist)
CREATE SEQUENCE IF NOT EXISTS auth.users_id_seq
    START WITH 1
    INCREMENT BY 1
    OWNED BY auth.users.id;

-- 3️⃣ Set the default value to use the sequence
ALTER TABLE auth.users
    ALTER COLUMN id SET DEFAULT nextval('auth.users_id_seq');

-- 4️⃣ (Optional) Set the sequence to continue after the current max id
SELECT setval('auth.users_id_seq', COALESCE((SELECT MAX(id) FROM auth.users), 1), TRUE);

SELECT r.* FROM auth.roles r INNER JOIN auth.user_roles ur ON r.id = ur.role_id WHERE ur.user_id = 1;
SELECT * FROM auth.users WHERE email = 'lounseven59@gmail.com';

SELECT * FROM auth.permissions;

-->>>>>>>>>>>>>>>
-- 1. Update existing keys to match the Controller format
-- Changing 'platform:users:create' -> 'user:create'
UPDATE auth.permissions
SET permission_key = 'user:create', description = 'Can create new users'
WHERE permission_key = 'platform:users:create';

-- Changing 'platform:users:read' -> 'user:read'
-- This covers ID, Username, and PublicID endpoints
UPDATE auth.permissions
SET permission_key = 'user:read', description = 'Can read public user data'
WHERE permission_key = 'platform:users:read';

-- Changing 'platform:users:update' -> 'user:update'
UPDATE auth.permissions
SET permission_key = 'user:update', description = 'Can update user data'
WHERE permission_key = 'platform:users:update';

-- Changing 'platform:users:delete' -> 'user:delete'
UPDATE auth.permissions
SET permission_key = 'user:delete', description = 'Can delete users'
WHERE permission_key = 'platform:users:delete';

-- 2. Insert the MISSING permissions needed for your Controller
-- Your controller checks for 'user:list' and 'user:read-email', which don't exist yet.

INSERT INTO auth.permissions (permission_key, description)
VALUES
    ('user:list', 'Can list all users and filter by status'),
    ('user:read-email', 'Can view sensitive email addresses')
ON CONFLICT (permission_key) DO NOTHING;

-- Clear existing links for a clean slate (Optional, remove if you want to keep old data)
-- DELETE FROM auth.role_permissions;

