import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { 
  Clock, 
  Users, 
  ChefHat, 
  Calendar,
  Utensils,
  TrendingUp,
  Star,
  Play
} from "lucide-react";

interface PrepPlanningProps {
  weekView: boolean;
}

interface BatchOpportunity {
  id: string;
  name: string;
  recipes: string[];
  sharedIngredients: string[];
  totalTime: number;
  efficiency: number;
  difficulty: 1 | 2 | 3;
  portions: number;
}

interface PrepSchedule {
  day: string;
  date: string;
  sessions: PrepScheduleItem[];
}

interface PrepScheduleItem {
  id: string;
  name: string;
  type: 'ingredient-prep' | 'full-meal' | 'batch-cook';
  time: string;
  duration: number;
  difficulty: 1 | 2 | 3;
  recipes: string[];
}

export function PrepPlanningSection({ weekView }: PrepPlanningProps) {
  const batchOpportunities: BatchOpportunity[] = [
    {
      id: '1',
      name: 'Grain Bowl Batch',
      recipes: ['Quinoa Buddha Bowl', 'Mediterranean Bowl', 'Asian Rice Bowl'],
      sharedIngredients: ['quinoa', 'chickpeas', 'vegetables', 'tahini'],
      totalTime: 90,
      efficiency: 85,
      difficulty: 2,
      portions: 12
    },
    {
      id: '2',
      name: 'Protein Prep',
      recipes: ['Grilled Chicken', 'Baked Salmon', 'Tofu Stir Fry'],
      sharedIngredients: ['herbs', 'spices', 'oil'],
      totalTime: 60,
      efficiency: 75,
      difficulty: 2,
      portions: 8
    },
    {
      id: '3',
      name: 'Breakfast Batch',
      recipes: ['Overnight Oats', 'Chia Pudding', 'Protein Smoothie Packs'],
      sharedIngredients: ['oats', 'chia seeds', 'berries', 'protein powder'],
      totalTime: 30,
      efficiency: 95,
      difficulty: 1,
      portions: 15
    }
  ];

  const weeklySchedule: PrepSchedule[] = [
    {
      day: 'Sunday',
      date: 'Nov 19',
      sessions: [
        {
          id: '1',
          name: 'Grain Bowl Batch',
          type: 'batch-cook',
          time: '10:00 AM',
          duration: 90,
          difficulty: 2,
          recipes: ['Quinoa Buddha Bowl', 'Mediterranean Bowl', 'Asian Rice Bowl']
        },
        {
          id: '2',
          name: 'Breakfast Prep',
          type: 'ingredient-prep',
          time: '2:00 PM',
          duration: 30,
          difficulty: 1,
          recipes: ['Overnight Oats', 'Chia Pudding']
        }
      ]
    },
    {
      day: 'Wednesday',
      date: 'Nov 22',
      sessions: [
        {
          id: '3',
          name: 'Protein Prep',
          type: 'full-meal',
          time: '6:00 PM',
          duration: 60,
          difficulty: 2,
          recipes: ['Grilled Chicken', 'Baked Salmon']
        }
      ]
    }
  ];

  const getDifficultyColor = (difficulty: number) => {
    switch (difficulty) {
      case 1: return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      case 2: return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 3: return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'ingredient-prep': return <Utensils className="w-4 h-4" />;
      case 'full-meal': return <ChefHat className="w-4 h-4" />;
      case 'batch-cook': return <Users className="w-4 h-4" />;
      default: return <Clock className="w-4 h-4" />;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'ingredient-prep': return 'text-blue-600';
      case 'full-meal': return 'text-green-600';
      case 'batch-cook': return 'text-purple-600';
      default: return 'text-muted-foreground';
    }
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Batch Cooking Opportunities */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-medium text-card-foreground">Batch Cooking Opportunities</h3>
          <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200">
            Save 2.5 hours
          </Badge>
        </div>
        
        <div className="space-y-3">
          {batchOpportunities.map((batch) => (
            <Card key={batch.id} className="hover:bg-accent/50 transition-colors">
              <CardContent className="p-4">
                <div className="space-y-3">
                  {/* Header */}
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="font-medium text-card-foreground">{batch.name}</h4>
                      <p className="text-xs text-muted-foreground">
                        {batch.recipes.join(', ')}
                      </p>
                    </div>
                    <Button size="sm" className="gap-1.5">
                      <Play className="w-3 h-3" />
                      Start Prep
                    </Button>
                  </div>

                  {/* Metrics */}
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">{batch.totalTime}min</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <TrendingUp className="w-3 h-3 text-green-600" />
                      <span className="text-xs text-green-600">{batch.efficiency}% efficient</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Users className="w-3 h-3 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">{batch.portions} portions</span>
                    </div>
                    <Badge className={`text-xs px-1.5 py-0.5 ${getDifficultyColor(batch.difficulty)}`}>
                      Level {batch.difficulty}
                    </Badge>
                  </div>

                  {/* Shared Ingredients */}
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">Shared ingredients:</p>
                    <div className="flex flex-wrap gap-1">
                      {batch.sharedIngredients.map((ingredient, index) => (
                        <Badge key={index} variant="secondary" className="text-xs px-1.5 py-0.5">
                          {ingredient}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  {/* Efficiency Bar */}
                  <div className="space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">Efficiency</span>
                      <span className="text-xs text-green-600">{batch.efficiency}%</span>
                    </div>
                    <Progress value={batch.efficiency} className="h-1" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Weekly Prep Schedule */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-medium text-card-foreground">
            {weekView ? 'Weekly Prep Schedule' : 'Upcoming Prep Sessions'}
          </h3>
          <Button variant="outline" size="sm">
            <Calendar className="w-4 h-4" />
            Customize Schedule
          </Button>
        </div>

        <div className="space-y-4">
          {weeklySchedule.map((day) => (
            <Card key={day.day}>
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center justify-between">
                  <div>
                    <span className="text-sm font-medium">{day.day}</span>
                    <span className="text-xs text-muted-foreground ml-2">{day.date}</span>
                  </div>
                  <Badge variant="secondary" className="text-xs">
                    {day.sessions.length} session{day.sessions.length !== 1 ? 's' : ''}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {day.sessions.map((session) => (
                  <div key={session.id} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className={`p-2 rounded-lg bg-accent ${getTypeColor(session.type)}`}>
                        {getTypeIcon(session.type)}
                      </div>
                      
                      <div>
                        <h5 className="font-medium text-card-foreground">{session.name}</h5>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <span>{session.time}</span>
                          <span>•</span>
                          <span>{session.duration}min</span>
                          <span>•</span>
                          <Badge className={`text-xs px-1.5 py-0.5 ${getDifficultyColor(session.difficulty)}`}>
                            Level {session.difficulty}
                          </Badge>
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                          {session.recipes.join(', ')}
                        </p>
                      </div>
                    </div>
                    
                    <Button variant="outline" size="sm">
                      Start
                    </Button>
                  </div>
                ))}
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Equipment Checklist */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Utensils className="w-4 h-4" />
            Equipment Needed Today
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-2">
            {[
              'Large pot for quinoa',
              'Baking sheet',
              'Mixing bowls (3)',
              'Sharp knife',
              'Cutting board',
              'Measuring cups',
              'Storage containers (8)',
              'Kitchen timer'
            ].map((equipment, index) => (
              <div key={index} className="flex items-center gap-2 text-sm">
                <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                <span className="text-muted-foreground">{equipment}</span>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}