-- WellTrack Database Schema: Recipes
-- Run this in your Supabase SQL Editor

-- Create recipes table
CREATE TABLE IF NOT EXISTS wt_recipes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    prep_time INTEGER, -- in minutes
    cook_time INTEGER, -- in minutes
    servings INTEGER DEFAULT 1,
    instructions TEXT, -- JSON string of step-by-step instructions
    nutrition_info TEXT, -- JSON string of nutritional information
    source_type TEXT CHECK (source_type IN ('MANUAL', 'URL_IMPORT', 'OCR_SCAN')) DEFAULT 'MANUAL',
    source_url TEXT,
    rating REAL CHECK (rating >= 0 AND rating <= 5),
    tags TEXT, -- comma-separated tags
    is_favorite BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create ingredients table
CREATE TABLE IF NOT EXISTS wt_ingredients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    category TEXT CHECK (category IN ('PROTEIN', 'VEGETABLES', 'FRUITS', 'GRAINS', 'DAIRY', 'FATS', 'SPICES', 'OTHER')) DEFAULT 'OTHER',
    calories_per_100g REAL,
    protein_per_100g REAL,
    carbs_per_100g REAL,
    fat_per_100g REAL,
    fiber_per_100g REAL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create recipe_ingredients junction table
CREATE TABLE IF NOT EXISTS wt_recipe_ingredients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    recipe_id UUID REFERENCES wt_recipes(id) ON DELETE CASCADE,
    ingredient_id UUID REFERENCES wt_ingredients(id) ON DELETE CASCADE,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL, -- g, ml, cup, tbsp, etc.
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(recipe_id, ingredient_id)
);

-- Enable Row Level Security
ALTER TABLE wt_recipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_recipe_ingredients ENABLE ROW LEVEL SECURITY;

-- Policies for recipes
CREATE POLICY "Users can view own recipes" ON wt_recipes
    FOR SELECT USING (auth.uid() = user_id OR is_public = TRUE);

CREATE POLICY "Users can insert own recipes" ON wt_recipes
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own recipes" ON wt_recipes
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own recipes" ON wt_recipes
    FOR DELETE USING (auth.uid() = user_id);

-- Policies for ingredients (public read, authenticated write)
CREATE POLICY "Anyone can view ingredients" ON wt_ingredients
    FOR SELECT TO authenticated;

CREATE POLICY "Authenticated users can insert ingredients" ON wt_ingredients
    FOR INSERT TO authenticated WITH CHECK (TRUE);

CREATE POLICY "Authenticated users can update ingredients" ON wt_ingredients
    FOR UPDATE TO authenticated USING (TRUE);

-- Policies for recipe_ingredients
CREATE POLICY "Users can view recipe ingredients for accessible recipes" ON wt_recipe_ingredients
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM wt_recipes 
            WHERE wt_recipes.id = wt_recipe_ingredients.recipe_id 
            AND (wt_recipes.user_id = auth.uid() OR wt_recipes.is_public = TRUE)
        )
    );

CREATE POLICY "Users can manage ingredients for own recipes" ON wt_recipe_ingredients
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM wt_recipes 
            WHERE wt_recipes.id = wt_recipe_ingredients.recipe_id 
            AND wt_recipes.user_id = auth.uid()
        )
    );

-- Create triggers for updated_at
CREATE TRIGGER update_wt_recipes_updated_at 
    BEFORE UPDATE ON wt_recipes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_wt_recipes_user_id ON wt_recipes(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_recipes_created_at ON wt_recipes(created_at);
CREATE INDEX IF NOT EXISTS idx_wt_recipes_rating ON wt_recipes(rating);
CREATE INDEX IF NOT EXISTS idx_wt_recipes_is_favorite ON wt_recipes(is_favorite);
CREATE INDEX IF NOT EXISTS idx_wt_recipes_is_public ON wt_recipes(is_public);
CREATE INDEX IF NOT EXISTS idx_wt_ingredients_name ON wt_ingredients(name);
CREATE INDEX IF NOT EXISTS idx_wt_ingredients_category ON wt_ingredients(category);
CREATE INDEX IF NOT EXISTS idx_wt_recipe_ingredients_recipe_id ON wt_recipe_ingredients(recipe_id);
CREATE INDEX IF NOT EXISTS idx_wt_recipe_ingredients_ingredient_id ON wt_recipe_ingredients(ingredient_id);