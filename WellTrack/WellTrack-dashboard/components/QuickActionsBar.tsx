import { Button } from "./ui/button";
import { Switch } from "./ui/switch";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Sparkles, Copy, Filter, ChefHat, Lightbulb } from "lucide-react";

interface QuickActionsBarProps {
  mealPrepMode: boolean;
  onMealPrepModeChange: (enabled: boolean) => void;
  onShowSuggestions: () => void;
  showSuggestions: boolean;
}

export function QuickActionsBar({ 
  mealPrepMode, 
  onMealPrepModeChange, 
  onShowSuggestions,
  showSuggestions 
}: QuickActionsBarProps) {
  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between gap-3">
        {/* Primary Action */}
        <Button className="bg-green-500 hover:bg-green-600 text-white gap-2 flex-1 max-w-[140px]">
          <Sparkles className="w-4 h-4" />
          Auto-Generate
        </Button>

        {/* Secondary Actions */}
        <div className="flex items-center gap-2">
          {/* Copy Previous Week */}
          <Button variant="outline" size="sm" className="gap-1.5">
            <Copy className="w-4 h-4" />
            Copy Week
          </Button>

          {/* Meal Prep Mode */}
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-accent/50">
            <ChefHat className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Prep</span>
            <Switch 
              checked={mealPrepMode}
              onCheckedChange={onMealPrepModeChange}
              className="scale-75"
            />
          </div>

          {/* Dietary Filters */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm" className="gap-1.5">
                <Filter className="w-4 h-4" />
                Filters
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>Vegetarian</DropdownMenuItem>
              <DropdownMenuItem>Gluten-Free</DropdownMenuItem>
              <DropdownMenuItem>Keto</DropdownMenuItem>
              <DropdownMenuItem>Low-Carb</DropdownMenuItem>
              <DropdownMenuItem>Dairy-Free</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Suggestions Toggle */}
          <Button 
            variant={showSuggestions ? "default" : "outline"} 
            size="sm" 
            onClick={onShowSuggestions}
            className="gap-1.5"
          >
            <Lightbulb className="w-4 h-4" />
            Ideas
          </Button>
        </div>
      </div>
    </div>
  );
}