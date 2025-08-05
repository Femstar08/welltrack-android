-- WellTrack Database Schema: Meals
-- Run this in your Supabase SQL Editor

-- Create meals table
CREATE TABLE IF NOT EXISTS meals (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    recipe_id UUID REFERENCES recipes(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    meal_type TEXT CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'SUPPLEMENT')) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    portions REAL DEFAULT 1.0,
    nutrition_info TEXT, -- JSON string of nutritional information
    score TEXT CHECK (score IN ('A', 'B', 'C', 'D', 'E')),
    status TEXT CHECK (status IN ('PLANNED', 'EATEN', 'SKIPPED')) DEFAULT 'PLANNED',
    rating REAL CHECK (rating >= 0 AND rating <= 5),
    notes TEXT,
    photo_url TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create meal_ingredients table for custom meals (not from recipes)
CREATE TABLE IF NOT EXISTS meal_ingredients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    meal_id UUID REFERENCES meals(id) ON DELETE CASCADE,
    ingredient_id UUID REFERENCES ingredients(id) ON DELETE CASCADE,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(meal_id, ingredient_id)
);

-- Create meal_plans table
CREATE TABLE IF NOT EXISTS meal_plans (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create meal_plan_items table
CREATE TABLE IF NOT EXISTS meal_plan_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    meal_plan_id UUID REFERENCES meal_plans(id) ON DELETE CASCADE,
    meal_id UUID REFERENCES meals(id) ON DELETE CASCADE,
    planned_date DATE NOT NULL,
    planned_time TIME,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(meal_plan_id, meal_id, planned_date, planned_time)
);

-- Enable Row Level Security
ALTER TABLE meals ENABLE ROW LEVEL SECURITY;
ALTER TABLE meal_ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE meal_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE meal_plan_items ENABLE ROW LEVEL SECURITY;

-- Policies for meals
CREATE POLICY "Users can view own meals" ON meals
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own meals" ON meals
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own meals" ON meals
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own meals" ON meals
    FOR DELETE USING (auth.uid() = user_id);

-- Policies for meal_ingredients
CREATE POLICY "Users can view meal ingredients for own meals" ON meal_ingredients
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM meals 
            WHERE meals.id = meal_ingredients.meal_id 
            AND meals.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage ingredients for own meals" ON meal_ingredients
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM meals 
            WHERE meals.id = meal_ingredients.meal_id 
            AND meals.user_id = auth.uid()
        )
    );

-- Policies for meal_plans
CREATE POLICY "Users can view own meal plans" ON meal_plans
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own meal plans" ON meal_plans
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own meal plans" ON meal_plans
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own meal plans" ON meal_plans
    FOR DELETE USING (auth.uid() = user_id);

-- Policies for meal_plan_items
CREATE POLICY "Users can view meal plan items for own plans" ON meal_plan_items
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM meal_plans 
            WHERE meal_plans.id = meal_plan_items.meal_plan_id 
            AND meal_plans.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage meal plan items for own plans" ON meal_plan_items
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM meal_plans 
            WHERE meal_plans.id = meal_plan_items.meal_plan_id 
            AND meal_plans.user_id = auth.uid()
        )
    );

-- Create triggers for updated_at
CREATE TRIGGER update_meals_updated_at 
    BEFORE UPDATE ON meals 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meal_plans_updated_at 
    BEFORE UPDATE ON meal_plans 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_meals_user_id ON meals(user_id);
CREATE INDEX IF NOT EXISTS idx_meals_recipe_id ON meals(recipe_id);
CREATE INDEX IF NOT EXISTS idx_meals_timestamp ON meals(timestamp);
CREATE INDEX IF NOT EXISTS idx_meals_meal_type ON meals(meal_type);
CREATE INDEX IF NOT EXISTS idx_meals_status ON meals(status);
CREATE INDEX IF NOT EXISTS idx_meals_score ON meals(score);
CREATE INDEX IF NOT EXISTS idx_meals_is_favorite ON meals(is_favorite);
CREATE INDEX IF NOT EXISTS idx_meal_ingredients_meal_id ON meal_ingredients(meal_id);
CREATE INDEX IF NOT EXISTS idx_meal_plans_user_id ON meal_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_meal_plans_dates ON meal_plans(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_meal_plan_items_plan_id ON meal_plan_items(meal_plan_id);
CREATE INDEX IF NOT EXISTS idx_meal_plan_items_planned_date ON meal_plan_items(planned_date);