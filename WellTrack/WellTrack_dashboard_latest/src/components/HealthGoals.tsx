import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { Slider } from "./ui/slider";
import { 
  Target,
  Plus,
  Edit,
  Trash2,
  TrendingUp,
  TrendingDown,
  Activity,
  Heart,
  Droplets,
  Scale,
  Clock,
  Zap,
  Apple,
  Moon
} from "lucide-react";
import { HealthGoal, NutritionGoals, UserProfile } from "./Profile";

interface HealthGoalsProps {
  healthGoals: HealthGoal[];
  nutritionGoals: NutritionGoals;
  user: UserProfile;
}

export function HealthGoals({ healthGoals, nutritionGoals, user }: HealthGoalsProps) {
  const [selectedGoalType, setSelectedGoalType] = useState<string | null>(null);
  const [showNutritionEditor, setShowNutritionEditor] = useState(false);

  const getGoalIcon = (type: string) => {
    switch (type) {
      case 'weight_loss':
      case 'weight_gain':
      case 'maintain_weight':
        return <Scale className="w-5 h-5 text-blue-500" />;
      case 'muscle_gain':
      case 'improve_fitness':
        return <Activity className="w-5 h-5 text-green-500" />;
      case 'better_sleep':
        return <Moon className="w-5 h-5 text-purple-500" />;
      case 'reduce_stress':
        return <Heart className="w-5 h-5 text-red-500" />;
      default:
        return <Target className="w-5 h-5 text-gray-500" />;
    }
  };

  const getGoalTitle = (type: string) => {
    switch (type) {
      case 'weight_loss': return 'Weight Loss';
      case 'weight_gain': return 'Weight Gain';
      case 'maintain_weight': return 'Maintain Weight';
      case 'muscle_gain': return 'Muscle Gain';
      case 'improve_fitness': return 'Improve Fitness';
      case 'better_sleep': return 'Better Sleep';
      case 'reduce_stress': return 'Reduce Stress';
      default: return 'Unknown Goal';
    }
  };

  const getProgressPercentage = (goal: HealthGoal) => {
    if (goal.type === 'weight_loss') {
      const totalToLose = user.weight - goal.target;
      const currentLoss = user.weight - goal.current;
      return Math.min((currentLoss / totalToLose) * 100, 100);
    }
    if (goal.type === 'weight_gain') {
      const totalToGain = goal.target - user.weight;
      const currentGain = goal.current - user.weight;
      return Math.min((currentGain / totalToGain) * 100, 100);
    }
    return (goal.current / goal.target) * 100;
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      case 'medium': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'low': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-950 dark:text-gray-200';
    }
  };

  const getDaysUntilDeadline = (deadline?: string) => {
    if (!deadline) return null;
    const deadlineDate = new Date(deadline);
    const today = new Date();
    const diffTime = deadlineDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Health Goals Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Target className="w-5 h-5 text-blue-500" />
              Health Goals
            </div>
            <Button size="sm">
              <Plus className="w-4 h-4 mr-1" />
              Add Goal
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {healthGoals.filter(goal => goal.isActive).map((goal) => {
            const progress = getProgressPercentage(goal);
            const daysLeft = getDaysUntilDeadline(goal.deadline);
            
            return (
              <Card key={goal.id} className="border border-border/50">
                <CardContent className="p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-3">
                      {getGoalIcon(goal.type)}
                      <div>
                        <h3 className="font-medium text-card-foreground">
                          {getGoalTitle(goal.type)}
                        </h3>
                        <p className="text-sm text-muted-foreground">
                          Target: {goal.target} {goal.unit}
                          {goal.deadline && daysLeft !== null && (
                            <span className="ml-2">
                              • {daysLeft > 0 ? `${daysLeft} days left` : 'Overdue'}
                            </span>
                          )}
                        </p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-2">
                      <Badge className={`text-xs px-2 py-1 ${getPriorityColor(goal.priority)}`}>
                        {goal.priority}
                      </Badge>
                      <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                        <Edit className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Progress</span>
                      <span className="font-medium">
                        {goal.current} / {goal.target} {goal.unit}
                      </span>
                    </div>
                    <Progress value={Math.min(progress, 100)} className="h-2" />
                    <div className="flex justify-between text-xs text-muted-foreground">
                      <span>{progress.toFixed(0)}% complete</span>
                      {progress >= 100 ? (
                        <span className="text-green-600 flex items-center gap-1">
                          <TrendingUp className="w-3 h-3" />
                          Goal achieved!
                        </span>
                      ) : (
                        <span>
                          {(goal.target - goal.current).toFixed(1)} {goal.unit} to go
                        </span>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}

          {healthGoals.filter(goal => goal.isActive).length === 0 && (
            <div className="text-center py-8">
              <Target className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">
                No active health goals set. Create your first goal to start tracking progress!
              </p>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                Set Your First Goal
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Nutrition Goals */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Apple className="w-5 h-5 text-green-500" />
              Daily Nutrition Targets
            </div>
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => setShowNutritionEditor(!showNutritionEditor)}
            >
              <Edit className="w-4 h-4 mr-1" />
              {showNutritionEditor ? 'Save' : 'Edit'}
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Calorie Target */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Zap className="w-4 h-4 text-orange-500" />
                <span className="font-medium">Daily Calories</span>
              </div>
              <span className="font-semibold">{nutritionGoals.calories} kcal</span>
            </div>
            {showNutritionEditor && (
              <div className="space-y-2">
                <Slider
                  value={[nutritionGoals.calories]}
                  onValueChange={(value) => console.log('Update calories:', value[0])}
                  max={3000}
                  min={1200}
                  step={50}
                  className="w-full"
                />
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>1200 kcal</span>
                  <span>3000 kcal</span>
                </div>
              </div>
            )}
          </div>

          {/* Macronutrient Ratios */}
          <div className="space-y-4">
            <h4 className="font-medium text-card-foreground">Macronutrient Distribution</h4>
            
            <div className="space-y-3">
              {/* Protein */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Protein</span>
                  <span className="text-sm font-medium">{nutritionGoals.protein}%</span>
                </div>
                <div className="flex items-center gap-3">
                  <Progress value={nutritionGoals.protein} className="flex-1 h-2" />
                  <span className="text-xs text-muted-foreground w-16">
                    {Math.round(nutritionGoals.calories * nutritionGoals.protein / 100 / 4)}g
                  </span>
                </div>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.protein]}
                    onValueChange={(value) => console.log('Update protein:', value[0])}
                    max={50}
                    min={10}
                    step={1}
                    className="w-full"
                  />
                )}
              </div>

              {/* Carbohydrates */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Carbohydrates</span>
                  <span className="text-sm font-medium">{nutritionGoals.carbs}%</span>
                </div>
                <div className="flex items-center gap-3">
                  <Progress value={nutritionGoals.carbs} className="flex-1 h-2" />
                  <span className="text-xs text-muted-foreground w-16">
                    {Math.round(nutritionGoals.calories * nutritionGoals.carbs / 100 / 4)}g
                  </span>
                </div>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.carbs]}
                    onValueChange={(value) => console.log('Update carbs:', value[0])}
                    max={70}
                    min={20}
                    step={1}
                    className="w-full"
                  />
                )}
              </div>

              {/* Fats */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Fats</span>
                  <span className="text-sm font-medium">{nutritionGoals.fats}%</span>
                </div>
                <div className="flex items-center gap-3">
                  <Progress value={nutritionGoals.fats} className="flex-1 h-2" />
                  <span className="text-xs text-muted-foreground w-16">
                    {Math.round(nutritionGoals.calories * nutritionGoals.fats / 100 / 9)}g
                  </span>
                </div>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.fats]}
                    onValueChange={(value) => console.log('Update fats:', value[0])}
                    max={50}
                    min={15}
                    step={1}
                    className="w-full"
                  />
                )}
              </div>
            </div>
          </div>

          {/* Other Nutrition Targets */}
          <div className="space-y-4">
            <h4 className="font-medium text-card-foreground">Other Daily Targets</h4>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Droplets className="w-4 h-4 text-blue-500" />
                  <span className="text-sm">Water</span>
                </div>
                <p className="font-medium">{nutritionGoals.water}L</p>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.water]}
                    onValueChange={(value) => console.log('Update water:', value[0])}
                    max={5}
                    min={1}
                    step={0.1}
                    className="w-full"
                  />
                )}
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Activity className="w-4 h-4 text-green-500" />
                  <span className="text-sm">Fiber</span>
                </div>
                <p className="font-medium">{nutritionGoals.fiber}g</p>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.fiber]}
                    onValueChange={(value) => console.log('Update fiber:', value[0])}
                    max={50}
                    min={15}
                    step={1}
                    className="w-full"
                  />
                )}
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Heart className="w-4 h-4 text-red-500" />
                  <span className="text-sm">Sodium</span>
                </div>
                <p className="font-medium">{nutritionGoals.sodium}mg</p>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.sodium]}
                    onValueChange={(value) => console.log('Update sodium:', value[0])}
                    max={3000}
                    min={1500}
                    step={100}
                    className="w-full"
                  />
                )}
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Zap className="w-4 h-4 text-yellow-500" />
                  <span className="text-sm">Sugar</span>
                </div>
                <p className="font-medium">{nutritionGoals.sugar}g</p>
                {showNutritionEditor && (
                  <Slider
                    value={[nutritionGoals.sugar]}
                    onValueChange={(value) => console.log('Update sugar:', value[0])}
                    max={100}
                    min={20}
                    step={5}
                    className="w-full"
                  />
                )}
              </div>
            </div>
          </div>

          {/* Goal Templates */}
          {!showNutritionEditor && (
            <div className="space-y-3">
              <h4 className="font-medium text-card-foreground">Quick Goal Templates</h4>
              <div className="grid grid-cols-2 gap-2">
                <Button variant="outline" size="sm" className="justify-start">
                  Weight Loss
                </Button>
                <Button variant="outline" size="sm" className="justify-start">
                  Muscle Gain
                </Button>
                <Button variant="outline" size="sm" className="justify-start">
                  Athletic Performance
                </Button>
                <Button variant="outline" size="sm" className="justify-start">
                  General Health
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Fitness Goals */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-green-500" />
            Fitness Targets
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">
                10,000
              </div>
              <p className="text-sm text-muted-foreground">Daily Steps</p>
            </div>
            
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">
                3
              </div>
              <p className="text-sm text-muted-foreground">Workouts/Week</p>
            </div>
            
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">
                8h
              </div>
              <p className="text-sm text-muted-foreground">Sleep Target</p>
            </div>
            
            <div className="text-center p-4 bg-muted/30 rounded-lg">
              <div className="text-2xl font-semibold text-card-foreground mb-1">
                150
              </div>
              <p className="text-sm text-muted-foreground">Active Minutes</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}