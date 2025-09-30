import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Badge } from "./ui/badge";
import { 
  Search,
  Filter,
  Plus,
  Grid3X3,
  List,
  Mic,
  Camera,
  Link,
  ScanLine,
  Settings,
  Clock,
  Zap,
  Leaf,
  Heart,
  Users
} from "lucide-react";
import { SearchFilters } from "./Recipes";

interface RecipesHeaderProps {
  searchFilters: SearchFilters;
  setSearchFilters: (filters: SearchFilters) => void;
  viewMode: 'grid' | 'list';
  setViewMode: (mode: 'grid' | 'list') => void;
  recipeCount: number;
}

export function RecipesHeader({ 
  searchFilters, 
  setSearchFilters, 
  viewMode, 
  setViewMode, 
  recipeCount 
}: RecipesHeaderProps) {
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const [voiceSearchActive, setVoiceSearchActive] = useState(false);

  // Quick filter options
  const quickFilters = [
    { id: 'pantry', label: 'Cook with what you have', icon: <Leaf className="w-3 h-3" />, color: 'bg-green-100 text-green-800 dark:bg-green-950' },
    { id: 'quick', label: 'Quick meals', icon: <Clock className="w-3 h-3" />, color: 'bg-blue-100 text-blue-800 dark:bg-blue-950' },
    { id: 'protein', label: 'High protein', icon: <Zap className="w-3 h-3" />, color: 'bg-purple-100 text-purple-800 dark:bg-purple-950' },
    { id: 'lowcarb', label: 'Low carb', icon: <Heart className="w-3 h-3" />, color: 'bg-red-100 text-red-800 dark:bg-red-950' },
    { id: 'vegetarian', label: 'Vegetarian', icon: <Leaf className="w-3 h-3" />, color: 'bg-green-100 text-green-800 dark:bg-green-950' },
    { id: 'mealprep', label: 'Meal prep friendly', icon: <Settings className="w-3 h-3" />, color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950' },
    { id: 'family', label: 'Family favorites', icon: <Users className="w-3 h-3" />, color: 'bg-orange-100 text-orange-800 dark:bg-orange-950' }
  ];

  const [activeQuickFilters, setActiveQuickFilters] = useState<string[]>([]);

  const toggleQuickFilter = (filterId: string) => {
    setActiveQuickFilters(prev => 
      prev.includes(filterId) 
        ? prev.filter(id => id !== filterId)
        : [...prev, filterId]
    );
  };

  const handleVoiceSearch = () => {
    setVoiceSearchActive(true);
    // Mock voice search functionality
    setTimeout(() => {
      setVoiceSearchActive(false);
      setSearchFilters({
        ...searchFilters,
        query: "healthy chicken recipes"
      });
    }, 2000);
  };

  const getActiveFilterCount = () => {
    let count = 0;
    if (searchFilters.query) count++;
    if (searchFilters.cuisine.length > 0) count++;
    if (searchFilters.dietary.length > 0) count++;
    if (searchFilters.difficulty.length > 0) count++;
    if (searchFilters.maxTime < 180) count++;
    if (searchFilters.minRating > 0) count++;
    if (searchFilters.availableIngredients) count++;
    if (activeQuickFilters.length > 0) count++;
    return count;
  };

  return (
    <div className="bg-card border-b border-border px-4 py-4 space-y-4">
      {/* Title and Actions Row */}
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-card-foreground">Recipes</h1>
        
        <div className="flex items-center gap-2">
          {/* View Toggle */}
          <div className="flex bg-muted rounded-lg p-1">
            <Button
              variant={viewMode === 'grid' ? 'default' : 'ghost'}
              size="sm"
              className="h-8 w-8 p-0"
              onClick={() => setViewMode('grid')}
            >
              <Grid3X3 className="w-4 h-4" />
            </Button>
            <Button
              variant={viewMode === 'list' ? 'default' : 'ghost'}
              size="sm"
              className="h-8 w-8 p-0"
              onClick={() => setViewMode('list')}
            >
              <List className="w-4 h-4" />
            </Button>
          </div>

          {/* Add Recipe Button */}
          <div className="relative">
            <Button size="sm" className="gap-1.5 bg-green-600 hover:bg-green-700">
              <Plus className="w-4 h-4" />
              Add
            </Button>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search recipes, ingredients, or cuisines..."
            value={searchFilters.query}
            onChange={(e) => setSearchFilters({ ...searchFilters, query: e.target.value })}
            className="pl-10 pr-20"
          />
          <div className="absolute right-2 top-1/2 transform -translate-y-1/2 flex items-center gap-1">
            <Button
              variant="ghost"
              size="sm"
              className={`h-8 w-8 p-0 ${voiceSearchActive ? 'text-red-500 animate-pulse' : ''}`}
              onClick={handleVoiceSearch}
            >
              <Mic className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0 relative"
              onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
            >
              <Filter className="w-4 h-4" />
              {getActiveFilterCount() > 0 && (
                <Badge className="absolute -top-1 -right-1 h-4 w-4 p-0 text-xs bg-red-500 text-white">
                  {getActiveFilterCount()}
                </Badge>
              )}
            </Button>
          </div>
        </div>

        {/* Recipe Count */}
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">
            {recipeCount.toLocaleString()} recipes available
          </span>
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" className="gap-1.5 h-8 text-xs">
              <Camera className="w-3 h-3" />
              Scan Recipe
            </Button>
            <Button variant="ghost" size="sm" className="gap-1.5 h-8 text-xs">
              <Link className="w-3 h-3" />
              Import URL
            </Button>
          </div>
        </div>
      </div>

      {/* Quick Filter Chips */}
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-card-foreground">Quick Filters:</span>
        </div>
        <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
          {quickFilters.map((filter) => (
            <Badge
              key={filter.id}
              className={`
                flex items-center gap-1.5 px-3 py-1.5 cursor-pointer whitespace-nowrap transition-all
                ${activeQuickFilters.includes(filter.id) 
                  ? `${filter.color} ring-2 ring-offset-1 ring-current` 
                  : 'bg-muted text-muted-foreground hover:bg-accent'
                }
              `}
              onClick={() => toggleQuickFilter(filter.id)}
            >
              {filter.icon}
              {filter.label}
            </Badge>
          ))}
        </div>
      </div>

      {/* Smart Suggestions Row */}
      <div className="bg-gradient-to-r from-green-50 to-blue-50 dark:from-green-950 dark:to-blue-950 rounded-lg p-3 border border-green-200 dark:border-green-800">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ScanLine className="w-4 h-4 text-green-600" />
            <div>
              <p className="text-sm font-medium text-green-800 dark:text-green-200">
                Cook Tonight Suggestion
              </p>
              <p className="text-xs text-green-700 dark:text-green-300">
                Mediterranean Quinoa Bowl - You have 4/5 ingredients
              </p>
            </div>
          </div>
          <Button size="sm" variant="outline" className="border-green-200 dark:border-green-800">
            View Recipe
          </Button>
        </div>
      </div>

      {/* Advanced Filters Panel */}
      {showAdvancedFilters && (
        <div className="bg-muted/50 rounded-lg p-4 space-y-4 border">
          <div className="flex items-center justify-between">
            <h3 className="font-medium text-card-foreground">Advanced Filters</h3>
            <Button 
              variant="ghost" 
              size="sm"
              onClick={() => {
                setSearchFilters({
                  query: '',
                  cuisine: [],
                  dietary: [],
                  difficulty: [],
                  maxTime: 180,
                  minRating: 0,
                  availableIngredients: false,
                  nutritionalGoals: [],
                  costRange: [0, 20]
                });
                setActiveQuickFilters([]);
              }}
            >
              Clear All
            </Button>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-card-foreground">Max Cook Time</label>
              <div className="flex items-center gap-2">
                <span className="text-xs text-muted-foreground">15m</span>
                <div className="flex-1 h-2 bg-muted rounded-full relative">
                  <div 
                    className="h-full bg-green-500 rounded-full"
                    style={{ width: `${(searchFilters.maxTime / 180) * 100}%` }}
                  />
                </div>
                <span className="text-xs text-muted-foreground">3h</span>
              </div>
              <p className="text-xs text-muted-foreground">
                Up to {searchFilters.maxTime} minutes
              </p>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-card-foreground">Min Rating</label>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Button
                    key={star}
                    variant="ghost"
                    size="sm"
                    className="h-8 w-8 p-0"
                    onClick={() => setSearchFilters({ ...searchFilters, minRating: star })}
                  >
                    <Heart 
                      className={`w-4 h-4 ${
                        star <= searchFilters.minRating 
                          ? 'text-red-500 fill-current' 
                          : 'text-muted-foreground'
                      }`} 
                    />
                  </Button>
                ))}
              </div>
            </div>
          </div>

          <div className="flex gap-2">
            <Button 
              variant={searchFilters.availableIngredients ? "default" : "outline"}
              size="sm"
              onClick={() => setSearchFilters({ 
                ...searchFilters, 
                availableIngredients: !searchFilters.availableIngredients 
              })}
            >
              Only show recipes I can make
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}