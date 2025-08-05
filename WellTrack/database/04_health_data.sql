-- WellTrack Database Schema: Health Data
-- Run this in your Supabase SQL Editor

-- Create health_metrics table
CREATE TABLE IF NOT EXISTS health_metrics (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    metric_type TEXT NOT NULL, -- weight, heart_rate, blood_pressure, etc.
    value REAL NOT NULL,
    unit TEXT NOT NULL, -- kg, bpm, mmHg, etc.
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    source TEXT CHECK (source IN ('MANUAL', 'HEALTH_CONNECT', 'GARMIN', 'SAMSUNG_HEALTH', 'OTHER')) DEFAULT 'MANUAL',
    metadata TEXT, -- JSON string for additional data
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create supplements table
CREATE TABLE IF NOT EXISTS supplements (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    brand TEXT,
    description TEXT,
    serving_size TEXT,
    nutrition_info TEXT, -- JSON string of nutritional information
    barcode TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(name, brand)
);

-- Create user_supplements table (user's supplement library)
CREATE TABLE IF NOT EXISTS user_supplements (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    supplement_id UUID REFERENCES supplements(id) ON DELETE CASCADE,
    custom_name TEXT, -- user's custom name for the supplement
    dosage TEXT,
    frequency TEXT, -- daily, weekly, etc.
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, supplement_id)
);

-- Create supplement_logs table
CREATE TABLE IF NOT EXISTS supplement_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    user_supplement_id UUID REFERENCES user_supplements(id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    dosage_taken TEXT,
    status TEXT CHECK (status IN ('TAKEN', 'SKIPPED', 'PLANNED')) DEFAULT 'TAKEN',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create custom_habits table
CREATE TABLE IF NOT EXISTS custom_habits (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT, -- meditation, exercise, etc.
    target_frequency TEXT, -- daily, weekly, etc.
    target_value REAL, -- target number per frequency
    unit TEXT, -- minutes, reps, etc.
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create habit_logs table
CREATE TABLE IF NOT EXISTS habit_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    habit_id UUID REFERENCES custom_habits(id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    value REAL, -- actual value achieved
    status TEXT CHECK (status IN ('COMPLETED', 'PARTIAL', 'SKIPPED')) DEFAULT 'COMPLETED',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create biomarkers table
CREATE TABLE IF NOT EXISTS biomarkers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    test_date DATE NOT NULL,
    test_type TEXT NOT NULL, -- blood_panel, hormone_panel, etc.
    marker_name TEXT NOT NULL, -- testosterone, vitamin_d, etc.
    value REAL NOT NULL,
    unit TEXT NOT NULL,
    reference_range_min REAL,
    reference_range_max REAL,
    lab_name TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE health_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE supplements ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_supplements ENABLE ROW LEVEL SECURITY;
ALTER TABLE supplement_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE custom_habits ENABLE ROW LEVEL SECURITY;
ALTER TABLE habit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE biomarkers ENABLE ROW LEVEL SECURITY;

-- Policies for health_metrics
CREATE POLICY "Users can view own health metrics" ON health_metrics
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own health metrics" ON health_metrics
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own health metrics" ON health_metrics
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own health metrics" ON health_metrics
    FOR DELETE USING (auth.uid() = user_id);

-- Policies for supplements (public read, authenticated write)
CREATE POLICY "Anyone can view supplements" ON supplements
    FOR SELECT TO authenticated;

CREATE POLICY "Authenticated users can insert supplements" ON supplements
    FOR INSERT TO authenticated WITH CHECK (TRUE);

CREATE POLICY "Authenticated users can update supplements" ON supplements
    FOR UPDATE TO authenticated USING (TRUE);

-- Policies for user_supplements
CREATE POLICY "Users can view own supplements" ON user_supplements
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own supplements" ON user_supplements
    FOR ALL USING (auth.uid() = user_id);

-- Policies for supplement_logs
CREATE POLICY "Users can view own supplement logs" ON supplement_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own supplement logs" ON supplement_logs
    FOR ALL USING (auth.uid() = user_id);

-- Policies for custom_habits
CREATE POLICY "Users can view own habits" ON custom_habits
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own habits" ON custom_habits
    FOR ALL USING (auth.uid() = user_id);

-- Policies for habit_logs
CREATE POLICY "Users can view own habit logs" ON habit_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own habit logs" ON habit_logs
    FOR ALL USING (auth.uid() = user_id);

-- Policies for biomarkers
CREATE POLICY "Users can view own biomarkers" ON biomarkers
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own biomarkers" ON biomarkers
    FOR ALL USING (auth.uid() = user_id);

-- Create triggers for updated_at
CREATE TRIGGER update_user_supplements_updated_at 
    BEFORE UPDATE ON user_supplements 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_custom_habits_updated_at 
    BEFORE UPDATE ON custom_habits 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_health_metrics_user_id ON health_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_health_metrics_type ON health_metrics(metric_type);
CREATE INDEX IF NOT EXISTS idx_health_metrics_timestamp ON health_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_supplements_name ON supplements(name);
CREATE INDEX IF NOT EXISTS idx_user_supplements_user_id ON user_supplements(user_id);
CREATE INDEX IF NOT EXISTS idx_supplement_logs_user_id ON supplement_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_supplement_logs_timestamp ON supplement_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_custom_habits_user_id ON custom_habits(user_id);
CREATE INDEX IF NOT EXISTS idx_habit_logs_user_id ON habit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_habit_logs_timestamp ON habit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_biomarkers_user_id ON biomarkers(user_id);
CREATE INDEX IF NOT EXISTS idx_biomarkers_test_date ON biomarkers(test_date);