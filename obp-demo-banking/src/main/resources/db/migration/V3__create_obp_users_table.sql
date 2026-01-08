-- Create obp_users table for persisting user information
CREATE TABLE IF NOT EXISTS obp_users (
    user_id VARCHAR(100) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_username ON obp_users(username);
CREATE INDEX IF NOT EXISTS idx_email ON obp_users(email);
CREATE INDEX IF NOT EXISTS idx_created_at ON obp_users(created_at);

-- Add comment to table
COMMENT ON TABLE obp_users IS 'Stores user information created in OBP sandbox';
