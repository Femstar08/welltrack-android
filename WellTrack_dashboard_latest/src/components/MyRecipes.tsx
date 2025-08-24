import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { 
  Plus,
  Edit,
  Trash2,
  Share2,
  Clock,
  Users,
  Star,
  Heart,
  Camera,
  Link,
  Mic,
  ChefHat,
  Copy,
  BookOpen
} from "lucide-react";
import { Recipe } from "./Recipes";

interface MyRecipesProps {
  recipes: Recipe[];
  viewMode: 'grid' | 'list';
}

export function MyRecipes({ recipes, viewMode }: MyRecipesProps) {
  const [showCreateOptions, setShowCreateOptions] = useState(false);

  const handleEditRecipe = (recipeId: string) => {
    console.log('Edit recipe:', recipeId);
  };

  const handleDeleteRecipe = (recipeId: string) => {
    console.log('Delete recipe:', recipeId);
  };

  const handleDuplicateRecipe = (recipeId: string) => {
    console.log('Duplicate recipe:', recipeId);
  };

  const createOptions = [
    {
      id: 'manual',
      title: 'Create Manually',
      description: 'Build your recipe step by step',
      icon: <Edit className="w-5 h-5" />,
      color: 'bg-blue-50 border-blue-200 dark:bg-blue-950 dark:border-blue-800'
    },
    {
      id: 'import-url',
      title: 'Import from URL',
      description: 'Paste a recipe link to import automatically',
      icon: <Link className="w-5 h-5" />,
      color: 'bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800'
    },
    {
      id: 'photo-scan',
      title: 'Scan Recipe Photo',
      description: 'Take a photo of a recipe to digitize it',
      icon: <Camera className="w-5 h-5" />,
      color: 'bg-purple-50 border-purple-200 dark:bg-purple-950 dark:border-purple-800'
    },
    {
      id: 'voice',
      title: 'Voice Recording',
      description: 'Dictate your recipe and we\'ll format it',
      icon: <Mic className="w-5 h-5" />,
      color: 'bg-orange-50 border-orange-200 dark:bg-orange-950 dark:border-orange-800'
    }
  ];

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
      <div className="h-full flex flex-col">
        {/* Empty State */}
        <div className="flex-1 flex flex-col items-center justify-center p-8 text-center">
          <ChefHat className="w-16 h-16 text-muted-foreground mb-4" />
          <h3 className="text-lg font-semibold text-card-foreground mb-2">
            Start Creating Your Recipe Collection
          </h3>
          <p className="text-muted-foreground mb-6 max-w-sm">
            Build your personal recipe library by creating, importing, or scanning recipes. 
            Your culinary journey starts here!
          </p>
          
          <Button 
            size="lg" 
            className="mb-6"
            onClick={() => setShowCreateOptions(true)}
          >
            <Plus className="w-5 h-5 mr-2" />
            Create Your First Recipe
          </Button>

          {/* Quick Start Options */}
          <div className="grid grid-cols-2 gap-3 w-full max-w-sm">
            {createOptions.map((option) => (
              <Card key={option.id} className={`cursor-pointer hover:bg-accent/50 transition-colors ${option.color}`}>
                <CardContent className="p-4 text-center">
                  <div className="mb-2">{option.icon}</div>
                  <h4 className="font-medium text-sm mb-1">{option.title}</h4>
                  <p className="text-xs text-muted-foreground line-clamp-2">
                    {option.description}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (viewMode === 'list') {
    return (
      <div className="h-full overflow-y-auto p-4 space-y-4">
        {/* Header with Create Button */}
        <div className="flex items-center justify-between">
          <h3 className="font-medium text-card-foreground">
            My Recipes ({recipes.length})
          </h3>
          <Button size="sm" onClick={() => setShowCreateOptions(true)}>
            <Plus className="w-4 h-4 mr-1" />
            Create Recipe
          </Button>
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
                    <Badge className="absolute -top-1 -right-1 text-xs px-1 py-0 bg-blue-500 text-white">
                      Mine
                    </Badge>
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
                      
                      {/* Action Buttons */}
                      <div className="flex items-center gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                          onClick={() => handleEditRecipe(recipe.id)}
                        >
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                          onClick={() => handleDuplicateRecipe(recipe.id)}
                        >
                          <Copy className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                        >
                          <Share2 className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                          onClick={() => handleDeleteRecipe(recipe.id)}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
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
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Create Recipe Modal/Panel Options */}
        {showCreateOptions && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <Card className="w-full max-w-md">
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  Create New Recipe
                  <Button 
                    variant="ghost" 
                    size="sm" 
                    onClick={() => setShowCreateOptions(false)}
                  >
                    ×
                  </Button>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {createOptions.map((option) => (
                  <Card 
                    key={option.id} 
                    className={`cursor-pointer hover:bg-accent/50 transition-colors ${option.color}`}
                  >
                    <CardContent className="p-4 flex items-center gap-3">
                      {option.icon}
                      <div>
                        <h4 className="font-medium text-sm">{option.title}</h4>
                        <p className="text-xs text-muted-foreground">
                          {option.description}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    );
  }

  // Grid view for My Recipes
  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="font-medium text-card-foreground">
          My Recipes ({recipes.length})
        </h3>
        <Button size="sm" onClick={() => setShowCreateOptions(true)}>
          <Plus className="w-4 h-4 mr-1" />
          Create
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
                
                {/* My Recipe Badge */}
                <Badge className="absolute top-2 left-2 text-xs px-2 py-0.5 bg-blue-500 text-white">
                  Mine
                </Badge>

                {/* Action Menu */}
                <div className="absolute top-2 right-2 flex flex-col gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 w-6 p-0 bg-white/90 hover:bg-white"
                    onClick={() => handleEditRecipe(recipe.id)}
                  >
                    <Edit className="w-3 h-3" />
                  </Button>
                </div>

                {/* Nutritional Score */}
                <Badge className={`absolute bottom-2 left-2 text-xs px-2 py-0.5 ${getNutritionalScoreColor(recipe.nutritionalScore)}`}>
                  {recipe.nutritionalScore}
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

                {/* Cook Count */}
                {recipe.cookCount > 0 && (
                  <div className="text-xs text-center text-muted-foreground">
                    Cooked {recipe.cookCount} times
                  </div>
                )}

                {/* Action Buttons */}
                <div className="flex gap-1 pt-1">
                  <Button size="sm" className="flex-1 text-xs h-8">
                    <BookOpen className="w-3 h-3 mr-1" />
                    View
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="px-2 h-8"
                    onClick={() => handleEditRecipe(recipe.id)}
                  >
                    <Edit className="w-3 h-3" />
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