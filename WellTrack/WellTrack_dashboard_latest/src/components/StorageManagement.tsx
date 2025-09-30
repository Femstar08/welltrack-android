import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Progress } from "./ui/progress";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { StorageCard } from "./StorageCard";
import { 
  Refrigerator, 
  Snowflake, 
  Package, 
  Container,
  AlertTriangle,
  CheckCircle,
  MoreHorizontal
} from "lucide-react";
import { StoredMeal } from "./MealPrepStorage";

export function StorageManagement() {
  const storageCapacity = {
    fridge: { used: 75, total: 100, unit: '%' },
    freezer: { used: 45, total: 100, unit: '%' },
    pantry: { used: 60, total: 100, unit: '%' },
    containers: { used: 12, total: 18, unit: 'containers' }
  };

  const storedMeals: StoredMeal[] = [
    {
      id: '1',
      name: 'Quinoa Buddha Bowls',
      prepDate: new Date('2024-11-17'),
      expiryDate: new Date('2024-11-21'),
      location: 'fridge',
      containerType: 'Glass meal prep containers',
      portions: 4,
      remainingPortions: 3,
      reheatingInstructions: 'Microwave 2-3 minutes, add fresh herbs',
      freshness: 'good'
    },
    {
      id: '2',
      name: 'Chicken Stir Fry',
      prepDate: new Date('2024-11-16'),
      expiryDate: new Date('2024-11-20'),
      location: 'fridge',
      containerType: 'Plastic containers',
      portions: 6,
      remainingPortions: 2,
      reheatingInstructions: 'Stovetop with splash of water, 5 minutes',
      freshness: 'use-soon'
    },
    {
      id: '3',
      name: 'Vegetable Soup',
      prepDate: new Date('2024-11-15'),
      expiryDate: new Date('2024-12-15'),
      location: 'freezer',
      containerType: 'Freezer bags',
      portions: 8,
      remainingPortions: 6,
      reheatingInstructions: 'Thaw overnight, heat in pot 10 minutes',
      freshness: 'fresh'
    },
    {
      id: '4',
      name: 'Overnight Oats',
      prepDate: new Date('2024-11-18'),
      expiryDate: new Date('2024-11-23'),
      location: 'fridge',
      containerType: 'Mason jars',
      portions: 5,
      remainingPortions: 4,
      reheatingInstructions: 'Serve cold or warm gently',
      freshness: 'fresh'
    },
    {
      id: '5',
      name: 'Protein Smoothie Packs',
      prepDate: new Date('2024-11-14'),
      expiryDate: new Date('2024-02-14'),
      location: 'freezer',
      containerType: 'Freezer bags',
      portions: 10,
      remainingPortions: 7,
      reheatingInstructions: 'Blend with liquid of choice',
      freshness: 'fresh'
    }
  ];

  const getStorageIcon = (location: string) => {
    switch (location) {
      case 'fridge': return <Refrigerator className="w-4 h-4 text-blue-500" />;
      case 'freezer': return <Snowflake className="w-4 h-4 text-blue-600" />;
      case 'pantry': return <Package className="w-4 h-4 text-orange-500" />;
      default: return <Container className="w-4 h-4 text-muted-foreground" />;
    }
  };

  const getCapacityColor = (percentage: number) => {
    if (percentage >= 90) return 'text-red-600';
    if (percentage >= 75) return 'text-yellow-600';
    return 'text-green-600';
  };

  const getFreshnessStats = () => {
    const fresh = storedMeals.filter(meal => meal.freshness === 'fresh').length;
    const good = storedMeals.filter(meal => meal.freshness === 'good').length;
    const useSoon = storedMeals.filter(meal => meal.freshness === 'use-soon').length;
    const expired = storedMeals.filter(meal => meal.freshness === 'expired').length;
    
    return { fresh, good, useSoon, expired };
  };

  const freshnessStats = getFreshnessStats();

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Storage Overview */}
      <section>
        <h3 className="font-medium text-card-foreground mb-4">Storage Overview</h3>
        
        <div className="grid grid-cols-2 gap-3">
          {Object.entries(storageCapacity).map(([location, capacity]) => {
            const percentage = location === 'containers' 
              ? (capacity.used / capacity.total) * 100 
              : capacity.used;
            
            return (
              <Card key={location}>
                <CardContent className="p-3">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      {getStorageIcon(location)}
                      <span className="text-sm font-medium capitalize">{location}</span>
                    </div>
                    <span className={`text-sm font-medium ${getCapacityColor(percentage)}`}>
                      {location === 'containers' 
                        ? `${capacity.used}/${capacity.total}`
                        : `${capacity.used}%`
                      }
                    </span>
                  </div>
                  <Progress value={percentage} className="h-2" />
                  <p className="text-xs text-muted-foreground mt-1">
                    {location === 'containers' 
                      ? `${capacity.total - capacity.used} available`
                      : `${100 - capacity.used}% space remaining`
                    }
                  </p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </section>

      {/* Freshness Alert */}
      {freshnessStats.useSoon > 0 && (
        <Card className="bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800">
          <CardContent className="p-3">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-yellow-600" />
              <span className="text-sm text-yellow-800 dark:text-yellow-200 font-medium">
                {freshnessStats.useSoon} meal{freshnessStats.useSoon !== 1 ? 's' : ''} should be used soon
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Meal Inventory */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-medium text-card-foreground">Prepared Meals</h3>
          <div className="flex items-center gap-2">
            <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200">
              <CheckCircle className="w-3 h-3 mr-1" />
              {freshnessStats.fresh} Fresh
            </Badge>
            <Badge className="bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200">
              <AlertTriangle className="w-3 h-3 mr-1" />
              {freshnessStats.useSoon} Use Soon
            </Badge>
          </div>
        </div>

        <div className="space-y-3">
          {storedMeals.map((meal) => (
            <StorageCard key={meal.id} meal={meal} />
          ))}
        </div>
      </section>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Storage Actions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start gap-2">
            <Container className="w-4 h-4" />
            Clean Empty Containers
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Package className="w-4 h-4" />
            Check Expiry Dates
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Snowflake className="w-4 h-4" />
            Defrost Items for Tomorrow
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}