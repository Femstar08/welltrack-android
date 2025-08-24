import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { CheckCircle, Circle, Utensils, ChevronDown, ChevronUp, Plus, Clock } from "lucide-react";
import { useState } from "react";

interface Meal {
  id: string;
  name: string;
  completed: boolean;
  calories?: number;
  time: string;
}

export function MealCard() {
  const [isOpen, setIsOpen] = useState(true);
  const [meals, setMeals] = useState<Meal[]>([
    { id: '1', name: "Breakfast", completed: true, calories: 420, time: "8:00 AM" },
    { id: '2', name: "Lunch", completed: true, calories: 650, time: "12:30 PM" },
    { id: '3', name: "Dinner", completed: false, time: "7:00 PM" },
    { id: '4', name: "Snacks", completed: false, calories: 180, time: "3:00 PM" }
  ]);

  const toggleMeal = (id: string) => {
    setMeals(meals.map(meal => 
      meal.id === id ? { ...meal, completed: !meal.completed } : meal
    ));
  };

  const addCalories = (id: string, calories: number) => {
    setMeals(meals.map(meal => 
      meal.id === id ? { ...meal, calories: calories } : meal
    ));
  };

  const completedMeals = meals.filter(meal => meal.completed).length;
  const totalCalories = meals.reduce((sum, meal) => sum + (meal.calories || 0), 0);

  return (
    <Card className="bg-card shadow-sm border border-border">
      <Collapsible open={isOpen} onOpenChange={setIsOpen}>
        <CollapsibleTrigger className="w-full">
          <CardHeader className="pb-3 px-4 py-4">
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Utensils className="w-6 h-6 text-green-600" />
                <span className="text-lg text-card-foreground">Today's Meals</span>
              </div>
              <div className="flex items-center space-x-2">
                <Badge variant="secondary" className="bg-green-100 text-green-700 px-3 py-1 dark:bg-green-900 dark:text-green-300">
                  {completedMeals}/{meals.length}
                </Badge>
                {isOpen ? (
                  <ChevronUp className="w-5 h-5 text-muted-foreground" />
                ) : (
                  <ChevronDown className="w-5 h-5 text-muted-foreground" />
                )}
              </div>
            </CardTitle>
          </CardHeader>
        </CollapsibleTrigger>
        
        <CollapsibleContent>
          <CardContent className="px-4 pb-4 space-y-3">
            {meals.map((meal) => (
              <div 
                key={meal.id} 
                className={`group relative p-4 rounded-xl border-2 transition-all duration-300 cursor-pointer ${
                  meal.completed 
                    ? 'bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800' 
                    : 'bg-muted/30 border-border hover:border-green-300 hover:bg-green-50/50 dark:hover:bg-green-950/30'
                }`}
                onClick={() => toggleMeal(meal.id)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className={`transition-all duration-300 ${meal.completed ? 'scale-110' : 'scale-100'}`}>
                      {meal.completed ? (
                        <CheckCircle className="w-7 h-7 text-green-500 drop-shadow-sm" />
                      ) : (
                        <Circle className="w-7 h-7 text-muted-foreground group-hover:text-green-400 transition-colors" />
                      )}
                    </div>
                    <div className="flex-1">
                      <h4 className={`text-base font-medium ${
                        meal.completed ? "text-green-800 dark:text-green-200" : "text-card-foreground"
                      }`}>
                        {meal.name}
                      </h4>
                      <div className="flex items-center space-x-2 mt-1">
                        <Clock className="w-3 h-3 text-muted-foreground" />
                        <span className="text-sm text-muted-foreground">{meal.time}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    {meal.calories ? (
                      <div className="text-right">
                        <span className="text-base font-semibold text-card-foreground">{meal.calories}</span>
                        <span className="text-sm text-muted-foreground ml-1">cal</span>
                      </div>
                    ) : (
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 hover:bg-green-100 dark:hover:bg-green-900"
                        onClick={(e) => {
                          e.stopPropagation();
                          // Mock adding calories - in a real app this would open a modal/sheet
                          addCalories(meal.id, Math.floor(Math.random() * 300) + 200);
                        }}
                      >
                        <Plus className="w-4 h-4 text-green-600" />
                      </Button>
                    )}
                  </div>
                </div>
                
                {/* Progress bar for incomplete meals */}
                {!meal.completed && (
                  <div className="mt-3 w-full bg-border rounded-full h-1">
                    <div className="bg-green-500 h-1 rounded-full w-0 group-hover:w-1/4 transition-all duration-500"></div>
                  </div>
                )}
              </div>
            ))}
            
            <div className="pt-4 border-t border-border mt-6">
              <div className="flex justify-between items-center py-3 px-4 rounded-lg bg-muted/50">
                <span className="text-base text-card-foreground font-medium">Total Calories</span>
                <div className="text-right">
                  <span className="text-xl font-semibold text-card-foreground">{totalCalories}</span>
                  <span className="text-sm text-muted-foreground ml-1">cal</span>
                </div>
              </div>
              
              {/* Progress towards daily goal */}
              <div className="mt-3 space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Daily Goal: 2000 cal</span>
                  <span className="text-muted-foreground">{Math.round((totalCalories / 2000) * 100)}%</span>
                </div>
                <div className="w-full bg-border rounded-full h-2">
                  <div 
                    className="bg-gradient-to-r from-green-500 to-emerald-600 h-2 rounded-full transition-all duration-1000"
                    style={{ width: `${Math.min((totalCalories / 2000) * 100, 100)}%` }}
                  ></div>
                </div>
              </div>
            </div>
          </CardContent>
        </CollapsibleContent>
      </Collapsible>
    </Card>
  );
}