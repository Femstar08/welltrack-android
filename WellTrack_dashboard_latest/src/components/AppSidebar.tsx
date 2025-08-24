import { 
  Sidebar, 
  SidebarContent, 
  SidebarHeader, 
  SidebarMenu, 
  SidebarMenuButton, 
  SidebarMenuItem,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarGroupContent,
  SidebarSeparator
} from "./ui/sidebar";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Progress } from "./ui/progress";
import { 
  CalendarDays,
  ChefHat,
  Clock,
  Filter,
  Heart,
  Lightbulb,
  PlusCircle,
  Search,
  Settings,
  Star,
  TrendingUp,
  Users,
  Utensils,
  Zap,
  Target,
  Apple,
  Activity,
  TestTube,
  BarChart3,
  Sun
} from "lucide-react";

interface AppSidebarProps {
  currentScreen: string;
  onNavigate: (screen: string) => void;
}

export function AppSidebar({ currentScreen, onNavigate }: AppSidebarProps) {
  
  const renderMealPlannerSidebar = () => (
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Quick Actions</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <PlusCircle className="w-4 h-4" />
                <span>Add Meal</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <Zap className="w-4 h-4" />
                <span>Auto-Generate Week</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <Users className="w-4 h-4" />
                <span>Family Plans</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Meal Suggestions</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="space-y-3 px-2">
            <Card className="border border-border">
              <CardContent className="p-3">
                <div className="flex items-center gap-2 mb-2">
                  <ChefHat className="w-4 h-4 text-orange-500" />
                  <span className="font-medium text-sm">Grilled Salmon</span>
                  <Badge variant="secondary" className="text-xs">High Protein</Badge>
                </div>
                <p className="text-xs text-muted-foreground mb-2">
                  Perfect for dinner with asparagus and quinoa
                </p>
                <div className="flex items-center gap-2 text-xs">
                  <Clock className="w-3 h-3" />
                  <span>25 mins</span>
                  <span>•</span>
                  <span>485 cal</span>
                </div>
              </CardContent>
            </Card>

            <Card className="border border-border">
              <CardContent className="p-3">
                <div className="flex items-center gap-2 mb-2">
                  <Apple className="w-4 h-4 text-green-500" />
                  <span className="font-medium text-sm">Greek Yogurt Bowl</span>
                  <Badge variant="secondary" className="text-xs">Quick</Badge>
                </div>
                <p className="text-xs text-muted-foreground mb-2">
                  Great breakfast with berries and granola
                </p>
                <div className="flex items-center gap-2 text-xs">
                  <Clock className="w-3 h-3" />
                  <span>5 mins</span>
                  <span>•</span>
                  <span>320 cal</span>
                </div>
              </CardContent>
            </Card>

            <Card className="border border-border">
              <CardContent className="p-3">
                <div className="flex items-center gap-2 mb-2">
                  <Utensils className="w-4 h-4 text-blue-500" />
                  <span className="font-medium text-sm">Quinoa Salad</span>
                  <Badge variant="secondary" className="text-xs">Vegan</Badge>
                </div>
                <p className="text-xs text-muted-foreground mb-2">
                  Nutritious lunch with mixed vegetables
                </p>
                <div className="flex items-center gap-2 text-xs">
                  <Clock className="w-3 h-3" />
                  <span>15 mins</span>
                  <span>•</span>
                  <span>380 cal</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Nutrition Goals</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-3">
            <div>
              <div className="flex items-center justify-between text-sm mb-1">
                <span>Daily Protein</span>
                <span>85/120g</span>
              </div>
              <Progress value={71} className="h-2" />
            </div>
            <div>
              <div className="flex items-center justify-between text-sm mb-1">
                <span>Daily Fiber</span>
                <span>18/25g</span>
              </div>
              <Progress value={72} className="h-2" />
            </div>
            <div>
              <div className="flex items-center justify-between text-sm mb-1">
                <span>Weekly Variety</span>
                <span>12/15 foods</span>
              </div>
              <Progress value={80} className="h-2" />
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  );

  const renderRecipesSidebar = () => (
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Search & Filter</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input 
                type="text" 
                placeholder="Search recipes..."
                className="w-full pl-9 pr-3 py-2 text-sm border border-border rounded-md bg-background"
              />
            </div>
            
            <div className="space-y-2">
              <Button variant="outline" size="sm" className="w-full justify-start">
                <Filter className="w-4 h-4 mr-2" />
                Dietary Filters
              </Button>
              <Button variant="outline" size="sm" className="w-full justify-start">
                <Clock className="w-4 h-4 mr-2" />
                Cooking Time
              </Button>
              <Button variant="outline" size="sm" className="w-full justify-start">
                <ChefHat className="w-4 h-4 mr-2" />
                Difficulty Level
              </Button>
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Categories</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🥗</span>
                <span>Salads & Bowls</span>
                <Badge variant="secondary" className="ml-auto">24</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🍖</span>
                <span>Main Dishes</span>
                <Badge variant="secondary" className="ml-auto">42</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🥤</span>
                <span>Smoothies</span>
                <Badge variant="secondary" className="ml-auto">18</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🍜</span>
                <span>Soups</span>
                <Badge variant="secondary" className="ml-auto">15</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🥘</span>
                <span>One-Pot Meals</span>
                <Badge variant="secondary" className="ml-auto">28</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <span>🍰</span>
                <span>Healthy Desserts</span>
                <Badge variant="secondary" className="ml-auto">12</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Popular Tags</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="flex flex-wrap gap-1 px-2">
            <Badge variant="outline" className="text-xs">High Protein</Badge>
            <Badge variant="outline" className="text-xs">Low Carb</Badge>
            <Badge variant="outline" className="text-xs">Vegan</Badge>
            <Badge variant="outline" className="text-xs">Gluten-Free</Badge>
            <Badge variant="outline" className="text-xs">Quick</Badge>
            <Badge variant="outline" className="text-xs">Meal Prep</Badge>
            <Badge variant="outline" className="text-xs">Anti-Inflammatory</Badge>
            <Badge variant="outline" className="text-xs">Heart Healthy</Badge>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  );

  const renderHealthAnalyticsSidebar = () => (
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Health Metrics</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-3">
            <Card className="border border-border">
              <CardContent className="p-3">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Heart className="w-4 h-4 text-red-500" />
                    <span className="font-medium text-sm">Overall Score</span>
                  </div>
                  <Badge className="bg-green-100 text-green-800 dark:bg-green-950">A-</Badge>
                </div>
                <div className="text-2xl font-semibold text-green-600 mb-1">87%</div>
                <Progress value={87} className="h-2" />
              </CardContent>
            </Card>

            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2">
                  <Activity className="w-3 h-3 text-blue-500" />
                  Fitness
                </span>
                <span className="font-medium">92%</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2">
                  <Apple className="w-3 h-3 text-green-500" />
                  Nutrition
                </span>
                <span className="font-medium">85%</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2">
                  <Clock className="w-3 h-3 text-purple-500" />
                  Sleep
                </span>
                <span className="font-medium">78%</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2">
                  <Target className="w-3 h-3 text-orange-500" />
                  Goals
                </span>
                <span className="font-medium">90%</span>
              </div>
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Quick Insights</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-2">
            <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-2">
              <div className="flex items-center gap-2 mb-1">
                <TrendingUp className="w-3 h-3 text-green-600" />
                <span className="text-xs font-medium">Improvement</span>
              </div>
              <p className="text-xs text-muted-foreground">
                Your sleep quality has improved by 15% this week
              </p>
            </div>

            <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-2">
              <div className="flex items-center gap-2 mb-1">
                <Lightbulb className="w-3 h-3 text-blue-600" />
                <span className="text-xs font-medium">Recommendation</span>
              </div>
              <p className="text-xs text-muted-foreground">
                Consider increasing your daily step goal to 12,000
              </p>
            </div>

            <div className="bg-orange-50 dark:bg-orange-950 border border-orange-200 dark:border-orange-800 rounded-lg p-2">
              <div className="flex items-center gap-2 mb-1">
                <Star className="w-3 h-3 text-orange-600" />
                <span className="text-xs font-medium">Achievement</span>
              </div>
              <p className="text-xs text-muted-foreground">
                7-day streak of meeting protein goals!
              </p>
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Time Ranges</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <CalendarDays className="w-4 h-4" />
                <span>Today</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <CalendarDays className="w-4 h-4" />
                <span>This Week</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <CalendarDays className="w-4 h-4" />
                <span>This Month</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <CalendarDays className="w-4 h-4" />
                <span>Last 3 Months</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  );

  const renderBiomarkersSidebar = () => (
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Quick Actions</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <PlusCircle className="w-4 h-4" />
                <span>Add Results</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <TestTube className="w-4 h-4" />
                <span>Schedule Test</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <BarChart3 className="w-4 h-4" />
                <span>View Trends</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Priority Markers</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-2">
            <div className="flex items-center justify-between text-sm p-2 bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-800 rounded-lg">
              <span>Vitamin D3</span>
              <div className="text-right">
                <div className="font-medium text-red-600">32 ng/mL</div>
                <div className="text-xs text-muted-foreground">Below optimal</div>
              </div>
            </div>

            <div className="flex items-center justify-between text-sm p-2 bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg">
              <span>Testosterone</span>
              <div className="text-right">
                <div className="font-medium text-green-600">720 ng/dL</div>
                <div className="text-xs text-muted-foreground">Optimal</div>
              </div>
            </div>

            <div className="flex items-center justify-between text-sm p-2 bg-yellow-50 dark:bg-yellow-950 border border-yellow-200 dark:border-yellow-800 rounded-lg">
              <span>HbA1C</span>
              <div className="text-right">
                <div className="font-medium text-yellow-600">5.1%</div>
                <div className="text-xs text-muted-foreground">Normal</div>
              </div>
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Test Categories</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <Zap className="w-4 h-4 text-orange-500" />
                <span>Hormonal</span>
                <Badge variant="secondary" className="ml-auto">3</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <Activity className="w-4 h-4 text-green-500" />
                <span>Micronutrients</span>
                <Badge variant="secondary" className="ml-auto">3</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton>
                <Heart className="w-4 h-4 text-red-500" />
                <span>General Health</span>
                <Badge variant="secondary" className="ml-auto">2</Badge>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarSeparator />

      <SidebarGroup>
        <SidebarGroupLabel>Upcoming Tests</SidebarGroupLabel>
        <SidebarGroupContent>
          <div className="px-2 space-y-2">
            <div className="bg-orange-50 dark:bg-orange-950 border border-orange-200 dark:border-orange-800 rounded-lg p-2">
              <div className="font-medium text-sm">Hormone Panel</div>
              <div className="text-xs text-muted-foreground">Due in 5 days</div>
              <div className="text-xs text-orange-600 mt-1">Fasting required</div>
            </div>

            <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-2">
              <div className="font-medium text-sm">Micronutrient Panel</div>
              <div className="text-xs text-muted-foreground">Due in 3 weeks</div>
              <div className="text-xs text-blue-600 mt-1">No preparation needed</div>
            </div>
          </div>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  );

  const renderSidebarContent = () => {
    switch (currentScreen) {
      case "meal-planner":
        return renderMealPlannerSidebar();
      case "recipes":
        return renderRecipesSidebar();
      case "health-analytics":
        return renderHealthAnalyticsSidebar();
      case "biomarkers":
        return renderBiomarkersSidebar();
      default:
        return null;
    }
  };

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="border-b border-border">
        <div className="flex items-center gap-2 px-2 py-1">
          <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-primary text-primary-foreground">
            <Heart className="w-4 h-4" />
          </div>
          <div className="flex flex-col">
            <span className="font-semibold">WellTrack</span>
            <span className="text-xs text-muted-foreground capitalize">
              {currentScreen.replace('-', ' ')} Tools
            </span>
          </div>
        </div>
        
        {/* Mobile hint */}
        <div className="md:hidden px-2 pb-2">
          <div className="text-xs text-muted-foreground bg-muted/50 rounded-lg p-2">
            💡 Swipe from left edge or tap the menu button to access these tools
          </div>
        </div>
      </SidebarHeader>
      
      {renderSidebarContent()}
    </Sidebar>
  );
}