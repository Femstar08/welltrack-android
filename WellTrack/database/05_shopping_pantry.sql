-- WellTrack Database Schema: Shopping & Pantry
-- Run this in your Supabase SQL Editor

-- Create shopping_lists table
CREATE TABLE IF NOT EXISTS wt_shopping_lists (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create shopping_list_items table
CREATE TABLE IF NOT EXISTS wt_shopping_list_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    shopping_list_id UUID REFERENCES wt_shopping_lists(id) ON DELETE CASCADE,
    ingredient_id UUID REFERENCES wt_ingredients(id) ON DELETE CASCADE,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL,
    category TEXT, -- produce, dairy, meat, etc.
    is_purchased BOOLEAN DEFAULT FALSE,
    estimated_cost REAL,
    actual_cost REAL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create pantry_items table
CREATE TABLE IF NOT EXISTS wt_pantry_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    ingredient_id UUID REFERENCES wt_ingredients(id) ON DELETE CASCADE,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL,
    purchase_date DATE,
    expiry_date DATE,
    location TEXT, -- fridge, pantry, freezer, etc.
    barcode TEXT,
    cost REAL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create pantry_usage_logs table
CREATE TABLE IF NOT EXISTS wt_pantry_usage_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    pantry_item_id UUID REFERENCES wt_pantry_items(id) ON DELETE CASCADE,
    meal_id UUID REFERENCES wt_meals(id) ON DELETE SET NULL,
    quantity_used REAL NOT NULL,
    unit TEXT NOT NULL,
    usage_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create budget_tracking table
CREATE TABLE IF NOT EXISTS wt_budget_tracking (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    budget_limit REAL NOT NULL,
    actual_spent REAL DEFAULT 0,
    category TEXT, -- groceries, supplements, dining_out, etc.
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create cost_tracking table
CREATE TABLE IF NOT EXISTS wt_cost_tracking (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    item_type TEXT CHECK (item_type IN ('INGREDIENT', 'MEAL', 'RECIPE', 'SUPPLEMENT')) NOT NULL,
    item_id UUID NOT NULL, -- references ingredients, meals, recipes, or supplements
    cost REAL NOT NULL,
    quantity REAL,
    unit TEXT,
    store_name TEXT,
    purchase_date DATE DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE wt_shopping_lists ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_shopping_list_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_pantry_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_pantry_usage_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_budget_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE wt_cost_tracking ENABLE ROW LEVEL SECURITY;

-- Policies for shopping_lists
CREATE POLICY "Users can view own shopping lists" ON wt_shopping_lists
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own shopping lists" ON wt_shopping_lists
    FOR ALL USING (auth.uid() = user_id);

-- Policies for shopping_list_items
CREATE POLICY "Users can view shopping list items for own lists" ON wt_shopping_list_items
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM wt_shopping_lists 
            WHERE wt_shopping_lists.id = wt_shopping_list_items.shopping_list_id 
            AND wt_shopping_lists.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage shopping list items for own lists" ON wt_shopping_list_items
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM wt_shopping_lists 
            WHERE wt_shopping_lists.id = wt_shopping_list_items.shopping_list_id 
            AND wt_shopping_lists.user_id = auth.uid()
        )
    );

-- Policies for pantry_items
CREATE POLICY "Users can view own pantry items" ON wt_pantry_items
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own pantry items" ON wt_pantry_items
    FOR ALL USING (auth.uid() = user_id);

-- Policies for pantry_usage_logs
CREATE POLICY "Users can view own pantry usage logs" ON wt_pantry_usage_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own pantry usage logs" ON wt_pantry_usage_logs
    FOR ALL USING (auth.uid() = user_id);

-- Policies for budget_tracking
CREATE POLICY "Users can view own budget tracking" ON wt_budget_tracking
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own budget tracking" ON wt_budget_tracking
    FOR ALL USING (auth.uid() = user_id);

-- Policies for cost_tracking
CREATE POLICY "Users can view own cost tracking" ON wt_cost_tracking
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own cost tracking" ON wt_cost_tracking
    FOR ALL USING (auth.uid() = user_id);

-- Create triggers for updated_at
CREATE TRIGGER update_wt_shopping_lists_updated_at 
    BEFORE UPDATE ON wt_shopping_lists 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wt_shopping_list_items_updated_at 
    BEFORE UPDATE ON wt_shopping_list_items 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wt_pantry_items_updated_at 
    BEFORE UPDATE ON wt_pantry_items 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wt_budget_tracking_updated_at 
    BEFORE UPDATE ON wt_budget_tracking 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_wt_shopping_lists_user_id ON wt_shopping_lists(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_shopping_list_items_list_id ON wt_shopping_list_items(shopping_list_id);
CREATE INDEX IF NOT EXISTS idx_wt_shopping_list_items_ingredient_id ON wt_shopping_list_items(ingredient_id);
CREATE INDEX IF NOT EXISTS idx_wt_pantry_items_user_id ON wt_pantry_items(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_pantry_items_ingredient_id ON wt_pantry_items(ingredient_id);
CREATE INDEX IF NOT EXISTS idx_wt_pantry_items_expiry_date ON wt_pantry_items(expiry_date);
CREATE INDEX IF NOT EXISTS idx_wt_pantry_usage_logs_user_id ON wt_pantry_usage_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_pantry_usage_logs_usage_date ON wt_pantry_usage_logs(usage_date);
CREATE INDEX IF NOT EXISTS idx_wt_budget_tracking_user_id ON wt_budget_tracking(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_budget_tracking_period ON wt_budget_tracking(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_wt_cost_tracking_user_id ON wt_cost_tracking(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_cost_tracking_item ON wt_cost_tracking(item_type, item_id);