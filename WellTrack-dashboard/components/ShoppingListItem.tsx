import { Checkbox } from "./ui/checkbox";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { 
  DollarSign, 
  Clock, 
  AlertTriangle, 
  Star, 
  ChefHat,
  Edit,
  MoreHorizontal
} from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { ShoppingItem } from "./ShoppingList";

interface ShoppingListItemProps {
  item: ShoppingItem;
  onToggle: () => void;
}

export function ShoppingListItem({ item, onToggle }: ShoppingListItemProps) {
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'essential': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      case 'preferred': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'optional': return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const getSourceIcon = (source: string) => {
    switch (source) {
      case 'meal-plan': return <ChefHat className="w-3 h-3" />;
      case 'pantry': return <AlertTriangle className="w-3 h-3" />;
      case 'manual': return <Edit className="w-3 h-3" />;
      default: return null;
    }
  };

  const getSourceColor = (source: string) => {
    switch (source) {
      case 'meal-plan': return 'text-green-600';
      case 'pantry': return 'text-orange-600';
      case 'manual': return 'text-blue-600';
      default: return 'text-muted-foreground';
    }
  };

  return (
    <div 
      className={`p-3 bg-card border border-border rounded-lg transition-all ${
        item.completed 
          ? 'opacity-60 bg-muted/50' 
          : 'hover:bg-accent/50'
      }`}
    >
      <div className="flex items-start gap-3">
        {/* Checkbox */}
        <Checkbox
          checked={item.completed}
          onCheckedChange={onToggle}
          className="mt-1"
        />

        {/* Main Content */}
        <div className="flex-1 space-y-2">
          {/* Item Name and Brand */}
          <div className="flex items-start justify-between">
            <div>
              <h4 className={`font-medium ${
                item.completed ? 'line-through text-muted-foreground' : 'text-card-foreground'
              }`}>
                {item.name}
              </h4>
              {item.brand && (
                <p className="text-xs text-muted-foreground">{item.brand}</p>
              )}
            </div>
            
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                  <MoreHorizontal className="w-4 h-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem>Edit Item</DropdownMenuItem>
                <DropdownMenuItem>Adjust Quantity</DropdownMenuItem>
                <DropdownMenuItem>Mark as Unavailable</DropdownMenuItem>
                <DropdownMenuItem className="text-red-600">Remove Item</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          {/* Quantity and Price */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">
                {item.quantity} {item.unit}
              </span>
              {item.estimatedPrice && (
                <div className="flex items-center gap-1">
                  <DollarSign className="w-3 h-3 text-muted-foreground" />
                  <span className="text-sm text-muted-foreground">
                    {item.estimatedPrice.toFixed(2)}
                  </span>
                </div>
              )}
            </div>

            {/* Priority Badge */}
            <Badge className={`text-xs px-1.5 py-0.5 ${getPriorityColor(item.priority)}`}>
              {item.priority}
            </Badge>
          </div>

          {/* Recipe Context */}
          {item.recipeContext && (
            <div className="flex items-center gap-1">
              <ChefHat className="w-3 h-3 text-green-600" />
              <span className="text-xs text-green-600">{item.recipeContext}</span>
            </div>
          )}

          {/* Source and Notes */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1">
              <span className={getSourceColor(item.source)}>
                {getSourceIcon(item.source)}
              </span>
              <span className={`text-xs ${getSourceColor(item.source)}`}>
                {item.source === 'meal-plan' ? 'From meal plan' : 
                 item.source === 'pantry' ? 'Low stock' : 'Added manually'}
              </span>
            </div>

            {item.notes && (
              <span className="text-xs text-muted-foreground">
                💭 {item.notes}
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}