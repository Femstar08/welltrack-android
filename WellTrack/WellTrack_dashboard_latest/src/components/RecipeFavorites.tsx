import { Card, CardContent } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { 
  Heart,
  Clock,
  Users,
  Star,
  Share2,
  Plus,
  ChefHat,
  Filter,
  Package
} from "lucide-react";
import { Recipe } from "./Recipes";

interface RecipeFavoritesProps {
  recipes: Recipe[];
  viewMode: 'grid' | 'list';
}

export function RecipeFavorites({ recipes, viewMode }: RecipeFavoritesProps) {
  const handleRemoveFromFavorites = (recipeId: string) => {
    console.log('Remove from favorites:', recipeId);
  };

  const handleAddToMealPlan = (recipeId: string) => {
    console.log('Add to meal plan:', recipeId);
  };

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

  if (recipes.length === 0) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-8 text-center">
        <Heart className="w-16 h-16 text-red-300 mb-4" />
        <h3 className="text-lg font-semibold text-card-foreground mb-2">
          No Favorite Recipes Yet
        </h3>
        <p className="text-muted-foreground mb-6 max-w-sm">
          Start exploring recipes and tap the heart icon to save your favorites here. 
          Build your personal collection of go-to recipes!
        </p>
        
        <Button size="lg">
          <ChefHat className="w-5 h-5 mr-2" />
          Discover Recipes
        </Button>
      </div>
    );
  }

  if (viewMode === 'list') {
    return (
      <div className="h-full overflow-y-auto p-4 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between">
          <h3 className="font-medium text-card-foreground">
            Favorite Recipes ({recipes.length})
          </h3>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">
              <Filter className="w-4 h-4 mr-1" />
              Filter
            </Button>
          </div>
        </div>

        {/* Recipe List */}
        <div className="space-y-3">
          {recipes.map((recipe) => (
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
                        <p className="text-xs text-muted-foreground mt-1">
                          by {recipe.author}
                        </p>
                      </div>
                      
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                        onClick={() => handleRemoveFromFavorites(recipe.id)}
                      >
                        <Heart className="w-4 h-4 fill-current" />
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
                      {recipe.cookCount > 0 && (
                        <span className="text-xs">
                          Cooked {recipe.cookCount} times
                        </span>
                      )}
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
                      {recipe.isUserCreated && (
                        <Badge className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 dark:bg-blue-950">
                          My Recipe
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
          ))}
        </div>
      </div>
    );
  }

  // Grid view
  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="font-medium text-card-foreground">
          Favorite Recipes ({recipes.length})
        </h3>
        <Button variant="outline" size="sm">
          <Filter className="w-4 h-4 mr-1" />
          Filter
        </Button>
      </div>

      {/* Recipe Grid */}
      <div className="grid grid-cols-2 gap-3">
        {recipes.map((recipe) => (
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
                  {recipe.isUserCreated && (
                    <Badge className="text-xs px-2 py-0.5 bg-blue-500 text-white">
                      Mine
                    </Badge>
                  )}
                </div>

                {/* Favorite Button */}
                <Button
                  variant="ghost"
                  size="sm"
                  className="absolute top-2 right-2 h-8 w-8 p-0 bg-white/90 hover:bg-white text-red-500 hover:text-red-700"
                  onClick={() => handleRemoveFromFavorites(recipe.id)}
                >
                  <Heart className="w-4 h-4 fill-current" />
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
                  <p className="text-xs text-muted-foreground">{recipe.cuisine} • by {recipe.author}</p>
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

                {/* Calories and Cook Count */}
                <div className="flex items-center justify-between text-xs">
                  <span className="text-muted-foreground">
                    {recipe.calories} cal
                  </span>
                  {recipe.cookCount > 0 && (
                    <span className="text-muted-foreground">
                      Cooked {recipe.cookCount}x
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
    </div>
  );
}