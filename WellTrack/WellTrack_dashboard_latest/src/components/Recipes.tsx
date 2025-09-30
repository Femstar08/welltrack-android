import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { RecipesHeader } from "./RecipesHeader";
import { RecipeDiscovery } from "./RecipeDiscovery";
import { MyRecipes } from "./MyRecipes";
import { RecipeFavorites } from "./RecipeFavorites";
import { RecipeCollections } from "./RecipeCollections";

export interface Recipe {
  id: string;
  title: string;
  description: string;
  cuisine: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  prepTime: number; // minutes
  cookTime: number; // minutes
  servings: number;
  calories: number;
  nutritionalScore: 'A' | 'B' | 'C' | 'D' | 'E';
  tags: string[];
  dietary: string[]; // 'vegetarian', 'vegan', 'gluten-free', 'keto', etc.
  ingredients: RecipeIngredient[];
  instructions: RecipeInstruction[];
  image: string;
  author: string;
  rating: number;
  reviewCount: number;
  isUserCreated: boolean;
  isFavorite: boolean;
  pantryMatch?: number; // percentage of ingredients available
  costPerServing?: number;
  lastCooked?: string;
  cookCount: number;
}

export interface RecipeIngredient {
  id: string;
  name: string;
  amount: number;
  unit: string;
  category: string;
  isOptional?: boolean;
  substitutes?: string[];
  inPantry?: boolean;
}

export interface RecipeInstruction {
  id: string;
  stepNumber: number;
  instruction: string;
  duration?: number; // minutes
  temperature?: number; // fahrenheit
  image?: string;
  tips?: string[];
}

export interface RecipeCollection {
  id: string;
  name: string;
  description: string;
  recipeIds: string[];
  color: string;
  icon: string;
  isDefault: boolean;
  createdAt: string;
}

export interface SearchFilters {
  query: string;
  cuisine: string[];
  dietary: string[];
  difficulty: string[];
  maxTime: number;
  minRating: number;
  availableIngredients: boolean;
  nutritionalGoals: string[];
  costRange: [number, number];
}

export function Recipes() {
  const [activeTab, setActiveTab] = useState("discover");
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchFilters, setSearchFilters] = useState<SearchFilters>({
    query: '',
    cuisine: [],
    dietary: [],
    difficulty: [],
    maxTime: 180,
    minRating: 0,
    availableIngredients: false,
    nutritionalGoals: [],
    costRange: [0, 20]
  });

  // Mock recipe data
  const recipes: Recipe[] = [
    {
      id: '1',
      title: 'Mediterranean Quinoa Bowl',
      description: 'A nutritious and colorful bowl packed with quinoa, fresh vegetables, and tahini dressing.',
      cuisine: 'Mediterranean',
      difficulty: 'beginner',
      prepTime: 15,
      cookTime: 20,
      servings: 4,
      calories: 420,
      nutritionalScore: 'A',
      tags: ['healthy', 'meal-prep', 'vegetarian', 'high-protein'],
      dietary: ['vegetarian', 'gluten-free', 'dairy-free'],
      ingredients: [
        { id: '1', name: 'Quinoa', amount: 1, unit: 'cup', category: 'grains', inPantry: true },
        { id: '2', name: 'Cucumber', amount: 1, unit: 'large', category: 'vegetables', inPantry: true },
        { id: '3', name: 'Cherry tomatoes', amount: 2, unit: 'cups', category: 'vegetables', inPantry: false },
        { id: '4', name: 'Red onion', amount: 0.5, unit: 'medium', category: 'vegetables', inPantry: true },
        { id: '5', name: 'Tahini', amount: 3, unit: 'tbsp', category: 'condiments', inPantry: false }
      ],
      instructions: [
        { id: '1', stepNumber: 1, instruction: 'Rinse quinoa and cook according to package directions.', duration: 15 },
        { id: '2', stepNumber: 2, instruction: 'While quinoa cooks, dice cucumber, halve cherry tomatoes, and thinly slice red onion.' },
        { id: '3', stepNumber: 3, instruction: 'Whisk tahini with lemon juice, olive oil, and water to make dressing.' },
        { id: '4', stepNumber: 4, instruction: 'Combine cooked quinoa with vegetables and drizzle with dressing.' }
      ],
      image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400',
      author: 'Chef Maria',
      rating: 4.8,
      reviewCount: 156,
      isUserCreated: false,
      isFavorite: true,
      pantryMatch: 75,
      costPerServing: 3.20,
      cookCount: 3
    },
    {
      id: '2',
      title: 'Honey Garlic Salmon',
      description: 'Perfectly glazed salmon with a sweet and savory honey garlic sauce.',
      cuisine: 'Asian',
      difficulty: 'intermediate',
      prepTime: 10,
      cookTime: 15,
      servings: 4,
      calories: 380,
      nutritionalScore: 'A',
      tags: ['high-protein', 'omega-3', 'quick-dinner'],
      dietary: ['gluten-free', 'dairy-free'],
      ingredients: [
        { id: '1', name: 'Salmon fillets', amount: 4, unit: 'pieces', category: 'protein', inPantry: false },
        { id: '2', name: 'Honey', amount: 3, unit: 'tbsp', category: 'sweeteners', inPantry: true },
        { id: '3', name: 'Soy sauce', amount: 2, unit: 'tbsp', category: 'condiments', inPantry: true },
        { id: '4', name: 'Garlic', amount: 3, unit: 'cloves', category: 'vegetables', inPantry: true },
        { id: '5', name: 'Ginger', amount: 1, unit: 'tbsp', category: 'spices', inPantry: true }
      ],
      instructions: [
        { id: '1', stepNumber: 1, instruction: 'Season salmon fillets with salt and pepper.', duration: 2 },
        { id: '2', stepNumber: 2, instruction: 'Mix honey, soy sauce, minced garlic, and ginger in a bowl.' },
        { id: '3', stepNumber: 3, instruction: 'Heat oil in a large skillet over medium-high heat.', duration: 2 },
        { id: '4', stepNumber: 4, instruction: 'Cook salmon 4-5 minutes per side, then add sauce and glaze.', duration: 10 }
      ],
      image: 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400',
      author: 'Chef David',
      rating: 4.9,
      reviewCount: 203,
      isUserCreated: false,
      isFavorite: false,
      pantryMatch: 90,
      costPerServing: 6.50,
      cookCount: 1
    },
    {
      id: '3',
      title: 'Veggie-Packed Pasta Primavera',
      description: 'Light and fresh pasta loaded with seasonal vegetables in a lemon herb sauce.',
      cuisine: 'Italian',
      difficulty: 'beginner',
      prepTime: 20,
      cookTime: 15,
      servings: 6,
      calories: 350,
      nutritionalScore: 'B',
      tags: ['vegetarian', 'family-friendly', 'colorful'],
      dietary: ['vegetarian'],
      ingredients: [
        { id: '1', name: 'Penne pasta', amount: 12, unit: 'oz', category: 'grains', inPantry: true },
        { id: '2', name: 'Zucchini', amount: 2, unit: 'medium', category: 'vegetables', inPantry: false },
        { id: '3', name: 'Bell peppers', amount: 2, unit: 'large', category: 'vegetables', inPantry: false },
        { id: '4', name: 'Cherry tomatoes', amount: 1, unit: 'cup', category: 'vegetables', inPantry: false },
        { id: '5', name: 'Parmesan cheese', amount: 0.5, unit: 'cup', category: 'dairy', inPantry: true }
      ],
      instructions: [
        { id: '1', stepNumber: 1, instruction: 'Cook pasta according to package directions.', duration: 12 },
        { id: '2', stepNumber: 2, instruction: 'Slice zucchini and bell peppers into thin strips.' },
        { id: '3', stepNumber: 3, instruction: 'Sauté vegetables in olive oil until tender-crisp.', duration: 8 },
        { id: '4', stepNumber: 4, instruction: 'Toss pasta with vegetables, lemon juice, and herbs.' }
      ],
      image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400',
      author: 'Home Cook Lisa',
      rating: 4.6,
      reviewCount: 89,
      isUserCreated: true,
      isFavorite: true,
      pantryMatch: 60,
      costPerServing: 2.80,
      cookCount: 5
    },
    {
      id: '4',
      title: 'Spicy Thai Green Curry',
      description: 'Aromatic and creamy green curry with vegetables and your choice of protein.',
      cuisine: 'Thai',
      difficulty: 'advanced',
      prepTime: 25,
      cookTime: 30,
      servings: 4,
      calories: 520,
      nutritionalScore: 'B',
      tags: ['spicy', 'coconut', 'exotic'],
      dietary: ['gluten-free', 'dairy-free'],
      ingredients: [
        { id: '1', name: 'Green curry paste', amount: 3, unit: 'tbsp', category: 'condiments', inPantry: false },
        { id: '2', name: 'Coconut milk', amount: 1, unit: 'can', category: 'pantry', inPantry: true },
        { id: '3', name: 'Chicken thighs', amount: 1, unit: 'lb', category: 'protein', inPantry: false },
        { id: '4', name: 'Thai basil', amount: 0.5, unit: 'cup', category: 'herbs', inPantry: false },
        { id: '5', name: 'Fish sauce', amount: 2, unit: 'tbsp', category: 'condiments', inPantry: true }
      ],
      instructions: [
        { id: '1', stepNumber: 1, instruction: 'Heat curry paste in a large pot until fragrant.', duration: 2 },
        { id: '2', stepNumber: 2, instruction: 'Add thick part of coconut milk and simmer.', duration: 5 },
        { id: '3', stepNumber: 3, instruction: 'Add chicken and cook until done.', duration: 15 },
        { id: '4', stepNumber: 4, instruction: 'Add vegetables and remaining coconut milk.', duration: 10 }
      ],
      image: 'https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400',
      author: 'Chef Siriporn',
      rating: 4.7,
      reviewCount: 124,
      isUserCreated: false,
      isFavorite: false,
      pantryMatch: 45,
      costPerServing: 4.90,
      cookCount: 0
    },
    {
      id: '5',
      title: 'Classic Overnight Oats',
      description: 'Creamy and customizable overnight oats perfect for busy mornings.',
      cuisine: 'American',
      difficulty: 'beginner',
      prepTime: 5,
      cookTime: 0,
      servings: 1,
      calories: 320,
      nutritionalScore: 'A',
      tags: ['breakfast', 'meal-prep', 'no-cook', 'healthy'],
      dietary: ['vegetarian', 'gluten-free'],
      ingredients: [
        { id: '1', name: 'Rolled oats', amount: 0.5, unit: 'cup', category: 'grains', inPantry: true },
        { id: '2', name: 'Greek yogurt', amount: 0.25, unit: 'cup', category: 'dairy', inPantry: true },
        { id: '3', name: 'Almond milk', amount: 0.5, unit: 'cup', category: 'dairy', inPantry: true },
        { id: '4', name: 'Chia seeds', amount: 1, unit: 'tbsp', category: 'seeds', inPantry: true },
        { id: '5', name: 'Maple syrup', amount: 1, unit: 'tbsp', category: 'sweeteners', inPantry: true }
      ],
      instructions: [
        { id: '1', stepNumber: 1, instruction: 'Combine all ingredients in a jar or container.' },
        { id: '2', stepNumber: 2, instruction: 'Stir well to ensure everything is mixed.' },
        { id: '3', stepNumber: 3, instruction: 'Refrigerate overnight or at least 4 hours.' },
        { id: '4', stepNumber: 4, instruction: 'Add toppings like fruits or nuts before serving.' }
      ],
      image: 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400',
      author: 'Nutritionist Sarah',
      rating: 4.5,
      reviewCount: 67,
      isUserCreated: false,
      isFavorite: true,
      pantryMatch: 100,
      costPerServing: 1.20,
      cookCount: 8
    }
  ];

  // Mock collections
  const collections: RecipeCollection[] = [
    {
      id: '1',
      name: 'Quick Weeknight Dinners',
      description: 'Fast and easy recipes for busy weeknights',
      recipeIds: ['1', '2', '3'],
      color: 'blue',
      icon: 'Clock',
      isDefault: true,
      createdAt: '2024-01-15'
    },
    {
      id: '2',
      name: 'Meal Prep Masters',
      description: 'Recipes perfect for meal prepping',
      recipeIds: ['1', '5'],
      color: 'green',
      icon: 'Package',
      isDefault: false,
      createdAt: '2024-02-01'
    },
    {
      id: '3',
      name: 'Healthy & Delicious',
      description: 'Nutritious recipes that taste amazing',
      recipeIds: ['1', '2', '5'],
      color: 'purple',
      icon: 'Heart',
      isDefault: false,
      createdAt: '2024-02-10'
    }
  ];

  const favoriteRecipes = recipes.filter(recipe => recipe.isFavorite);
  const myRecipes = recipes.filter(recipe => recipe.isUserCreated);

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <RecipesHeader 
        searchFilters={searchFilters}
        setSearchFilters={setSearchFilters}
        viewMode={viewMode}
        setViewMode={setViewMode}
        recipeCount={recipes.length}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-5 mx-4 mt-2">
            <TabsTrigger value="discover" className="text-xs">Discover</TabsTrigger>
            <TabsTrigger value="my-recipes" className="text-xs">My Recipes</TabsTrigger>
            <TabsTrigger value="favorites" className="text-xs">Favorites</TabsTrigger>
            <TabsTrigger value="recent" className="text-xs">Recent</TabsTrigger>
            <TabsTrigger value="collections" className="text-xs">Collections</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="discover" className="h-full m-0">
              <RecipeDiscovery 
                recipes={recipes}
                searchFilters={searchFilters}
                viewMode={viewMode}
              />
            </TabsContent>

            <TabsContent value="my-recipes" className="h-full m-0">
              <MyRecipes 
                recipes={myRecipes}
                viewMode={viewMode}
              />
            </TabsContent>

            <TabsContent value="favorites" className="h-full m-0">
              <RecipeFavorites 
                recipes={favoriteRecipes}
                viewMode={viewMode}
              />
            </TabsContent>

            <TabsContent value="recent" className="h-full m-0">
              <RecipeDiscovery 
                recipes={recipes.filter(r => r.cookCount > 0).sort((a, b) => (b.lastCooked || '').localeCompare(a.lastCooked || ''))}
                searchFilters={searchFilters}
                viewMode={viewMode}
                showRecentInfo={true}
              />
            </TabsContent>

            <TabsContent value="collections" className="h-full m-0">
              <RecipeCollections 
                collections={collections}
                recipes={recipes}
                viewMode={viewMode}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}