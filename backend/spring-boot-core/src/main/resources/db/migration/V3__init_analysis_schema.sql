CREATE TABLE analyses (
    id SERIAL PRIMARY KEY,
    repository_name VARCHAR(150) NOT NULL,
    branch VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    overall_score INT,
    security_score INT,
    reliability_score INT,
    maintainability_score INT,
    performance_score INT,
    code_quality_score INT,
    technical_debt_hours DOUBLE PRECISION,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analysis_files (
    id SERIAL PRIMARY KEY,
    analysis_id INT REFERENCES analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(255) NOT NULL,
    language VARCHAR(50)
);

CREATE TABLE issues (
    id SERIAL PRIMARY KEY,
    analysis_id INT REFERENCES analyses(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    line_number INT,
    description TEXT,
    impact TEXT,
    recommendation TEXT,
    suggested_fix TEXT,
    source VARCHAR(50) NOT NULL
);

CREATE TABLE issue_comments (
    id SERIAL PRIMARY KEY,
    issue_id INT REFERENCES issues(id) ON DELETE CASCADE,
    author VARCHAR(100) NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE technical_debt (
    id SERIAL PRIMARY KEY,
    repository_name VARCHAR(150) UNIQUE NOT NULL,
    total_hours DOUBLE PRECISION DEFAULT 0.0,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
