import { Card, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { 
  Calendar,
  MapPin,
  Container,
  Users,
  Microwave,
  MoreHorizontal,
  AlertTriangle,
  CheckCircle,
  Clock
} from "lucide-react";
import { StoredMeal } from "./MealPrepStorage";

interface StorageCardProps {
  meal: StoredMeal;
}

export function StorageCard({ meal }: StorageCardProps) {
  const getFreshnessColor = (freshness: string) => {
    switch (freshness) {
      case 'fresh': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      case 'good': return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
      case 'use-soon': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'expired': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const getFreshnessIcon = (freshness: string) => {
    switch (freshness) {
      case 'fresh': return <CheckCircle className="w-3 h-3" />;
      case 'good': return <CheckCircle className="w-3 h-3" />;
      case 'use-soon': return <AlertTriangle className="w-3 h-3" />;
      case 'expired': return <AlertTriangle className="w-3 h-3" />;
      default: return <Clock className="w-3 h-3" />;
    }
  };

  const getLocationIcon = (location: string) => {
    switch (location) {
      case 'fridge': return '🧊';
      case 'freezer': return '❄️';
      case 'pantry': return '📦';
      default: return '📍';
    }
  };

  const getDaysUntilExpiry = () => {
    const now = new Date();
    const expiry = new Date(meal.expiryDate);
    const diffTime = expiry.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const daysUntilExpiry = getDaysUntilExpiry();

  return (
    <Card className="hover:bg-accent/50 transition-colors">
      <CardContent className="p-4">
        <div className="space-y-3">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h4 className="font-medium text-card-foreground">{meal.name}</h4>
              <div className="flex items-center gap-2 mt-1">
                <Badge className={`text-xs px-2 py-0.5 ${getFreshnessColor(meal.freshness)}`}>
                  {getFreshnessIcon(meal.freshness)}
                  <span className="ml-1 capitalize">{meal.freshness}</span>
                </Badge>
                <span className="text-xs text-muted-foreground">
                  {getLocationIcon(meal.location)} {meal.location}
                </span>
              </div>
            </div>
            
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                  <MoreHorizontal className="w-4 h-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem>Edit Details</DropdownMenuItem>
                <DropdownMenuItem>Move Location</DropdownMenuItem>
                <DropdownMenuItem>Update Portions</DropdownMenuItem>
                <DropdownMenuItem className="text-red-600">Mark as Consumed</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="flex items-center gap-2">
              <Calendar className="w-3 h-3 text-muted-foreground" />
              <div>
                <p className="text-muted-foreground">Prepped</p>
                <p className="text-card-foreground">
                  {meal.prepDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <Clock className="w-3 h-3 text-muted-foreground" />
              <div>
                <p className="text-muted-foreground">Expires</p>
                <p className={`${
                  daysUntilExpiry <= 2 ? 'text-red-600' : 
                  daysUntilExpiry <= 5 ? 'text-yellow-600' : 
                  'text-green-600'
                }`}>
                  {daysUntilExpiry > 0 ? `${daysUntilExpiry} days` : 'Expired'}
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <Users className="w-3 h-3 text-muted-foreground" />
              <div>
                <p className="text-muted-foreground">Portions</p>
                <p className="text-card-foreground">
                  {meal.remainingPortions} of {meal.portions}
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <Container className="w-3 h-3 text-muted-foreground" />
              <div>
                <p className="text-muted-foreground">Container</p>
                <p className="text-card-foreground text-xs">{meal.containerType}</p>
              </div>
            </div>
          </div>

          {/* Reheating Instructions */}
          <div className="bg-muted/30 rounded-lg p-2">
            <div className="flex items-start gap-2">
              <Microwave className="w-3 h-3 text-muted-foreground mt-0.5" />
              <div>
                <p className="text-xs text-muted-foreground">Reheating</p>
                <p className="text-xs text-card-foreground">{meal.reheatingInstructions}</p>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-2">
            <Button variant="outline" size="sm" className="flex-1">
              Reheat Instructions
            </Button>
            <Button variant="outline" size="sm" className="flex-1">
              Mark as Used
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}