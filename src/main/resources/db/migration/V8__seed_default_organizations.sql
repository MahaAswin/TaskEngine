INSERT INTO organizations (name, slug, plan)
VALUES
    ('Google', 'google', 'FREE'),
    ('Amazon', 'amazon', 'FREE'),
    ('Microsoft', 'microsoft', 'FREE'),
    ('Apple', 'apple', 'FREE'),
    ('Meta', 'meta', 'FREE')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO users (org_id, email, password_hash, full_name, role, is_active)
SELECT o.id,
       LOWER('admin@' || o.slug || '.com') AS email,
       crypt('Admin@123', gen_salt('bf', 10)),
       INITCAP(o.name) || ' Admin',
       'ADMIN',
       TRUE
FROM organizations o
WHERE o.slug IN ('google', 'amazon', 'microsoft', 'apple', 'meta')
ON CONFLICT (email) DO NOTHING;
