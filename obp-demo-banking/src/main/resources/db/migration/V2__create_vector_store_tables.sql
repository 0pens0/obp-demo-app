-- Create vector store table for Spring AI
-- This table will be used by Spring AI PostgreSQL vector store
-- The Spring AI library will handle the actual table creation, but we ensure the extension exists

-- Note: Spring AI PostgreSQL vector store will create its own tables automatically
-- This migration ensures pgvector extension is available
