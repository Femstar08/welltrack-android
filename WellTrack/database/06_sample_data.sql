-- WellTrack Database Schema: Sample Data
-- Run this in your Supabase SQL Editor (OPTIONAL)
-- This provides some initial data to test your app

-- Insert common ingredients
INSERT INTO ingredients (name, category, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, fiber_per_100g) VALUES
-- Proteins
('Chicken Breast', 'PROTEIN', 165, 31, 0, 3.6, 0),
('Salmon', 'PROTEIN', 208, 20, 0, 12, 0),
('Eggs', 'PROTEIN', 155, 13, 1.1, 11, 0),
('Greek Yogurt', 'DAIRY', 59, 10, 3.6, 0.4, 0),
('Tofu', 'PROTEIN', 76, 8, 1.9, 4.8, 0.3),

-- Vegetables
('Broccoli', 'VEGETABLES', 34, 2.8, 7, 0.4, 2.6),
('Spinach', 'VEGETABLES', 23, 2.9, 3.6, 0.4, 2.2),
('Bell Pepper', 'VEGETABLES', 31, 1, 7, 0.3, 2.5),
('Tomato', 'VEGETABLES', 18, 0.9, 3.9, 0.2, 1.2),
('Onion', 'VEGETABLES', 40, 1.1, 9.3, 0.1, 1.7),

-- Fruits
('Banana', 'FRUITS', 89, 1.1, 23, 0.3, 2.6),
('Apple', 'FRUITS', 52, 0.3, 14, 0.2, 2.4),
('Avocado', 'FRUITS', 160, 2, 9, 15, 7),
('Blueberries', 'FRUITS', 57, 0.7, 14, 0.3, 2.4),

-- Grains
('Brown Rice', 'GRAINS', 111, 2.6, 23, 0.9, 1.8),
('Quinoa', 'GRAINS', 120, 4.4, 22, 1.9, 2.8),
('Oats', 'GRAINS', 68, 2.4, 12, 1.4, 1.7),
('Whole Wheat Bread', 'GRAINS', 247, 13, 41, 4.2, 7),

-- Fats
('Olive Oil', 'FATS', 884, 0, 0, 100, 0),
('Almonds', 'FATS', 579, 21, 22, 50, 12),
('Peanut Butter', 'FATS', 588, 25, 20, 50, 6),

-- Spices
('Salt', 'SPICES', 0, 0, 0, 0, 0),
('Black Pepper', 'SPICES', 251, 10, 64, 3.3, 25),
('Garlic', 'SPICES', 149, 6.4, 33, 0.5, 2.1),
('Ginger', 'SPICES', 80, 1.8, 18, 0.8, 2)

ON CONFLICT (name) DO NOTHING;

-- Insert common supplements
INSERT INTO supplements (name, brand, description, serving_size, nutrition_info) VALUES
('Vitamin D3', 'Generic', 'Vitamin D3 supplement for bone health', '1 capsule', '{"vitamin_d": "1000 IU"}'),
('Omega-3 Fish Oil', 'Generic', 'EPA and DHA omega-3 fatty acids', '1 softgel', '{"epa": "180mg", "dha": "120mg"}'),
('Multivitamin', 'Generic', 'Complete daily multivitamin', '1 tablet', '{"vitamin_a": "5000 IU", "vitamin_c": "60mg", "vitamin_e": "30 IU"}'),
('Protein Powder', 'Generic', 'Whey protein powder', '1 scoop (30g)', '{"protein": "24g", "calories": "120", "carbs": "2g", "fat": "1g"}'),
('Magnesium', 'Generic', 'Magnesium supplement for muscle function', '1 capsule', '{"magnesium": "400mg"}'),
('Probiotics', 'Generic', 'Multi-strain probiotic supplement', '1 capsule', '{"live_cultures": "10 billion CFU"}')

ON CONFLICT (name, brand) DO NOTHING;

-- Note: User-specific data (profiles, meals, etc.) should be created through the app
-- as it requires authentication and proper user context