import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { 
  Lightbulb, 
  Clock, 
  ChefHat, 
  ShoppingBasket, 
  Heart, 
  ChevronDown, 
  ChevronUp,
  Sparkles,
  RefreshCw
} from "lucide-react";
import { useState } from "react";

interface SuggestedMeal {
  id: string;
  name: string;
  image?: string;
  calories: number;
  prepTime: number;
  cookTime: number;
  difficulty: 1 | 2 | 3;
  grade: 'A' | 'B' | 'C' | 'D' | 'E';
  dietary: string[];
  reason: string;
  pantryMatch: number; // percentage of ingredients in pantry
  isFavorite: boolean;
}

export function SmartSuggestions() {
  const [pantryOpen, setPantryOpen] = useState(true);
  const [favoritesOpen, setFavoritesOpen] = useState(true);
  const [quickOpen, setQuickOpen] = useState(false);

  const pantryBasedMeals: SuggestedMeal[] = [
    {
      id: '1',
      name: 'Vegetable Stir Fry',
      calories: 320,
      prepTime: 10,
      cookTime: 15,
      difficulty: 1,
      grade: 'A',
      dietary: ['V', 'GF'],
      reason: 'Uses 90% pantry ingredients',
      pantryMatch: 90,
      isFavorite: false
    },
    {
      id: '2',
      name: 'Chicken Rice Bowl',
      calories: 450,
      prepTime: 15,
      cookTime: 20,
      difficulty: 2,
      grade: 'B',
      dietary: ['GF'],
      reason: 'Uses leftover chicken',
      pantryMatch: 75,
      isFavorite: true
    }
  ];

  const favoriteMeals: SuggestedMeal[] = [
    {
      id: '3',
      name: 'Avocado Toast Supreme',
      calories: 380,
      prepTime: 5,
      cookTime: 5,
      difficulty: 1,
      grade: 'A',
      dietary: ['V'],
      reason: 'Made 5 times this month',
      pantryMatch: 60,
      isFavorite: true
    },
    {
      id: '4',
      name: 'Mediterranean Wrap',
      calories: 420,
      prepTime: 8,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V'],
      reason: 'Rated 5 stars',
      pantryMatch: 45,
      isFavorite: true
    }
  ];

  const quickMeals: SuggestedMeal[] = [
    {
      id: '5',
      name: 'Protein Smoothie Bowl',
      calories: 290,
      prepTime: 5,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V', 'GF'],
      reason: 'Ready in 5 minutes',
      pantryMatch: 80,
      isFavorite: false
    },
    {
      id: '6',
      name: 'Greek Yogurt Parfait',
      calories: 250,
      prepTime: 3,
      cookTime: 0,
      difficulty: 1,
      grade: 'B',
      dietary: ['V'],
      reason: 'No cooking required',
      pantryMatch: 70,
      isFavorite: false
    }
  ];

  const getGradeColor = (grade: string) => {
    switch (grade) {
      case 'A': return "bg-green-500 text-white";
      case 'B': return "bg-green-400 text-white";
      case 'C': return "bg-yellow-500 text-white";
      case 'D': return "bg-orange-500 text-white";
      case 'E': return "bg-red-500 text-white";
      default: return "bg-muted text-muted-foreground";
    }
  };

  const getDifficultyChefs = (difficulty: number) => {
    return Array.from({ length: 3 }, (_, i) => (
      <ChefHat
        key={i}
        className={`w-3 h-3 ${i < difficulty ? 'text-orange-500' : 'text-muted-foreground/30'}`}
      />
    ));
  };

  const MealSuggestionCard = ({ meal }: { meal: SuggestedMeal }) => (
    <Card className="bg-card hover:bg-accent/50 transition-colors cursor-pointer border border-border">
      <CardContent className="p-3">
        <div className="space-y-2">
          {/* Header */}
          <div className="flex items-start justify-between">
            <h4 className="text-sm font-medium line-clamp-2 flex-1 text-card-foreground">
              {meal.name}
            </h4>
            <div className="flex items-center gap-1 ml-2">
              <Badge className={`text-xs px-1 py-0 h-4 ${getGradeColor(meal.grade)}`}>
                {meal.grade}
              </Badge>
              {meal.isFavorite && <Heart className="w-3 h-3 text-red-500 fill-current" />}
            </div>
          </div>

          {/* Reason */}
          <p className="text-xs text-muted-foreground">{meal.reason}</p>

          {/* Stats */}
          <div className="flex items-center justify-between text-xs">
            <span className="text-muted-foreground">{meal.calories} cal</span>
            <div className="flex items-center gap-1">
              <Clock className="w-3 h-3 text-muted-foreground" />
              <span className="text-muted-foreground">{meal.prepTime + meal.cookTime}m</span>
            </div>
            <div className="flex items-center gap-0.5">
              {getDifficultyChefs(meal.difficulty)}
            </div>
          </div>

          {/* Pantry match */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1">
              <ShoppingBasket className="w-3 h-3 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">{meal.pantryMatch}% pantry</span>
            </div>
            <div className="flex gap-1">
              {meal.dietary.map((diet) => (
                <Badge
                  key={diet}
                  variant="secondary"
                  className="text-xs px-1 py-0 h-4"
                >
                  {diet}
                </Badge>
              ))}
            </div>
          </div>

          {/* Action */}
          <Button size="sm" variant="outline" className="w-full h-7 text-xs">
            Add to Plan
          </Button>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="bg-card border-l border-border h-full overflow-y-auto">
      <div className="p-4 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Lightbulb className="w-5 h-5 text-orange-500" />
            <h3 className="font-medium text-card-foreground">Smart Suggestions</h3>
          </div>
          <Button variant="ghost" size="sm" className="h-7 w-7 p-0">
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>

        {/* Pantry-Based Suggestions */}
        <Collapsible open={pantryOpen} onOpenChange={setPantryOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ShoppingBasket className="w-4 h-4 text-green-600" />
                    <span className="text-sm text-green-800 dark:text-green-200">Pantry Match</span>
                  </div>
                  {pantryOpen ? (
                    <ChevronUp className="w-4 h-4 text-green-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-green-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {pantryBasedMeals.map((meal) => (
              <MealSuggestionCard key={meal.id} meal={meal} />
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Favorites */}
        <Collapsible open={favoritesOpen} onOpenChange={setFavoritesOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-red-50 border-red-200 dark:bg-red-950 dark:border-red-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Heart className="w-4 h-4 text-red-600" />
                    <span className="text-sm text-red-800 dark:text-red-200">Your Favorites</span>
                  </div>
                  {favoritesOpen ? (
                    <ChevronUp className="w-4 h-4 text-red-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-red-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {favoriteMeals.map((meal) => (
              <MealSuggestionCard key={meal.id} meal={meal} />
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Quick Meals */}
        <Collapsible open={quickOpen} onOpenChange={setQuickOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-blue-50 border-blue-200 dark:bg-blue-950 dark:border-blue-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-blue-600" />
                    <span className="text-sm text-blue-800 dark:text-blue-200">Quick & Easy</span>
                  </div>
                  {quickOpen ? (
                    <ChevronUp className="w-4 h-4 text-blue-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-blue-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {quickMeals.map((meal) => (
              <MealSuggestionCard key={meal.id} meal={meal} />
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Generate New Ideas */}
        <Button className="w-full bg-purple-500 hover:bg-purple-600 text-white gap-2">
          <Sparkles className="w-4 h-4" />
          Generate New Ideas
        </Button>
      </div>
    </div>
  );
}