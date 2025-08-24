import { useState } from "react";
import { MealSlot } from "./MealSlot";
import { Badge } from "./ui/badge";

interface WeeklyCalendarProps {
  currentWeek: Date;
  mealPrepMode: boolean;
}

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

export function WeeklyCalendar({ currentWeek, mealPrepMode }: WeeklyCalendarProps) {
  const [meals, setMeals] = useState<Record<string, Record<string, MealData | null>>>({});

  const getDaysOfWeek = (startDate: Date) => {
    const days = [];
    const start = new Date(startDate);
    const day = start.getDay();
    const diff = start.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Monday start
    start.setDate(diff);

    for (let i = 0; i < 7; i++) {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      days.push(date);
    }
    return days;
  };

  const days = getDaysOfWeek(currentWeek);
  const mealTypes = [
    { id: 'breakfast', name: 'Breakfast', color: 'bg-yellow-100 border-yellow-300 dark:bg-yellow-950 dark:border-yellow-800' },
    { id: 'lunch', name: 'Lunch', color: 'bg-green-100 border-green-300 dark:bg-green-950 dark:border-green-800' },
    { id: 'dinner', name: 'Dinner', color: 'bg-blue-100 border-blue-300 dark:bg-blue-950 dark:border-blue-800' },
    { id: 'snacks', name: 'Snacks', color: 'bg-orange-100 border-orange-300 dark:bg-orange-950 dark:border-orange-800' },
    { id: 'supplements', name: 'Supplements', color: 'bg-purple-100 border-purple-300 dark:bg-purple-950 dark:border-purple-800' }
  ];

  // Mock data for demonstration
  const mockMeals: Record<string, MealData> = {
    'mon-breakfast': {
      id: 'mon-breakfast',
      name: 'Overnight Oats with Berries',
      calories: 350,
      prepTime: 10,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V', 'GF'],
      completed: true,
      mealPrepBatch: 'batch-1'
    },
    'mon-lunch': {
      id: 'mon-lunch',
      name: 'Mediterranean Wrap',
      calories: 420,
      prepTime: 5,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V'],
      completed: true
    },
    'tue-breakfast': {
      id: 'tue-breakfast',
      name: 'Protein Smoothie Bowl',
      calories: 290,
      prepTime: 5,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V', 'GF'],
      completed: false,
      mealPrepBatch: 'batch-1'
    },
    'tue-lunch': {
      id: 'tue-lunch',
      name: 'Quinoa Buddha Bowl',
      calories: 485,
      prepTime: 15,
      cookTime: 20,
      difficulty: 2,
      grade: 'A',
      dietary: ['V', 'GF'],
      completed: false,
      mealPrepBatch: 'batch-2'
    },
    'tue-dinner': {
      id: 'tue-dinner',
      name: 'Chicken Stir Fry',
      calories: 450,
      prepTime: 10,
      cookTime: 15,
      difficulty: 2,
      grade: 'B',
      dietary: ['GF'],
      completed: false
    },
    'wed-breakfast': {
      id: 'wed-breakfast',
      name: 'Avocado Toast',
      calories: 320,
      prepTime: 5,
      cookTime: 2,
      difficulty: 1,
      grade: 'B',
      dietary: ['V'],
      completed: false
    },
    'wed-dinner': {
      id: 'wed-dinner',
      name: 'Grilled Salmon with Veggies',
      calories: 520,
      prepTime: 10,
      cookTime: 25,
      difficulty: 2,
      grade: 'B',
      dietary: ['GF'],
      completed: false
    },
    'thu-lunch': {
      id: 'thu-lunch',
      name: 'Asian Rice Bowl',
      calories: 410,
      prepTime: 12,
      cookTime: 18,
      difficulty: 2,
      grade: 'A',
      dietary: ['GF'],
      completed: false,
      mealPrepBatch: 'batch-2'
    },
    'fri-breakfast': {
      id: 'fri-breakfast',
      name: 'Greek Yogurt Parfait',
      calories: 250,
      prepTime: 3,
      cookTime: 0,
      difficulty: 1,
      grade: 'B',
      dietary: ['V'],
      completed: false
    },
    'fri-snacks': {
      id: 'fri-snacks',
      name: 'Mixed Nuts & Fruit',
      calories: 180,
      prepTime: 2,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: ['V', 'GF'],
      completed: false
    },
    'sat-supplements': {
      id: 'sat-supplements',
      name: 'Daily Vitamins',
      calories: 0,
      prepTime: 1,
      cookTime: 0,
      difficulty: 1,
      grade: 'A',
      dietary: [],
      completed: false
    }
  };

  const getMeal = (dayIndex: number, mealType: string) => {
    const day = days[dayIndex];
    const dayKey = day.toLocaleDateString('en-CA'); // YYYY-MM-DD format
    const mealKey = `${dayKey}-${mealType}`;
    
    // For demo purposes, also check simplified keys
    const dayNames = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
    const simpleMealKey = `${dayNames[day.getDay()]}-${mealType}`;
    
    return mockMeals[mealKey] || mockMeals[simpleMealKey] || null;
  };

  const getDayCalories = (dayIndex: number) => {
    let total = 0;
    mealTypes.forEach(mealType => {
      const meal = getMeal(dayIndex, mealType.id);
      if (meal) total += meal.calories;
    });
    return total;
  };

  return (
    <div className="p-4">
      {/* Days Header */}
      <div className="grid grid-cols-7 gap-1 sm:gap-2 mb-4">
        {days.map((day, index) => {
          const isToday = day.toDateString() === new Date().toDateString();
          const dayCalories = getDayCalories(index);
          
          return (
            <div key={index} className="text-center">
              <div className={`p-1 sm:p-2 rounded-lg ${isToday ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-muted/50'}`}>
                <div className="text-xs text-muted-foreground uppercase tracking-wide">
                  {day.toLocaleDateString('en-US', { weekday: 'short' })}
                </div>
                <div className="text-sm sm:text-lg font-semibold">
                  {day.getDate()}
                </div>
                {dayCalories > 0 && (
                  <div className="text-xs text-muted-foreground mt-1 hidden sm:block">
                    {dayCalories} cal
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Meal Grid */}
      <div className="space-y-3">
        {mealTypes.map((mealType) => (
          <div key={mealType.id} className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-card-foreground min-w-[80px]">
                {mealType.name}
              </span>
              <div className="flex-1 h-px bg-border"></div>
            </div>
            
            <div className="grid grid-cols-7 gap-1 sm:gap-2">
              {days.map((day, dayIndex) => {
                const meal = getMeal(dayIndex, mealType.id);
                
                return (
                  <MealSlot
                    key={`${dayIndex}-${mealType.id}`}
                    meal={meal}
                    mealType={mealType}
                    mealPrepMode={mealPrepMode}
                    onMealClick={(meal) => {
                      // Handle meal click - could open edit modal
                      console.log('Clicked meal:', meal);
                    }}
                    onAddMeal={() => {
                      // Handle add meal - could open meal selection
                      console.log('Add meal for:', day, mealType.id);
                    }}
                  />
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}