import { useState, useEffect } from "react";
import { Button } from "./ui/button";
import { Checkbox } from "./ui/checkbox";
import { Progress } from "./ui/progress";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { 
  Play, 
  Pause, 
  Check, 
  Clock, 
  ChefHat,
  AlertCircle,
  CheckCircle
} from "lucide-react";
import { PrepSession } from "./MealPrepStorage";

interface ActivePrepSessionProps {
  session: PrepSession;
  onComplete: () => void;
  onPause: () => void;
}

interface PrepStep {
  id: number;
  instruction: string;
  estimatedTime: number;
  isCompleted: boolean;
  hasTimer: boolean;
  timerActive?: boolean;
  timeRemaining?: number;
}

interface Ingredient {
  id: string;
  name: string;
  amount: string;
  isChecked: boolean;
}

export function ActivePrepSession({ session, onComplete, onPause }: ActivePrepSessionProps) {
  const [steps, setSteps] = useState<PrepStep[]>([
    { id: 1, instruction: "Rinse and prepare quinoa", estimatedTime: 5, isCompleted: true, hasTimer: false },
    { id: 2, instruction: "Chop vegetables (bell peppers, cucumber, carrots)", estimatedTime: 10, isCompleted: true, hasTimer: false },
    { id: 3, instruction: "Cook quinoa in boiling water", estimatedTime: 15, isCompleted: false, hasTimer: true, timerActive: true, timeRemaining: 720 },
    { id: 4, instruction: "Prepare tahini dressing", estimatedTime: 5, isCompleted: false, hasTimer: false },
    { id: 5, instruction: "Massage kale with lemon juice", estimatedTime: 3, isCompleted: false, hasTimer: false },
    { id: 6, instruction: "Roast chickpeas with spices", estimatedTime: 20, isCompleted: false, hasTimer: true },
    { id: 7, instruction: "Assemble bowls with quinoa and vegetables", estimatedTime: 10, isCompleted: false, hasTimer: false },
    { id: 8, instruction: "Portion into meal prep containers", estimatedTime: 5, isCompleted: false, hasTimer: false }
  ]);

  const [ingredients, setIngredients] = useState<Ingredient[]>([
    { id: '1', name: '1 cup quinoa', amount: '', isChecked: true },
    { id: '2', name: '2 bell peppers', amount: '', isChecked: true },
    { id: '3', name: '1 cucumber', amount: '', isChecked: false },
    { id: '4', name: '2 carrots', amount: '', isChecked: false },
    { id: '5', name: '4 cups kale', amount: '', isChecked: false },
    { id: '6', name: '1 can chickpeas', amount: '', isChecked: false },
    { id: '7', name: '3 tbsp tahini', amount: '', isChecked: false },
    { id: '8', name: '1 lemon', amount: '', isChecked: false }
  ]);

  // Timer countdown effect
  useEffect(() => {
    const interval = setInterval(() => {
      setSteps(prev => prev.map(step => {
        if (step.timerActive && step.timeRemaining && step.timeRemaining > 0) {
          return { ...step, timeRemaining: step.timeRemaining - 1 };
        }
        return step;
      }));
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const toggleStep = (stepId: number) => {
    setSteps(prev => prev.map(step => 
      step.id === stepId ? { ...step, isCompleted: !step.isCompleted } : step
    ));
  };

  const toggleIngredient = (ingredientId: string) => {
    setIngredients(prev => prev.map(ingredient =>
      ingredient.id === ingredientId 
        ? { ...ingredient, isChecked: !ingredient.isChecked }
        : ingredient
    ));
  };

  const startTimer = (stepId: number) => {
    setSteps(prev => prev.map(step =>
      step.id === stepId 
        ? { ...step, timerActive: true, timeRemaining: step.estimatedTime * 60 }
        : step
    ));
  };

  const currentStep = steps.find(step => !step.isCompleted);
  const progressPercentage = (steps.filter(step => step.isCompleted).length / steps.length) * 100;

  return (
    <div className="bg-green-50 dark:bg-green-950 border-b border-green-200 dark:border-green-800 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Main Prep Instructions */}
          <div className="flex-1 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-medium text-green-800 dark:text-green-200">
                Current Step: {currentStep?.instruction || "All steps completed!"}
              </h3>
              <div className="flex items-center gap-2">
                <Button variant="outline" size="sm" onClick={onPause}>
                  <Pause className="w-4 h-4" />
                  Pause
                </Button>
                {currentStep && (
                  <Button onClick={() => toggleStep(currentStep.id)} size="sm">
                    <Check className="w-4 h-4" />
                    Complete Step
                  </Button>
                )}
              </div>
            </div>

            {/* Progress Bar */}
            <div className="space-y-2">
              <Progress value={progressPercentage} className="h-2" />
              <p className="text-sm text-green-700 dark:text-green-300">
                {steps.filter(step => step.isCompleted).length} of {steps.length} steps completed
              </p>
            </div>

            {/* Active Timers */}
            {steps.some(step => step.timerActive) && (
              <Card className="bg-orange-50 dark:bg-orange-950 border-orange-200 dark:border-orange-800">
                <CardHeader className="pb-3">
                  <CardTitle className="flex items-center gap-2 text-orange-800 dark:text-orange-200">
                    <Clock className="w-4 h-4" />
                    Active Timers
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  {steps.filter(step => step.timerActive).map(step => (
                    <div key={step.id} className="flex items-center justify-between p-2 bg-orange-100 dark:bg-orange-900 rounded">
                      <span className="text-sm text-orange-800 dark:text-orange-200">
                        Step {step.id}: {step.instruction}
                      </span>
                      <div className="flex items-center gap-2">
                        <Badge className={`font-mono ${
                          (step.timeRemaining || 0) <= 60 
                            ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' 
                            : 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200'
                        }`}>
                          {formatTime(step.timeRemaining || 0)}
                        </Badge>
                        {(step.timeRemaining || 0) <= 0 && (
                          <AlertCircle className="w-4 h-4 text-red-500" />
                        )}
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>
            )}

            {/* All Steps List */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center gap-2">
                  <ChefHat className="w-4 h-4" />
                  Prep Steps
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {steps.map(step => (
                  <div 
                    key={step.id} 
                    className={`flex items-center gap-3 p-2 rounded ${
                      step.isCompleted 
                        ? 'bg-green-50 dark:bg-green-950' 
                        : step.id === currentStep?.id 
                          ? 'bg-blue-50 dark:bg-blue-950 ring-2 ring-blue-200 dark:ring-blue-800' 
                          : 'bg-muted/30'
                    }`}
                  >
                    <Checkbox
                      checked={step.isCompleted}
                      onCheckedChange={() => toggleStep(step.id)}
                    />
                    <div className="flex-1">
                      <p className={`text-sm ${step.isCompleted ? 'line-through text-muted-foreground' : 'text-card-foreground'}`}>
                        {step.instruction}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        ~{step.estimatedTime} minutes
                      </p>
                    </div>
                    {step.hasTimer && !step.isCompleted && !step.timerActive && (
                      <Button 
                        size="sm" 
                        variant="outline" 
                        onClick={() => startTimer(step.id)}
                      >
                        <Clock className="w-3 h-3" />
                        Start Timer
                      </Button>
                    )}
                    {step.isCompleted && (
                      <CheckCircle className="w-4 h-4 text-green-500" />
                    )}
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          {/* Ingredient Checklist */}
          <div className="lg:w-80">
            <Card>
              <CardHeader className="pb-3">
                <CardTitle>Ingredients Checklist</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {ingredients.map(ingredient => (
                  <div key={ingredient.id} className="flex items-center gap-2">
                    <Checkbox
                      checked={ingredient.isChecked}
                      onCheckedChange={() => toggleIngredient(ingredient.id)}
                    />
                    <span className={`text-sm ${
                      ingredient.isChecked 
                        ? 'line-through text-muted-foreground' 
                        : 'text-card-foreground'
                    }`}>
                      {ingredient.name}
                    </span>
                  </div>
                ))}
                
                <div className="mt-4 pt-3 border-t border-border">
                  <p className="text-xs text-muted-foreground">
                    {ingredients.filter(i => i.isChecked).length} of {ingredients.length} ingredients ready
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        {/* Complete Session Button */}
        {progressPercentage === 100 && (
          <div className="mt-4 text-center">
            <Button 
              onClick={onComplete} 
              size="lg"
              className="bg-green-500 hover:bg-green-600 text-white"
            >
              Complete Prep Session
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}