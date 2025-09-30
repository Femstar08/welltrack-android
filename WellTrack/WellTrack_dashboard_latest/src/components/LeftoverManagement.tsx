import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { 
  AlertTriangle,
  Lightbulb,
  Snowflake,
  ChefHat,
  Clock,
  Trash2,
  RefreshCw
} from "lucide-react";
import { LeftoverItem } from "./MealPrepStorage";

export function LeftoverManagement() {
  const leftovers: LeftoverItem[] = [
    {
      id: '1',
      name: 'Roasted Vegetables',
      quantity: '2 cups',
      expiryDate: new Date('2024-11-20'),
      location: 'fridge',
      freshness: 'good',
      suggestions: ['Add to grain bowls', 'Make vegetable soup', 'Blend into pasta sauce']
    },
    {
      id: '2',
      name: 'Cooked Quinoa',
      quantity: '1.5 cups',
      expiryDate: new Date('2024-11-19'),
      location: 'fridge',
      freshness: 'use-soon',
      suggestions: ['Quinoa salad', 'Stuffed peppers', 'Breakfast bowl']
    },
    {
      id: '3',
      name: 'Grilled Chicken',
      quantity: '8 oz',
      expiryDate: new Date('2024-11-21'),
      location: 'fridge',
      freshness: 'fresh',
      suggestions: ['Chicken salad wraps', 'Pasta with chicken', 'Chicken fried rice']
    },
    {
      id: '4',
      name: 'Mixed Berries',
      quantity: '1 cup',
      expiryDate: new Date('2024-11-18'),
      location: 'fridge',
      freshness: 'use-soon',
      suggestions: ['Smoothie', 'Oatmeal topping', 'Freeze for later']
    }
  ];

  const transformSuggestions = [
    {
      id: '1',
      title: 'Rainbow Grain Bowl',
      description: 'Combine roasted vegetables + quinoa + grilled chicken',
      ingredients: ['Roasted Vegetables', 'Cooked Quinoa', 'Grilled Chicken'],
      time: 5,
      difficulty: 1
    },
    {
      id: '2',
      title: 'Quick Fried Rice',
      description: 'Transform quinoa into Asian-style fried rice',
      ingredients: ['Cooked Quinoa', 'Grilled Chicken'],
      time: 15,
      difficulty: 2
    },
    {
      id: '3',
      title: 'Berry Smoothie Bowl',
      description: 'Blend berries with protein powder',
      ingredients: ['Mixed Berries'],
      time: 3,
      difficulty: 1
    }
  ];

  const getFreshnessColor = (freshness: string) => {
    switch (freshness) {
      case 'fresh': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      case 'good': return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
      case 'use-soon': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'expired': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const getDaysUntilExpiry = (expiryDate: Date) => {
    const now = new Date();
    const diffTime = expiryDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const useSoonCount = leftovers.filter(item => item.freshness === 'use-soon').length;
  const totalWasteValue = 12.45; // Mock calculation

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Leftover Stats */}
      <div className="grid grid-cols-3 gap-3">
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-semibold text-card-foreground">{leftovers.length}</p>
            <p className="text-xs text-muted-foreground">Active Leftovers</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-semibold text-yellow-600">{useSoonCount}</p>
            <p className="text-xs text-muted-foreground">Use Soon</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-semibold text-green-600">${totalWasteValue.toFixed(2)}</p>
            <p className="text-xs text-muted-foreground">Saved Value</p>
          </CardContent>
        </Card>
      </div>

      {/* Priority Alert */}
      {useSoonCount > 0 && (
        <Card className="bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800">
          <CardContent className="p-3">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-yellow-600" />
              <span className="text-sm text-yellow-800 dark:text-yellow-200 font-medium">
                {useSoonCount} leftover{useSoonCount !== 1 ? 's' : ''} need attention today
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Active Leftovers */}
      <section>
        <h3 className="font-medium text-card-foreground mb-4">Active Leftovers</h3>
        
        <div className="space-y-3">
          {leftovers.map((item) => {
            const daysLeft = getDaysUntilExpiry(item.expiryDate);
            
            return (
              <Card key={item.id} className="hover:bg-accent/50 transition-colors">
                <CardContent className="p-4">
                  <div className="space-y-3">
                    {/* Header */}
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="font-medium text-card-foreground">{item.name}</h4>
                        <div className="flex items-center gap-2 mt-1">
                          <span className="text-sm text-muted-foreground">{item.quantity}</span>
                          <Badge className={`text-xs px-2 py-0.5 ${getFreshnessColor(item.freshness)}`}>
                            {item.freshness === 'use-soon' ? `${daysLeft} day${daysLeft !== 1 ? 's' : ''} left` : item.freshness}
                          </Badge>
                          <span className="text-xs text-muted-foreground">
                            {item.location === 'fridge' ? '🧊' : '❄️'} {item.location}
                          </span>
                        </div>
                      </div>
                      
                      <div className="flex gap-1">
                        <Button variant="outline" size="sm" className="h-8 w-8 p-0">
                          <Snowflake className="w-3 h-3" />
                        </Button>
                        <Button variant="outline" size="sm" className="h-8 w-8 p-0">
                          <Trash2 className="w-3 h-3" />
                        </Button>
                      </div>
                    </div>

                    {/* Quick Suggestions */}
                    <div>
                      <div className="flex items-center gap-1 mb-2">
                        <Lightbulb className="w-3 h-3 text-yellow-500" />
                        <span className="text-xs text-muted-foreground">Quick ideas:</span>
                      </div>
                      <div className="flex flex-wrap gap-1">
                        {item.suggestions.slice(0, 2).map((suggestion, index) => (
                          <Badge key={index} variant="secondary" className="text-xs px-2 py-0.5 cursor-pointer hover:bg-accent">
                            {suggestion}
                          </Badge>
                        ))}
                        {item.suggestions.length > 2 && (
                          <Badge variant="secondary" className="text-xs px-2 py-0.5 cursor-pointer hover:bg-accent">
                            +{item.suggestions.length - 2} more
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </section>

      {/* Transform Leftovers */}
      <section>
        <div className="flex items-center gap-2 mb-4">
          <RefreshCw className="w-4 h-4 text-purple-500" />
          <h3 className="font-medium text-card-foreground">Transform Into New Meals</h3>
        </div>

        <div className="space-y-3">
          {transformSuggestions.map((suggestion) => (
            <Card key={suggestion.id} className="hover:bg-accent/50 transition-colors cursor-pointer">
              <CardContent className="p-4">
                <div className="space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="font-medium text-card-foreground">{suggestion.title}</h4>
                      <p className="text-sm text-muted-foreground">{suggestion.description}</p>
                    </div>
                    <Button size="sm" className="gap-1.5">
                      <ChefHat className="w-3 h-3" />
                      Cook This
                    </Button>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">{suggestion.time}min</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <span className="text-xs text-muted-foreground">
                        {'⭐'.repeat(suggestion.difficulty)} Easy
                      </span>
                    </div>
                  </div>

                  <div>
                    <p className="text-xs text-muted-foreground mb-1">Uses:</p>
                    <div className="flex flex-wrap gap-1">
                      {suggestion.ingredients.map((ingredient, index) => (
                        <Badge key={index} className="bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-200 text-xs px-2 py-0.5">
                          {ingredient}
                        </Badge>
                      ))}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Waste Prevention Tips */}
      <Card className="bg-green-50 dark:bg-green-950 border-green-200 dark:border-green-800">
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-green-800 dark:text-green-200">
            <Lightbulb className="w-4 h-4" />
            Waste Prevention Tips
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="text-sm text-green-700 dark:text-green-300 space-y-1">
            <p>• Freeze portions before they expire</p>
            <p>• Blend vegetables into smoothies or sauces</p>
            <p>• Transform grains into new dishes quickly</p>
            <p>• Use herbs and citrus to refresh old flavors</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}