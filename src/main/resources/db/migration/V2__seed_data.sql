-- Optional demo data for local development (bcrypt-compatible hash via pgcrypto)

INSERT INTO organizations (id, name, slug, plan)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'Demo Organization',
    'demo-org',
    'FREE'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, org_id, email, password_hash, full_name, role, is_active)
VALUES (
    'b0000000-0000-4000-8000-000000000001',
    'a0000000-0000-4000-8000-000000000001',
    'admin@demo.com',
    crypt('Password123!', gen_salt('bf', 10)),
    'Demo Admin',
    'ADMIN',
    TRUE
)
ON CONFLICT (id) DO NOTHING;
