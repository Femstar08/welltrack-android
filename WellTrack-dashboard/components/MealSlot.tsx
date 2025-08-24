import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Plus, CheckCircle, Clock, ChefHat } from "lucide-react";

interface MealData {
  id: string;
  name: string;
  image?: string;
  calories: number;
  prepTime: number;
  cookTime: number;
  difficulty: 1 | 2 | 3;
  grade: 'A' | 'B' | 'C' | 'D' | 'E';
  dietary: string[];
  completed: boolean;
  mealPrepBatch?: string;
}

interface MealType {
  id: string;
  name: string;
  color: string;
}

interface MealSlotProps {
  meal: MealData | null;
  mealType: MealType;
  mealPrepMode: boolean;
  onMealClick: (meal: MealData) => void;
  onAddMeal: () => void;
}

export function MealSlot({ meal, mealType, mealPrepMode, onMealClick, onAddMeal }: MealSlotProps) {
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

  if (!meal) {
    return (
      <Button
        variant="outline"
        className={`h-16 sm:h-20 w-full border-2 border-dashed hover:border-solid transition-all ${mealType.color}`}
        onClick={onAddMeal}
      >
        <Plus className="w-4 sm:w-5 h-4 sm:h-5 text-muted-foreground" />
      </Button>
    );
  }

  return (
    <div
      className={`relative p-1.5 sm:p-2 rounded-lg border-2 cursor-pointer transition-all hover:shadow-md min-h-[64px] sm:min-h-[80px] ${
        mealPrepMode && meal.mealPrepBatch 
          ? `${mealType.color} ring-2 ring-blue-400` 
          : mealType.color
      }`}
      onClick={() => onMealClick(meal)}
    >
      {/* Completed indicator */}
      {meal.completed && (
        <div className="absolute -top-1 -right-1 z-10">
          <CheckCircle className="w-5 h-5 text-green-500 bg-card rounded-full" />
        </div>
      )}

      {/* Meal prep batch indicator */}
      {mealPrepMode && meal.mealPrepBatch && (
        <div className="absolute -top-1 -left-1 z-10">
          <Badge className="bg-blue-500 text-white text-xs px-1 py-0 h-5">
            {meal.mealPrepBatch.split('-')[1]}
          </Badge>
        </div>
      )}

      {/* Main content */}
      <div className="space-y-1">
        {/* Meal name */}
        <h4 className="text-xs font-medium line-clamp-2 leading-tight text-card-foreground">
          {meal.name}
        </h4>

        {/* Calories and grade */}
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted-foreground">
            {meal.calories} cal
          </span>
          <Badge className={`text-xs px-1 py-0 h-4 ${getGradeColor(meal.grade)}`}>
            {meal.grade}
          </Badge>
        </div>

        {/* Time and difficulty */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1">
            <Clock className="w-3 h-3 text-muted-foreground" />
            <span className="text-xs text-muted-foreground">
              {meal.prepTime + meal.cookTime}m
            </span>
          </div>
          <div className="flex items-center gap-0.5">
            {getDifficultyChefs(meal.difficulty)}
          </div>
        </div>

        {/* Dietary badges */}
        {meal.dietary.length > 0 && (
          <div className="flex gap-1 flex-wrap">
            {meal.dietary.map((diet) => (
              <Badge
                key={diet}
                variant="secondary"
                className="text-xs px-1 py-0 h-4 bg-secondary/50 text-secondary-foreground"
              >
                {diet}
              </Badge>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}