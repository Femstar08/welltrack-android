-- WellTrack Database Schema: Health Data
-- Run this in your Supabase SQL Editor

-- Create health_metrics table
CREATE TABLE IF NOT EXISTS wt_health_metrics (
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
CREATE TABLE IF NOT EXISTS wt_supplements (
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
CREATE TABLE IF NOT EXISTS wt_user_supplements (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    supplement_id UUID REFERENCES wt_supplements(id) ON DELETE CASCADE,
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
CREATE TABLE IF NOT EXISTS wt_supplement_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    user_supplement_id UUID REFERENCES wt_user_supplements(id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    dosage_taken TEXT,
    status TEXT CHECK (status IN ('TAKEN', 'SKIPPED', 'PLANNED')) DEFAULT 'TAKEN',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create custom_habits table
CREATE TABLE IF NOT EXISTS wt_custom_habits (
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
CREATE TABLE IF NOT EXISTS wt_habit_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    habit_id UUID REFERENCES wt_custom_habits(id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    value REAL, -- actual value achieved
    status TEXT CHECK (status IN ('COMPLETED', 'PARTIAL', 'SKIPPED')) DEFAULT 'COMPLETED',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create biomarkers table
CREATE TABLE IF NOT EXISTS wt_biomarkers (
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
ALTER TABLE wt_health_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_supplements ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_user_supplements ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_supplement_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_custom_habits ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_habit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_biomarkers ENABLE ROW LEVEL SECURITY;

-- Policies for health_metrics
CREATE POLICY "Users can view own health metrics" ON wt_health_metrics
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own health metrics" ON wt_health_metrics
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own health metrics" ON wt_health_metrics
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own health metrics" ON wt_health_metrics
    FOR DELETE USING (auth.uid() = user_id);

-- Policies for supplements (public read, authenticated write)
CREATE POLICY "Anyone can view supplements" ON wt_supplements
    FOR SELECT TO authenticated;

CREATE POLICY "Authenticated users can insert supplements" ON wt_supplements
    FOR INSERT TO authenticated WITH CHECK (TRUE);

CREATE POLICY "Authenticated users can update supplements" ON wt_supplements
    FOR UPDATE TO authenticated USING (TRUE);

-- Policies for user_supplements
CREATE POLICY "Users can view own supplements" ON wt_user_supplements
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own supplements" ON wt_user_supplements
    FOR ALL USING (auth.uid() = user_id);

-- Policies for supplement_logs
CREATE POLICY "Users can view own supplement logs" ON wt_supplement_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own supplement logs" ON wt_supplement_logs
    FOR ALL USING (auth.uid() = user_id);

-- Policies for custom_habits
CREATE POLICY "Users can view own habits" ON wt_custom_habits
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own habits" ON wt_custom_habits
    FOR ALL USING (auth.uid() = user_id);

-- Policies for habit_logs
CREATE POLICY "Users can view own habit logs" ON wt_habit_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own habit logs" ON wt_habit_logs
    FOR ALL USING (auth.uid() = user_id);

-- Policies for biomarkers
CREATE POLICY "Users can view own biomarkers" ON wt_biomarkers
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own biomarkers" ON wt_biomarkers
    FOR ALL USING (auth.uid() = user_id);

-- Create triggers for updated_at
CREATE TRIGGER update_wt_user_supplements_updated_at 
    BEFORE UPDATE ON wt_user_supplements 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wt_custom_habits_updated_at 
    BEFORE UPDATE ON wt_custom_habits 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_wt_health_metrics_user_id ON wt_health_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_health_metrics_type ON wt_health_metrics(metric_type);
CREATE INDEX IF NOT EXISTS idx_wt_health_metrics_timestamp ON wt_health_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_wt_supplements_name ON wt_supplements(name);
CREATE INDEX IF NOT EXISTS idx_wt_user_supplements_user_id ON wt_user_supplements(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_supplement_logs_user_id ON wt_supplement_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_supplement_logs_timestamp ON wt_supplement_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_wt_custom_habits_user_id ON wt_custom_habits(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_habit_logs_user_id ON wt_habit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_habit_logs_timestamp ON wt_habit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_wt_biomarkers_user_id ON wt_biomarkers(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_biomarkers_test_date ON wt_biomarkers(test_date);