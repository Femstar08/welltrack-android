import { useState } from "react";
import { Card, CardContent } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { 
  Clock,
  Star,
  Users,
  Plus,
  Heart,
  Share2,
  ChefHat,
  Zap,
  Leaf,
  GradeA,
  DollarSign,
  Package
} from "lucide-react";
import { Recipe, SearchFilters } from "./Recipes";

interface RecipeDiscoveryProps {
  recipes: Recipe[];
  searchFilters: SearchFilters;
  viewMode: 'grid' | 'list';
  showRecentInfo?: boolean;
}

export function RecipeDiscovery({ 
  recipes, 
  searchFilters, 
  viewMode, 
  showRecentInfo = false 
}: RecipeDiscoveryProps) {
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null);

  // Filter recipes based on search criteria
  const filteredRecipes = recipes.filter(recipe => {
    // Text search
    if (searchFilters.query) {
      const query = searchFilters.query.toLowerCase();
      const matchesText = 
        recipe.title.toLowerCase().includes(query) ||
        recipe.description.toLowerCase().includes(query) ||
        recipe.cuisine.toLowerCase().includes(query) ||
        recipe.tags.some(tag => tag.toLowerCase().includes(query)) ||
        recipe.ingredients.some(ing => ing.name.toLowerCase().includes(query));
      
      if (!matchesText) return false;
    }

    // Cuisine filter
    if (searchFilters.cuisine.length > 0 && !searchFilters.cuisine.includes(recipe.cuisine)) {
      return false;
    }

    // Dietary restrictions
    if (searchFilters.dietary.length > 0) {
      const hasAllDietaryReqs = searchFilters.dietary.every(diet => 
        recipe.dietary.includes(diet)
      );
      if (!hasAllDietaryReqs) return false;
    }

    // Difficulty filter
    if (searchFilters.difficulty.length > 0 && !searchFilters.difficulty.includes(recipe.difficulty)) {
      return false;
    }

    // Time filter
    const totalTime = recipe.prepTime + recipe.cookTime;
    if (totalTime > searchFilters.maxTime) return false;

    // Rating filter
    if (recipe.rating < searchFilters.minRating) return false;

    // Available ingredients filter
    if (searchFilters.availableIngredients && (recipe.pantryMatch || 0) < 80) {
      return false;
    }

    return true;
  });

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'beginner': return 'text-green-600 bg-green-100 dark:bg-green-950';
      case 'intermediate': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950';
      case 'advanced': return 'text-red-600 bg-red-100 dark:bg-red-950';
      default: return 'text-muted-foreground bg-muted';
    }
  };

  const getNutritionalScoreColor = (score: string) => {
    switch (score) {
      case 'A': return 'text-green-600 bg-green-100 dark:bg-green-950';
      case 'B': return 'text-blue-600 bg-blue-100 dark:bg-blue-950';
      case 'C': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950';
      case 'D': return 'text-orange-600 bg-orange-100 dark:bg-orange-950';
      case 'E': return 'text-red-600 bg-red-100 dark:bg-red-950';
      default: return 'text-muted-foreground bg-muted';
    }
  };

  const handleToggleFavorite = (recipeId: string) => {
    // This would integrate with state management
    console.log('Toggle favorite:', recipeId);
  };

  const handleAddToMealPlan = (recipeId: string) => {
    console.log('Add to meal plan:', recipeId);
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-0.5">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star 
            key={star}
            className={`w-3 h-3 ${star <= rating ? 'text-yellow-500 fill-current' : 'text-muted-foreground'}`}
          />
        ))}
      </div>
    );
  };

  if (viewMode === 'list') {
    return (
      <div className="h-full overflow-y-auto p-4 space-y-3">
        {filteredRecipes.length === 0 ? (
          <div className="text-center py-12">
            <ChefHat className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground">
              No recipes found matching your criteria.
            </p>
            <Button className="mt-4" size="sm">
              Clear Filters
            </Button>
          </div>
        ) : (
          filteredRecipes.map((recipe) => (
            <Card key={recipe.id} className="hover:bg-accent/50 transition-colors">
              <CardContent className="p-4">
                <div className="flex gap-4">
                  {/* Recipe Image */}
                  <div className="relative w-24 h-24 flex-shrink-0">
                    <img
                      src={recipe.image}
                      alt={recipe.title}
                      className="w-full h-full object-cover rounded-lg"
                    />
                    {recipe.pantryMatch && recipe.pantryMatch > 70 && (
                      <Badge className="absolute -top-1 -right-1 text-xs px-1 py-0 bg-green-500 text-white">
                        {recipe.pantryMatch}%
                      </Badge>
                    )}
                  </div>

                  {/* Recipe Details */}
                  <div className="flex-1 space-y-2">
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="font-medium text-card-foreground line-clamp-1">
                          {recipe.title}
                        </h3>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {recipe.description}
                        </p>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                        onClick={() => handleToggleFavorite(recipe.id)}
                      >
                        <Heart className={`w-4 h-4 ${recipe.isFavorite ? 'fill-current' : ''}`} />
                      </Button>
                    </div>

                    {/* Recipe Meta */}
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {recipe.prepTime + recipe.cookTime}m
                      </div>
                      <div className="flex items-center gap-1">
                        <Users className="w-3 h-3" />
                        {recipe.servings}
                      </div>
                      <div className="flex items-center gap-1">
                        {renderStars(recipe.rating)}
                        <span className="text-xs">({recipe.reviewCount})</span>
                      </div>
                    </div>

                    {/* Tags and Badges */}
                    <div className="flex items-center gap-2 flex-wrap">
                      <Badge className={`text-xs px-2 py-0.5 ${getDifficultyColor(recipe.difficulty)}`}>
                        {recipe.difficulty}
                      </Badge>
                      <Badge className={`text-xs px-2 py-0.5 ${getNutritionalScoreColor(recipe.nutritionalScore)}`}>
                        Grade {recipe.nutritionalScore}
                      </Badge>
                      {recipe.dietary.slice(0, 2).map((diet) => (
                        <Badge key={diet} variant="secondary" className="text-xs px-2 py-0.5">
                          {diet}
                        </Badge>
                      ))}
                      {showRecentInfo && recipe.cookCount > 0 && (
                        <Badge className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 dark:bg-blue-950">
                          Cooked {recipe.cookCount}x
                        </Badge>
                      )}
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-2">
                      <Button size="sm" className="flex-1">
                        View Recipe
                      </Button>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => handleAddToMealPlan(recipe.id)}
                      >
                        <Plus className="w-4 h-4 mr-1" />
                        Plan
                      </Button>
                      <Button variant="outline" size="sm" className="px-3">
                        <Share2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto p-4">
      {filteredRecipes.length === 0 ? (
        <div className="text-center py-12">
          <ChefHat className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">
            No recipes found matching your criteria.
          </p>
          <Button className="mt-4" size="sm">
            Clear Filters
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3">
          {filteredRecipes.map((recipe) => (
            <Card key={recipe.id} className="hover:bg-accent/50 transition-colors overflow-hidden">
              <CardContent className="p-0">
                {/* Recipe Image */}
                <div className="relative aspect-[4/3]">
                  <img
                    src={recipe.image}
                    alt={recipe.title}
                    className="w-full h-full object-cover"
                  />
                  
                  {/* Overlay Badges */}
                  <div className="absolute top-2 left-2 flex flex-col gap-1">
                    <Badge className={`text-xs px-2 py-0.5 ${getNutritionalScoreColor(recipe.nutritionalScore)}`}>
                      {recipe.nutritionalScore}
                    </Badge>
                    {recipe.pantryMatch && recipe.pantryMatch > 70 && (
                      <Badge className="text-xs px-2 py-0.5 bg-green-500 text-white">
                        <Package className="w-3 h-3 mr-1" />
                        {recipe.pantryMatch}%
                      </Badge>
                    )}
                  </div>

                  {/* Favorite Button */}
                  <Button
                    variant="ghost"
                    size="sm"
                    className="absolute top-2 right-2 h-8 w-8 p-0 bg-white/90 hover:bg-white text-red-500 hover:text-red-700"
                    onClick={() => handleToggleFavorite(recipe.id)}
                  >
                    <Heart className={`w-4 h-4 ${recipe.isFavorite ? 'fill-current' : ''}`} />
                  </Button>

                  {/* Difficulty Badge */}
                  <Badge className={`absolute bottom-2 left-2 text-xs px-2 py-0.5 ${getDifficultyColor(recipe.difficulty)}`}>
                    {recipe.difficulty}
                  </Badge>
                </div>

                {/* Recipe Info */}
                <div className="p-3 space-y-2">
                  <div>
                    <h3 className="font-medium text-card-foreground line-clamp-2 text-sm leading-tight">
                      {recipe.title}
                    </h3>
                    <p className="text-xs text-muted-foreground">{recipe.cuisine}</p>
                  </div>

                  {/* Quick Stats */}
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {recipe.prepTime + recipe.cookTime}m
                    </div>
                    <div className="flex items-center gap-1">
                      <Users className="w-3 h-3" />
                      {recipe.servings}
                    </div>
                    <div className="flex items-center gap-1">
                      {renderStars(recipe.rating)}
                    </div>
                  </div>

                  {/* Calories and Cost */}
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-muted-foreground">
                      {recipe.calories} cal
                    </span>
                    {recipe.costPerServing && (
                      <span className="text-muted-foreground">
                        ${recipe.costPerServing.toFixed(2)}/serving
                      </span>
                    )}
                  </div>

                  {/* Dietary Tags */}
                  <div className="flex flex-wrap gap-1">
                    {recipe.dietary.slice(0, 2).map((diet) => (
                      <Badge key={diet} variant="secondary" className="text-xs px-1.5 py-0">
                        {diet.charAt(0).toUpperCase() + diet.slice(1)}
                      </Badge>
                    ))}
                    {showRecentInfo && recipe.cookCount > 0 && (
                      <Badge className="text-xs px-1.5 py-0 bg-blue-100 text-blue-800 dark:bg-blue-950">
                        {recipe.cookCount}x
                      </Badge>
                    )}
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-1 pt-1">
                    <Button size="sm" className="flex-1 text-xs h-8">
                      View Recipe
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="px-2 h-8"
                      onClick={() => handleAddToMealPlan(recipe.id)}
                    >
                      <Plus className="w-3 h-3" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}